package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.components.TvEmptyState
import com.music.bitchord.ui.tv.components.TvErrorState
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.theme.AppleSpringPreset
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvSFProDisplay
import com.music.bitchord.ui.tv.theme.appleSpring
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 1:1 Apple Music TV Lyrics with continuous smooth swept line flow,
 * inspired by BitChord's mobile player implementation.
 */
@Composable
fun TvLyricsList(
    lyrics: List<LyricLine>?,
    currentPositionMs: Long,
    isPlaying: Boolean = true,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onSeekToTimestamp: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isManualScroll by remember { mutableStateOf(false) }

    // Frame-accurate clock for smooth continuous sweep across words/characters
    val clock = rememberTvLyricClock(currentPositionMs, isPlaying)

    val activeIndex by remember(lyrics) {
        derivedStateOf {
            if (lyrics == null) -1
            else lyrics.indexOfLast { it.timeMs <= clock.longValue }
        }
    }

    // Auto-scroll to keep active line 1/3 down the screen with smooth physics
    var isFirstPlaced by remember(lyrics) { mutableStateOf(false) }
    LaunchedEffect(activeIndex, isManualScroll) {
        if (!isManualScroll && lyrics != null && activeIndex in lyrics.indices) {
            val viewport = snapshotFlow { listState.layoutInfo.viewportSize.height }
                .first { it > 0 }
            val third = viewport / 3
            if (isFirstPlaced) {
                listState.animateScrollToItem(activeIndex, scrollOffset = -third)
            } else {
                listState.scrollToItem(activeIndex, scrollOffset = -third)
                isFirstPlaced = true
            }
        }
    }

    // Automatically resume auto-follow 4 seconds after D-pad browsing
    LaunchedEffect(isManualScroll) {
        if (isManualScroll) {
            delay(4000)
            isManualScroll = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onTvKeyEvent(
                onUp = {
                    isManualScroll = true
                    scope.launch {
                        val current = listState.firstVisibleItemIndex
                        listState.animateScrollToItem((current - 1).coerceAtLeast(0))
                    }
                    true
                },
                onDown = {
                    isManualScroll = true
                    scope.launch {
                        val current = listState.firstVisibleItemIndex
                        val max = (lyrics?.size ?: 1) - 1
                        listState.animateScrollToItem((current + 1).coerceAtMost(max))
                    }
                    true
                },
            ),
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TvColors.AccentRed)
                }
            }
            error != null -> {
                TvErrorState(
                    message = error,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            lyrics.isNullOrEmpty() -> {
                TvEmptyState(
                    title = "Lyrics aren't available for this track",
                    message = "We couldn't find synchronized lyrics for this song.",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 100.dp, bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    itemsIndexed(
                        items = lyrics,
                        key = { index, line -> "${line.timeMs}_$index" },
                    ) { index, line ->
                        val isActive = index == activeIndex
                        val isPast = index < activeIndex

                        TvSweptLyricItem(
                            line = line,
                            clock = clock,
                            isActive = isActive,
                            isPast = isPast,
                            onClick = {
                                onSeekToTimestamp(line.timeMs)
                                isManualScroll = false
                            },
                        )
                    }
                }

                // Return to current line floating action button
                AnimatedVisibility(
                    visible = isManualScroll,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp, end = 24.dp),
                ) {
                    TvButton(
                        text = "Return to current line",
                        icon = Icons.Default.KeyboardArrowDown,
                        isPrimary = true,
                        onClick = {
                            isManualScroll = false
                        },
                    )
                }
            }
        }
    }
}

/**
 * Continuous frame clock running on VSYNC for smooth lyrics sweep transitions.
 */
@Composable
private fun rememberTvLyricClock(positionMs: Long, isPlaying: Boolean): MutableLongState {
    val clock = remember { mutableLongStateOf(positionMs) }
    LaunchedEffect(positionMs, isPlaying) {
        clock.longValue = positionMs
        if (!isPlaying) return@LaunchedEffect
        var previousFrame = withFrameMillis { it }
        while (true) {
            withFrameMillis { frame ->
                clock.longValue += frame - previousFrame
                previousFrame = frame
            }
        }
    }
    return clock
}

/**
 * Individual lyric row that smoothly sweeps light across the words/letters in real time.
 */
@Composable
private fun TvSweptLyricItem(
    line: LyricLine,
    clock: MutableLongState,
    isActive: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.04f else 1.0f,
        animationSpec = appleSpring(AppleSpringPreset.Gentle),
        label = "lyricScale",
    )

    val fontSize = if (isActive) 36.sp else 28.sp
    val fontWeight = if (isActive) FontWeight.W800 else FontWeight.W600

    var layout by remember(line) { mutableStateOf<TextLayoutResult?>(null) }

    val style = TextStyle(
        fontFamily = TvSFProDisplay,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = (fontSize.value * 1.38f).sp,
    )

    val dimAlpha = when {
        isActive -> 0.40f
        isFocused -> 0.85f
        isPast -> 0.30f
        else -> 0.45f
    }

    // Continuous smooth sweep clipping logic (identical to mobile SweptLyricLine)
    val sweepModifier = Modifier.drawWithContent {
        val pos = clock.longValue
        when {
            pos >= line.endMs -> drawContent()
            pos <= line.timeMs -> Unit
            else -> {
                val measured = layout
                if (measured != null) {
                    val revealedChars = if (line.words.isNotEmpty()) {
                        line.revealedChars(pos)
                    } else {
                        val duration = (line.endMs - line.timeMs).coerceAtLeast(1000L)
                        val fraction = (pos - line.timeMs).toFloat() / duration.toFloat()
                        fraction.coerceIn(0f, 1f) * line.text.length
                    }
                    tvSweepTo(measured, revealedChars)
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.02f,
                focusedBorderColor = Color.White.copy(alpha = 0.7f),
                unfocusedBorderColor = Color.Transparent,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (line.isGap) {
            Text(
                text = "♪  ♪  ♪",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.35f),
                fontFamily = TvSFProDisplay,
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Base Dimmed Layer (handles line wrapping and layout measurement)
                Text(
                    text = line.text,
                    style = style,
                    color = Color.White.copy(alpha = dimAlpha),
                    onTextLayout = { layout = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Top Lit Layer (revealed smoothly character by character via sweepTo)
                if (isActive) {
                    Text(
                        text = line.text,
                        style = style,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(sweepModifier),
                    )
                }
            }
        }
    }
}

/**
 * Clips text horizontally to smoothly reveal up to [revealedChars] across any number of wrapped lines.
 */
private fun ContentDrawScope.tvSweepTo(layout: TextLayoutResult, revealedChars: Float) {
    if (revealedChars <= 0f) return
    val totalLength = layout.layoutInput.text.length
    if (revealedChars >= totalLength) {
        drawContent()
        return
    }

    for (visualLine in 0 until layout.lineCount) {
        val start = layout.getLineStart(visualLine)
        if (revealedChars <= start) return

        val end = layout.getLineEnd(visualLine, visibleEnd = true)
        val right = if (revealedChars >= end) {
            layout.getLineRight(visualLine)
        } else {
            val charIndex = revealedChars.toInt().coerceIn(start, end)
            val here = layout.getHorizontalPosition(charIndex, usePrimaryDirection = true)
            val next = layout.getHorizontalPosition((charIndex + 1).coerceAtMost(end), usePrimaryDirection = true)
            here + (next - here) * (revealedChars - charIndex)
        }

        clipRect(
            left = layout.getLineLeft(visualLine),
            top = layout.getLineTop(visualLine),
            right = right,
            bottom = layout.getLineBottom(visualLine),
        ) {
            this@tvSweepTo.drawContent()
        }
    }
}
