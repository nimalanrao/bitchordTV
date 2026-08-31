package com.music.bitchord.ui.tv.player

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.ui.tv.components.TvEmptyState
import com.music.bitchord.ui.tv.components.TvErrorState
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.theme.LocalTvFontFamily
import kotlin.math.abs

/**
 * 1:1 Apple Music TV Lyrics Viewport with ultra-smooth spring-animated continuous scrolling.
 *
 * - Active singing line is vertically centered and highlighted in bold white with progressive syllable sweep.
 * - Smoothly glides to the next lyrics line using Apple spring physics (never instant/jarring).
 * - Upper lines smoothly blur & dim (~40% blur/dim).
 * - Lower lines smoothly blur & dim (~20% blur/dim).
 * - Background vocals rendered in smaller italicized text directly beneath the lead vocal line.
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
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    if (error != null) {
        TvErrorState(
            message = "No lyrics found for this music",
            onRetry = onRetry,
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    if (lyrics.isNullOrEmpty()) {
        TvEmptyState(
            title = "No lyrics found for this music",
            message = "We couldn't find synchronized lyrics for this song.",
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    // Microsecond VSYNC clock for real-time progressive syllable sweep
    val clock = rememberTvLyricClock(currentPositionMs, isPlaying)

    val activeIndex by remember(lyrics, currentPositionMs) {
        derivedStateOf {
            val idx = lyrics.indexOfLast { it.timeMs <= clock.longValue }
            idx.coerceAtLeast(0)
        }
    }

    // Animated float index for butter-smooth continuous interpolation between lines
    val animatedActiveIndex by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 100f, // Apple gentle glide physics (slow, cinematic, fluid)
        ),
        label = "lyricsAppleFloatGlide",
    )

    val listState = rememberLazyListState()

    // Smoothly scroll the list to center the active line
    LaunchedEffect(activeIndex) {
        if (lyrics.isNotEmpty() && activeIndex in lyrics.indices) {
            val targetScrollIndex = (activeIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(
                index = targetScrollIndex,
                scrollOffset = -40,
            )
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = 80.dp, bottom = 160.dp, start = 16.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onTvKeyEvent(
                onUp = {
                    val prev = (activeIndex - 1).coerceAtLeast(0)
                    onSeekToTimestamp(lyrics[prev].timeMs)
                    true
                },
                onDown = {
                    val next = (activeIndex + 1).coerceAtMost(lyrics.lastIndex)
                    onSeekToTimestamp(lyrics[next].timeMs)
                    true
                },
            ),
    ) {
        itemsIndexed(lyrics, key = { index, line -> "${line.timeMs}_$index" }) { index, line ->
            val distance = index - animatedActiveIndex
            val absDist = abs(distance)
            val isCurrentActive = index == activeIndex

            TvSmoothLyricLineItem(
                line = line,
                index = index,
                distance = distance,
                absDist = absDist,
                isCurrentActive = isCurrentActive,
                clock = clock,
                onClick = { onSeekToTimestamp(line.timeMs) },
            )
        }
    }
}

@Composable
private fun TvSmoothLyricLineItem(
    line: LyricLine,
    index: Int,
    distance: Float,
    absDist: Float,
    isCurrentActive: Boolean,
    clock: MutableLongState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentFont = LocalTvFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Dynamic Apple-physics styling based on distance to active line
    val targetScale = when {
        absDist < 0.5f -> 1.04f
        absDist < 1.5f -> 0.98f
        else -> 0.94f
    }
    val animatedScale by animateFloatAsState(targetScale, spring(0.85f, 120f), label = "lyricScale")

    val targetAlpha = when {
        absDist < 0.5f -> 1.0f
        distance < 0 -> 0.35f // 40% blur/dim for past lines
        else -> 0.45f // 20% blur/dim for upcoming lines
    }
    val animatedAlpha by animateFloatAsState(
        if (isFocused) 1.0f else targetAlpha,
        spring(0.85f, 120f),
        label = "lyricAlpha",
    )

    val targetBlurDp: Dp = when {
        absDist < 0.5f -> 0.dp
        distance < 0 -> 2.5.dp // Upper unfocused blur
        else -> 1.5.dp // Lower unfocused blur
    }
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetBlurDp > 0.dp && !isFocused) {
        Modifier.blur(targetBlurDp)
    } else Modifier

    val style = TextStyle(
        fontFamily = currentFont,
        fontSize = if (absDist < 0.5f) 32.sp else 24.sp,
        fontWeight = if (absDist < 0.5f) FontWeight.W800 else FontWeight.SemiBold,
        lineHeight = if (absDist < 0.5f) 42.sp else 32.sp,
    )

    var layout by remember(line) { mutableStateOf<TextLayoutResult?>(null) }

    val sweepModifier = if (isCurrentActive) {
        Modifier.drawWithContent {
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
                        tvProgressiveSweep(measured, revealedChars)
                    }
                }
            }
        }
    } else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp)
            .then(blurModifier),
    ) {
        if (line.isGap) {
            Text(
                text = "♪  ♪  ♪",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = currentFont,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                // Dim background text
                Text(
                    text = line.text,
                    style = style,
                    color = Color.White.copy(alpha = if (isCurrentActive) 0.35f else 1.0f),
                    onTextLayout = { layout = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Progressive glowing swept text on active line
                if (isCurrentActive) {
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

            // Secondary background vocalist text (small & italic beneath main line)
            if (line.background != null && line.background.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = line.background.text,
                    fontSize = if (isCurrentActive) 18.sp else 15.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontFamily = currentFont,
                    color = Color.White.copy(alpha = if (isCurrentActive) 0.70f else 0.50f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * VSYNC Frame Clock for progressive real-time sweeping.
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
                clock.longValue += (frame - previousFrame)
                previousFrame = frame
            }
        }
    }
    return clock
}

/**
 * Clips text horizontally to smoothly reveal up to [revealedChars] across any number of wrapped lines.
 */
private fun ContentDrawScope.tvProgressiveSweep(layout: TextLayoutResult, revealedChars: Float) {
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
            this@tvProgressiveSweep.drawContent()
        }
    }
}
