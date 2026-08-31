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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.music.bitchord.ui.tv.components.TvIconButton
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

/**
 * 1:1 Apple Music TV Lyrics & Player Overlay matching Apple TV Reference Images 4 & 5.
 *
 * Left Column: Glowing album artwork card, song title, artist.
 * Right Column: High-impact karaoke lyrics with glowing sung syllables.
 * Bottom Bar: Apple progress bar, vocal sing mic, add (+), more (...), lyrics bubble, queue buttons.
 * Top-Right: Neon Stats for Nerds diagnostic HUD (when enabled).
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
    var isSingModeActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = 28.dp,
                bottom = 20.dp,
            ),
    ) {
        // Top-Right: Stats for Nerds Overlay Card (matching Image 4)
        AnimatedVisibility(
            visible = showNerdStats,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd),
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

        // Center Area: Left Art (38%) + Right Lyrics (62%)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.84f)
                .align(Alignment.TopStart),
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
                        .size(270.dp)
                        .shadow(32.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.75f))
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

        // Bottom Region: 1:1 Apple TV Transport & Action Bar (matching Image 5)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Vocal sing status hint if active
            if (isSingModeActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "Adjust vocal level using + and - volume buttons",
                        fontSize = 12.sp,
                        fontFamily = TvSFProDisplay,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }
            }

            // Horizontal Progress Bar + Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Progress Bar with Left Current Time & Right Remaining Time
                Box(modifier = Modifier.weight(1f)) {
                    TvPlayerProgress(
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onSeek = onSeek,
                    )
                }

                // Apple TV Control Pills: Vocal Sing, Favorite (+), More (...), Lyrics, Queue
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Apple Sing Vocal Mic Button
                    TvIconButton(
                        icon = Icons.Default.AutoAwesome,
                        contentDescription = "Apple Music Sing",
                        size = 40.dp,
                        iconSize = 20.dp,
                        isActive = isSingModeActive,
                        onClick = { isSingModeActive = !isSingModeActive },
                    )

                    // Add to Favorites (+) Button
                    TvIconButton(
                        icon = if (isLiked) Icons.Default.Favorite else Icons.Default.Add,
                        contentDescription = "Favorite",
                        size = 40.dp,
                        iconSize = 20.dp,
                        onClick = { onToggleLike?.invoke() },
                    )

                    // More Options (...) Button
                    TvIconButton(
                        icon = Icons.Default.MoreHoriz,
                        contentDescription = "More Options",
                        size = 40.dp,
                        iconSize = 20.dp,
                        onClick = {},
                    )

                    // Lyrics Bubble Toggle Button
                    TvIconButton(
                        icon = Icons.Default.ChatBubble,
                        contentDescription = "Toggle Lyrics",
                        size = 40.dp,
                        iconSize = 18.dp,
                        isPrimary = true,
                        onClick = onCloseLyrics,
                    )

                    // Queue List Button
                    TvIconButton(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        size = 40.dp,
                        iconSize = 20.dp,
                        onClick = {},
                    )
                }
            }
        }
    }
}
