package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

@Composable
fun TvPlayerLayout(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isLiked: Boolean,
    isShuffleActive: Boolean,
    repeatMode: Int,
    isLyricsActive: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    playingFromSource: String? = null,
    onToggleLike: () -> Unit,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleQueue: () -> Unit,
    onToggleLyrics: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showNerdStats by AppSettings.showNerdStats.collectAsState()

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
        // Top Region: Context Label (Left) & Stats for Nerds (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            // Upper-Left Context Label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = TvColors.AccentRed,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PLAYING FROM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        letterSpacing = 1.sp,
                        fontFamily = TvSFProDisplay,
                        color = TvColors.TextSecondary,
                    )
                    Text(
                        text = playingFromSource ?: song.albumName ?: song.artist,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        fontFamily = TvSFProDisplay,
                        color = TvColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Stats for Nerds Overlay Card (Top Right - matching Image 4)
            AnimatedVisibility(
                visible = showNerdStats,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFA2D48), Color(0xFF00E5FF)),
                            ),
                            RoundedCornerShape(16.dp),
                        )
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "STATS FOR NERDS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W900,
                            fontFamily = TvSFProDisplay,
                            color = Color(0xFFFF2D55),
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = "Codec: Opus / FLAC • 48 kHz / 24-bit",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                        )
                        Text(
                            text = "Bitrate: 320 kbps (Lossless Master)",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
            }
        }

        // Lower Region: Metadata, Progress Bar & Transport Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Metadata Row: Album Thumbnail (68dp) + Title & Artist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TvColors.SurfaceVariant),
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

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = song.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.W800,
                            fontFamily = TvSFProDisplay,
                            color = TvColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // Format Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TvColors.SurfaceSelected)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "LOSSLESS",
                                color = TvColors.AccentRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = TvSFProDisplay,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${song.artist} • ${song.albumName ?: "Single"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = TvSFProDisplay,
                        color = TvColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Thin Horizontal Progress Timeline
            TvPlayerProgress(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onSeek = onSeek,
            )

            // Centered Bottom Transport Controls Row
            TvPlayerControls(
                isPlaying = isPlaying,
                isLiked = isLiked,
                isShuffleActive = isShuffleActive,
                repeatMode = repeatMode,
                isLyricsActive = isLyricsActive,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                onToggleLike = onToggleLike,
                onToggleShuffle = onToggleShuffle,
                onPrevious = onPrevious,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onToggleQueue = onToggleQueue,
                onToggleLyrics = onToggleLyrics,
            )
        }
    }
}
