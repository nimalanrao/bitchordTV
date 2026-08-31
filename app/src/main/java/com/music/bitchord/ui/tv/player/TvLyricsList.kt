package com.music.bitchord.ui.tv.player

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.music.bitchord.ui.tv.theme.AppleSpringPreset
import com.music.bitchord.ui.tv.theme.LocalTvFontFamily
import com.music.bitchord.ui.tv.theme.TvSFProDisplay
import com.music.bitchord.ui.tv.theme.appleSpring

/**
 * 1:1 Apple Music TV Lyrics Viewport with Apple-physics smooth gliding, background vocals, and progressive syllable sweep.
 *
 * - 2 Unfocused Top Lines (~40% blur/dim)
 * - 1 Focused Singing Line in Middle (Crystal sharp, bold, glowing, progressive flow sweep)
 * - 3 Unfocused Bottom Lines (~20% blur/dim)
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

    // Apple-physics smooth vertical glide animation between rows
    val animatedIndexFloat by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.86f,
            stiffness = 110f, // Apple gentle physics (slow & smooth, not sluggish or jarring)
        ),
        label = "lyricsAppleGlide",
    )

    val currentFont = LocalTvFontFamily.current

    // 6-Line Structured Viewport
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
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
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start,
    ) {
        // Line -2: Top Unfocused 1 (40% blur/dim)
        val lineMinus2 = lyrics.getOrNull(activeIndex - 2)
        TvBlurLyricLineSlot(
            line = lineMinus2,
            alpha = 0.35f,
            blurRadius = 3.dp,
            fontSize = 22,
            fontWeight = FontWeight.Medium,
            onClick = { lineMinus2?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line -1: Top Unfocused 2 (40% blur/dim)
        val lineMinus1 = lyrics.getOrNull(activeIndex - 1)
        TvBlurLyricLineSlot(
            line = lineMinus1,
            alpha = 0.45f,
            blurRadius = 2.dp,
            fontSize = 24,
            fontWeight = FontWeight.Medium,
            onClick = { lineMinus1?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line 0: ACTIVE FOCUSED LINE (Crystal sharp, 0% blur, bold white, glowing progressive sweep & background vocals)
        val activeLine = lyrics.getOrNull(activeIndex)
        TvActiveSweptLyricLine(
            line = activeLine,
            clock = clock,
            onClick = { activeLine?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line +1: Bottom Unfocused 1 (20% blur/dim)
        val linePlus1 = lyrics.getOrNull(activeIndex + 1)
        TvBlurLyricLineSlot(
            line = linePlus1,
            alpha = 0.50f,
            blurRadius = 1.5.dp,
            fontSize = 24,
            fontWeight = FontWeight.Medium,
            onClick = { linePlus1?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line +2: Bottom Unfocused 2 (20% blur/dim)
        val linePlus2 = lyrics.getOrNull(activeIndex + 2)
        TvBlurLyricLineSlot(
            line = linePlus2,
            alpha = 0.35f,
            blurRadius = 2.dp,
            fontSize = 22,
            fontWeight = FontWeight.Medium,
            onClick = { linePlus2?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line +3: Bottom Unfocused 3 (20% blur/dim)
        val linePlus3 = lyrics.getOrNull(activeIndex + 3)
        TvBlurLyricLineSlot(
            line = linePlus3,
            alpha = 0.22f,
            blurRadius = 3.dp,
            fontSize = 19,
            fontWeight = FontWeight.Medium,
            onClick = { linePlus3?.let { onSeekToTimestamp(it.timeMs) } },
        )
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
 * Unfocused blurred lyric line slot with secondary background vocal support.
 */
@Composable
private fun TvBlurLyricLineSlot(
    line: LyricLine?,
    alpha: Float,
    blurRadius: Dp,
    fontSize: Int,
    fontWeight: FontWeight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentFont = LocalTvFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val text = when {
        line == null -> ""
        line.isGap -> "♪  ♪  ♪"
        else -> line.text
    }

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
        Modifier.blur(blurRadius)
    } else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp)
            .then(blurModifier),
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.32f).sp,
                fontWeight = fontWeight,
                fontFamily = currentFont,
                color = Color.White.copy(alpha = if (isFocused) 0.95f else alpha),
                modifier = Modifier.fillMaxWidth(),
            )

            // Secondary background vocal (if present)
            if (line?.background != null && line.background.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = line.background.text,
                    fontSize = (fontSize - 5).sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = currentFont,
                    color = Color.White.copy(alpha = if (isFocused) 0.85f else alpha * 0.85f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Active singing line with crystal sharp focus, smooth progressive syllable sweep, and background vocal rendering.
 */
@Composable
private fun TvActiveSweptLyricLine(
    line: LyricLine?,
    clock: MutableLongState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (line == null) return

    val currentFont = LocalTvFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = 1.03f,
        animationSpec = appleSpring(AppleSpringPreset.Gentle),
        label = "activeLyricScale",
    )

    val style = TextStyle(
        fontFamily = currentFont,
        fontSize = 34.sp,
        fontWeight = FontWeight.W800,
        lineHeight = 44.sp,
    )

    var layout by remember(line) { mutableStateOf<TextLayoutResult?>(null) }

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
                    tvProgressiveSweep(measured, revealedChars)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
    ) {
        if (line.isGap) {
            Text(
                text = "♪  ♪  ♪",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = currentFont,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                // Dim base text
                Text(
                    text = line.text,
                    style = style,
                    color = Color.White.copy(alpha = 0.38f),
                    onTextLayout = { layout = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Bright progressive white text revealed smoothly across syllables
                Text(
                    text = line.text,
                    style = style,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(sweepModifier),
                )
            }

            // Secondary background vocalist text (small & italic beneath main line)
            if (line.background != null && line.background.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = line.background.text,
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontFamily = currentFont,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
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
