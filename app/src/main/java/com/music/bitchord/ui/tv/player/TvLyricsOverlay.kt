package com.music.bitchord.ui.tv.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Close
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
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = TvDimensions.SafeMarginVertical,
                bottom = TvDimensions.SafeMarginVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        // Left Column: Compact Metadata & Mini Controls (38% width)
        Column(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(16.dp))
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = song.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.W800,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = song.artist,
                fontSize = 15.sp,
                fontWeight = FontWeight.W500,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Slider
            TvPlayerProgress(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onSeek = onSeek,
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Mini Transport Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TvIconButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    size = 44.dp,
                    onClick = onPrevious,
                )

                TvIconButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    size = 54.dp,
                    iconSize = 28.dp,
                    isPrimary = true,
                    onClick = onPlayPause,
                )

                TvIconButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    size = 44.dp,
                    onClick = onNext,
                )

                Spacer(modifier = Modifier.width(8.dp))

                TvButton(
                    text = "Hide lyrics",
                    isPrimary = false,
                    onClick = onCloseLyrics,
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
