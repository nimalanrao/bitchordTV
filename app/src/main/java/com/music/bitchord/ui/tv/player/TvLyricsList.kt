package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

/**
 * 1:1 Apple Music TV Lyrics List with smooth spring-physics auto-scroll
 * and active line scaling.
 */
@Composable
fun TvLyricsList(
    lyrics: List<LyricLine>?,
    currentPositionMs: Long,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onSeekToTimestamp: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isManualScroll by remember { mutableStateOf(false) }

    val syncState = remember(lyrics, currentPositionMs) {
        LyricsSynchronizer.synchronize(lyrics, currentPositionMs)
    }

    // Auto-scroll when in auto-follow mode with smooth spring spec
    LaunchedEffect(syncState.activeLineIndex, isManualScroll) {
        if (!isManualScroll && lyrics != null && syncState.activeLineIndex in lyrics.indices) {
            val targetIndex = (syncState.activeLineIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
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
                        val isActive = index == syncState.activeLineIndex
                        val isPast = index < syncState.activeLineIndex

                        TvLyricLineItem(
                            line = line,
                            isActive = isActive,
                            isPast = isPast,
                            syncState = if (isActive) syncState else null,
                            onClick = {
                                onSeekToTimestamp(line.timeMs)
                                isManualScroll = false
                            },
                        )
                    }
                }

                // Return to current line floating action
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

@Composable
private fun TvLyricLineItem(
    line: LyricLine,
    isActive: Boolean,
    isPast: Boolean,
    syncState: LyricsSyncState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColor by animateColorAsState(
        targetValue = when {
            isActive -> Color.White
            isFocused -> Color.White
            isPast -> Color.White.copy(alpha = 0.40f)
            else -> Color.White.copy(alpha = 0.55f)
        },
        animationSpec = appleSpring(AppleSpringPreset.Gentle),
        label = "lyricTextColor",
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.04f else 1.0f,
        animationSpec = appleSpring(AppleSpringPreset.Gentle),
        label = "lyricScale",
    )

    val fontSize = if (isActive) 36.sp else 28.sp
    val fontWeight = if (isActive) FontWeight.W800 else FontWeight.W600

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.02f,
                focusedBorderColor = TvColors.BorderFocused,
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
        } else if (line.words.isNotEmpty() && isActive && syncState != null) {
            // Word-level karaoke highlighting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                line.words.forEachIndexed { wordIdx, word ->
                    val isWordActive = wordIdx == syncState.activeWordIndex
                    val isWordSung = wordIdx < syncState.activeWordIndex

                    Text(
                        text = "${word.text} ",
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontFamily = TvSFProDisplay,
                        color = when {
                            isWordActive -> Color.White
                            isWordSung -> Color.White
                            else -> Color.White.copy(alpha = 0.55f)
                        },
                        lineHeight = (fontSize.value * 1.3f).sp,
                    )
                }
            }
        } else {
            Text(
                text = line.text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontFamily = TvSFProDisplay,
                color = textColor,
                lineHeight = (fontSize.value * 1.3f).sp,
            )
        }
    }
}
