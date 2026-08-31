package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.ui.tv.components.TvEmptyState
import com.music.bitchord.ui.tv.components.TvErrorState
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

/**
 * Ultra-performance Apple Music 6-line TV Lyrics viewport.
 *
 * Window layout:
 * - 2 Unfocused Lyrics (Top)
 * - 1 Active Focused Lyric (Middle - Large, Bold White, Glowing)
 * - 3 Unfocused Lyrics (Bottom)
 *
 * Zero-lag rendering: replaces heavy list recycling with a fixed, GPU-accelerated 6-slot column.
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

    val activeIndex by remember(lyrics, currentPositionMs) {
        derivedStateOf {
            lyrics.indexOfLast { it.timeMs <= currentPositionMs }.coerceAtLeast(0)
        }
    }

    // Fixed 6-Line Window
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
        // Line -2 (Top Unfocused 1)
        val lineMinus2 = lyrics.getOrNull(activeIndex - 2)
        TvLyricLineSlot(
            line = lineMinus2,
            alpha = 0.20f,
            fontSize = 20,
            fontWeight = FontWeight.Medium,
            onClick = { lineMinus2?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line -1 (Top Unfocused 2)
        val lineMinus1 = lyrics.getOrNull(activeIndex - 1)
        TvLyricLineSlot(
            line = lineMinus1,
            alpha = 0.42f,
            fontSize = 24,
            fontWeight = FontWeight.Medium,
            onClick = { lineMinus1?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line 0 (MIDDLE ACTIVE FOCUSED LINE)
        val activeLine = lyrics.getOrNull(activeIndex)
        TvLyricLineSlot(
            line = activeLine,
            alpha = 1.0f,
            fontSize = 36,
            fontWeight = FontWeight.W800,
            isActive = true,
            onClick = { activeLine?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line +1 (Bottom Unfocused 1)
        val linePlus1 = lyrics.getOrNull(activeIndex + 1)
        TvLyricLineSlot(
            line = linePlus1,
            alpha = 0.45f,
            fontSize = 24,
            fontWeight = FontWeight.Medium,
            onClick = { linePlus1?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line +2 (Bottom Unfocused 2)
        val linePlus2 = lyrics.getOrNull(activeIndex + 2)
        TvLyricLineSlot(
            line = linePlus2,
            alpha = 0.28f,
            fontSize = 20,
            fontWeight = FontWeight.Medium,
            onClick = { linePlus2?.let { onSeekToTimestamp(it.timeMs) } },
        )

        // Line +3 (Bottom Unfocused 3)
        val linePlus3 = lyrics.getOrNull(activeIndex + 3)
        TvLyricLineSlot(
            line = linePlus3,
            alpha = 0.16f,
            fontSize = 17,
            fontWeight = FontWeight.Medium,
            onClick = { linePlus3?.let { onSeekToTimestamp(it.timeMs) } },
        )
    }
}

@Composable
private fun TvLyricLineSlot(
    line: LyricLine?,
    alpha: Float,
    fontSize: Int,
    fontWeight: FontWeight,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val text = when {
        line == null -> ""
        line.isGap -> "♪  ♪  ♪"
        else -> line.text
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.32f).sp,
                fontWeight = fontWeight,
                fontFamily = TvSFProDisplay,
                color = if (isActive) Color.White else Color.White.copy(alpha = if (isFocused) 0.9f else alpha),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (isActive) {
                            scaleX = 1.02f
                            scaleY = 1.02f
                        }
                    },
            )
        }
    }
}
