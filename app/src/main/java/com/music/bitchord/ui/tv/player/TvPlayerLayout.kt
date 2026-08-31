package com.music.bitchord.ui.tv.player

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import com.music.bitchord.data.canvas.CanvasArtwork
import com.music.bitchord.data.canvas.CanvasRepository
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.tv.components.TvLyricsIcon
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.theme.LocalTvFontFamily
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay
import kotlinx.coroutines.delay

/**
 * 1:1 Apple Music TV Now Playing Screen with 3D Cover Flow, live video canvas, and crisp focus rings.
 */
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
    previousSong: Song? = null,
    nextSong: Song? = null,
    playingFromSource: String? = null,
    onBack: (() -> Unit)? = null,
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
    val currentFont = LocalTvFontFamily.current
    val autoplay by AppSettings.autoplay.collectAsState()
    val liveCanvasEnabled by AppSettings.animatedCanvas.collectAsState()

    var canvasArtwork by remember(song.videoId) { mutableStateOf<CanvasArtwork?>(null) }
    var showNoCanvasBanner by remember(song.videoId) { mutableStateOf(false) }

    // Live Video Canvas Resolver & Notification
    LaunchedEffect(song.videoId, liveCanvasEnabled) {
        if (!liveCanvasEnabled) {
            canvasArtwork = null
            showNoCanvasBanner = false
            return@LaunchedEffect
        }
        val resolved = CanvasRepository.canvasFor(song)
        canvasArtwork = resolved
        if (resolved == null) {
            showNoCanvasBanner = true
            delay(3500)
            showNoCanvasBanner = false
        } else {
            showNoCanvasBanner = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 48.dp,
                    end = 48.dp,
                    top = 26.dp,
                    bottom = 24.dp,
                ),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── TOP BAR (Back button, AirPlay device pill, Title, and 4 Action Buttons) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top Left: Back Button + Device Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (onBack != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .tvButtonFocus(
                                    shape = CircleShape,
                                    focusedScale = 1.18f,
                                    focusedBorderColor = Color.White,
                                    onClick = onBack,
                                )
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Airplay,
                            contentDescription = "AirPlay Device",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "BitChord TV",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = currentFont,
                            color = Color.White,
                        )
                    }
                }

                // Top Center: Album / Context Title
                Text(
                    text = song.albumName ?: playingFromSource ?: song.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = currentFont,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                // Top Right: 4 Frosted Action Buttons (Shuffle, Repeat, Autoplay, Lyrics)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TvTopActionButton(
                        icon = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        isActive = isShuffleActive,
                        onClick = onToggleShuffle,
                    )

                    val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    TvTopActionButton(
                        icon = repeatIcon,
                        contentDescription = "Repeat",
                        isActive = repeatMode != Player.REPEAT_MODE_OFF,
                        onClick = {},
                    )

                    TvTopActionButton(
                        icon = Icons.Default.AllInclusive,
                        contentDescription = "Autoplay Similar Music",
                        isActive = autoplay,
                        onClick = { AppSettings.setAutoplay(!autoplay) },
                    )

                    TvTopActionLyricsButton(
                        contentDescription = "Synchronized Lyrics",
                        isActive = isLyricsActive,
                        onClick = onToggleLyrics,
                    )
                }
            }

            // ── CENTER 3D COVER FLOW CAROUSEL ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Previous Track Card (Left)
                TvAdjacentTrackCard(
                    song = previousSong,
                    isPrevious = true,
                    onClick = onPrevious,
                    modifier = Modifier.weight(0.28f),
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Center Active Playing Track Card (Large, Sharp, Glowing)
                Column(
                    modifier = Modifier
                        .weight(0.44f)
                        .zIndex(10f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1.0f)
                            .shadow(32.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.85f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!song.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(song.thumbnailUrl)
                                    .size(720, 720)
                                    .precision(Precision.EXACT)
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = currentFont,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${song.artist} — ${song.albumName ?: "Single"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = currentFont,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .tvButtonFocus(
                                shape = CircleShape,
                                focusedScale = 1.15f,
                                focusedBorderColor = Color.White,
                                onClick = onToggleQueue,
                            )
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More Options",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Next Track Card (Right)
                TvAdjacentTrackCard(
                    song = nextSong,
                    isPrevious = false,
                    onClick = onNext,
                    modifier = Modifier.weight(0.28f),
                )
            }

            // ── BOTTOM PROGRESS LINE (PURE WHITE & SMOOTH GLIDE) ─────────────────
            TvPlayerProgress(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onSeek = onSeek,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // ── TOP NOTIFICATION PILL FOR LIVE CANVAS ────────────────────────────
        AnimatedVisibility(
            visible = showNoCanvasBanner,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "No live video canvas for this track • Playing standard artwork",
                    fontSize = 13.sp,
                    fontFamily = currentFont,
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Adjacent Previous/Next track card with subtle blur when unfocused and high-contrast visible border when hovered.
 */
@Composable
private fun TvAdjacentTrackCard(
    song: Song?,
    isPrevious: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentFont = LocalTvFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 0.98f else 0.85f,
        label = "adjacentScale",
    )

    val blurRadius = if (isFocused) 0.dp else 4.dp
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
        Modifier.blur(blurRadius)
    } else Modifier

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = song != null,
                onClick = onClick,
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(1.0f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isFocused) 3.dp else 1.dp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                )
                .background(Color.White.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            if (!song?.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song?.thumbnailUrl)
                        .size(512, 512)
                        .precision(Precision.EXACT)
                        .crossfade(true)
                        .build(),
                    contentDescription = song?.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = if (isPrevious) Icons.Default.SkipPrevious else Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.40f),
                    modifier = Modifier.size(44.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = song?.title ?: if (isPrevious) "Start of Queue" else "End of Queue",
            fontSize = 14.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = currentFont,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.70f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )

        if (song != null) {
            Text(
                text = song.artist,
                fontSize = 12.sp,
                fontFamily = currentFont,
                color = if (isFocused) Color.White.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.50f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TvTopActionButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isActive -> Color.White.copy(alpha = 0.45f)
            else -> Color.White.copy(alpha = 0.18f)
        },
        label = "topActionBg",
    )

    val iconTint by animateColorAsState(
        targetValue = if (isFocused) Color.Black else Color.White,
        label = "topActionTint",
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .tvButtonFocus(
                shape = CircleShape,
                focusedScale = 1.15f,
                focusedBorderColor = Color.White,
                onClick = onClick,
            )
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun TvTopActionLyricsButton(
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isActive -> Color.White.copy(alpha = 0.45f)
            else -> Color.White.copy(alpha = 0.18f)
        },
        label = "topActionLyricsBg",
    )

    val iconTint by animateColorAsState(
        targetValue = if (isFocused) Color.Black else Color.White,
        label = "topActionLyricsTint",
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .tvButtonFocus(
                shape = CircleShape,
                focusedScale = 1.15f,
                focusedBorderColor = Color.White,
                onClick = onClick,
            )
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        TvLyricsIcon(
            modifier = Modifier.size(18.dp),
            tint = iconTint,
        )
    }
}
