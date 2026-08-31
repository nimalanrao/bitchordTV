package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.model.Song
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.components.TvIconButton
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay
import com.music.bitchord.ui.tv.theme.TvThemeColors

/**
 * 1:1 Apple Music TV Lyrics Overlay layout.
 * Left Side: Large centered artwork, song title, artist, BitChord brand watermark.
 * Right Side: Full-bleed large-scale synchronized lyrics with smooth spring animations.
 */
@Composable
fun TvLyricsOverlay(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    lyrics: List<LyricLine>?,
    isLoadingLyrics: Boolean,
    lyricsError: String?,
    onRetryLyrics: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCloseLyrics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = TvThemeColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = TvDimensions.SafeMarginVertical,
                bottom = TvDimensions.SafeMarginVertical,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            // Left Column: Artwork (38% width) + Song Info + Brand
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Centered Artwork and metadata
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.6f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!song.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(song.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = song.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = song.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = TvSFProDisplay,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = song.artist,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = TvSFProDisplay,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Bottom Left: Brand Watermark ("BitChord" logo like Apple Music)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    Text(
                        text = "BitChord",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = TvSFProDisplay,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = (-0.4).sp,
                    )
                }
            }

            // Right Column: Synchronized Lyrics View (62% width)
            Box(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight(),
            ) {
                TvLyricsList(
                    lyrics = lyrics,
                    currentPositionMs = currentPositionMs,
                    isLoading = isLoadingLyrics,
                    error = lyricsError,
                    onRetry = onRetryLyrics,
                    onSeekToTimestamp = onSeek,
                )
            }
        }
    }
}
