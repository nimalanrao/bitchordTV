package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

/**
 * 1:1 Apple Music TV Lyrics & Player Overlay matching Apple TV Reference Images 4 & 5.
 *
 * - Left Column: Glowing album artwork card, song title, artist.
 * - Right Column: Giant synchronized lyrics with proper word wrapping and singing glow.
 * - Bottom Edge: 4px progress line with instant D-pad Left/Right remote seeking.
 * - Top-Right: Stats for Nerds diagnostic HUD.
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
    isLiked: Boolean = false,
    onToggleLike: (() -> Unit)? = null,
) {
    val showNerdStats by AppSettings.showNerdStats.collectAsState()

    val progressFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onTvKeyEvent(
                onLeft = {
                    val stepMs = 10_000L // 10s seek
                    onSeek((currentPositionMs - stepMs).coerceAtLeast(0L))
                    true
                },
                onRight = {
                    val stepMs = 10_000L // 10s seek
                    onSeek((currentPositionMs + stepMs).coerceAtMost(durationMs))
                    true
                },
                onPlayPause = {
                    onPlayPause()
                    true
                },
                onBack = {
                    onCloseLyrics()
                    true
                },
            ),
    ) {
        // Main Screen Content: Left Art + Right Lyrics
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = TvDimensions.SafeMarginHorizontal,
                    end = TvDimensions.SafeMarginHorizontal,
                    top = 28.dp,
                    bottom = 32.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            // Left Column: Album Art Card + Title & Artist
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .shadow(36.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.8f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
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

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = song.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = TvSFProDisplay,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = song.artist,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = TvSFProDisplay,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Right Column: Synchronized Lyrics
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

        // Top-Right: Stats for Nerds Overlay Card (matching Image 4)
        AnimatedVisibility(
            visible = showNerdStats,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = TvDimensions.SafeMarginHorizontal),
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

        // 4-Pixel Bottom Progress Line for Lyrics Mode
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.18f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF375F), Color(0xFFFA2D48)),
                        ),
                    ),
            )
        }
    }
}
