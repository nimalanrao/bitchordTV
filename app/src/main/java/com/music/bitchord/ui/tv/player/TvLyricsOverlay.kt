package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.music.bitchord.ui.tv.components.TvLyricsBadge
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

/**
 * 1:1 Apple Music TV Synchronized Lyrics Overlay.
 *
 * - Left Column: Album Art Card (~280dp), Song Title & Artist, Stats for Nerds with Info Icon, and Lyrics Badge on bottom-left.
 * - Right Column: Ultra-smooth 6-line Lyrics Viewport (2 top unfocused, 1 middle active, 3 bottom unfocused).
 * - Bottom: Full-width pure white seekbar with smooth linear animation.
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

    val targetFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val smoothFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing),
        label = "lyricsSmoothFraction",
    )

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
        // Main Content: Left Metadata & Artwork + Right 6-line Lyrics Window
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 48.dp,
                    end = 48.dp,
                    top = 28.dp,
                    bottom = 24.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            // Left Column: Album Card, Title, Artist, Stats for Nerds, and Lyrics Logo Badge
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                // Album Art Card
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .shadow(32.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.85f))
                        .clip(RoundedCornerShape(22.dp))
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

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = song.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = TvSFProDisplay,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Artist
                Text(
                    text = song.artist,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = TvSFProDisplay,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Stats for Nerds (Clean HUD directly beside description with Info icon)
                AnimatedVisibility(
                    visible = showNerdStats,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.60f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Stream Stats",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Opus / FLAC • 48 kHz / 24-bit • Lossless Audio",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lyrics Logo on Bottom Left below the Album Cover
                TvLyricsBadge(
                    backgroundColor = Color(0xFF141414),
                )
            }

            // Right Column: Synchronized 6-line Lyrics Window
            Box(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight(),
            ) {
                TvLyricsList(
                    lyrics = lyrics,
                    currentPositionMs = currentPositionMs,
                    isPlaying = isPlaying,
                    isLoading = isLoadingLyrics,
                    error = lyricsError,
                    onRetry = onRetryLyrics,
                    onSeekToTimestamp = onSeek,
                )
            }
        }

        // Bottom Full-Width Progressive Seek Line (Pure White & Glides Smoothly Left-to-Right)
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
                    .fillMaxWidth(smoothFraction)
                    .background(Color.White),
            )
        }
    }
}
