package com.music.bitchord.ui.tv.keyboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.music.bitchord.ui.tv.personalization.NicknamePolicy
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

enum class KeyboardPage {
    LETTERS,
    SYMBOLS,
    EMOJI
}

/**
 * Super smooth animated TV remote keyboard with spring hover scale, dynamic halo focus, and tactile response.
 */
@Composable
fun TvKeyboard(
    text: String,
    cursorIndex: Int,
    onTextChange: (String, Int) -> Unit,
    onDone: () -> Unit,
    onOpenSystemIme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentPage by remember { mutableStateOf(KeyboardPage.LETTERS) }
    var isShiftActive by remember { mutableStateOf(false) }
    var isCapsLockActive by remember { mutableStateOf(false) }

    val isUppercase = isShiftActive || isCapsLockActive

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TvColors.SurfaceVariant)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Crossfade(
            targetState = currentPage,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            label = "keyboardPageCrossfade",
        ) { page ->
            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (page) {
                    KeyboardPage.LETTERS -> {
                        // Row 1: Numbers (1 - 0)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { char ->
                                TvKeyButton(
                                    label = char,
                                    onClick = {
                                        insertText(text, cursorIndex, char, onTextChange)
                                    },
                                )
                            }
                        }

                        // Row 2: Q - P
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").forEach { char ->
                                val displayChar = if (isUppercase) char.uppercase() else char
                                TvKeyButton(
                                    label = displayChar,
                                    onClick = {
                                        insertText(text, cursorIndex, displayChar, onTextChange)
                                        if (isShiftActive && !isCapsLockActive) isShiftActive = false
                                    },
                                )
                            }
                        }

                        // Row 3: A - L
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").forEach { char ->
                                val displayChar = if (isUppercase) char.uppercase() else char
                                TvKeyButton(
                                    label = displayChar,
                                    onClick = {
                                        insertText(text, cursorIndex, displayChar, onTextChange)
                                        if (isShiftActive && !isCapsLockActive) isShiftActive = false
                                    },
                                )
                            }
                        }

                        // Row 4: Shift, Z - M, Backspace
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TvKeyButton(
                                icon = Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isCapsLockActive) "Caps Lock Active" else "Shift",
                                width = 52.dp,
                                isSelected = isUppercase,
                                onClick = {
                                    if (isShiftActive) {
                                        isCapsLockActive = !isCapsLockActive
                                        isShiftActive = false
                                    } else {
                                        isShiftActive = true
                                    }
                                },
                            )

                            listOf("z", "x", "c", "v", "b", "n", "m").forEach { char ->
                                val displayChar = if (isUppercase) char.uppercase() else char
                                TvKeyButton(
                                    label = displayChar,
                                    onClick = {
                                        insertText(text, cursorIndex, displayChar, onTextChange)
                                        if (isShiftActive && !isCapsLockActive) isShiftActive = false
                                    },
                                )
                            }

                            TvKeyButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                width = 52.dp,
                                onClick = {
                                    val (newText, newCursor) = NicknamePolicy.deletePreviousGrapheme(text, cursorIndex)
                                    onTextChange(newText, newCursor)
                                },
                            )
                        }
                    }

                    KeyboardPage.SYMBOLS -> {
                        val symbolRows = listOf(
                            listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")"),
                            listOf("-", "_", "=", "+", "[", "]", "{", "}", "\\", "|"),
                            listOf(";", ":", "'", "\"", ",", ".", "<", ">", "/", "?"),
                        )
                        symbolRows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { sym ->
                                    TvKeyButton(
                                        label = sym,
                                        onClick = {
                                            insertText(text, cursorIndex, sym, onTextChange)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    KeyboardPage.EMOJI -> {
                        val emojiRows = listOf(
                            listOf("✨", "🎵", "🎧", "⚡", "🔥", "💫", "🌟", "💜", "❤️", "🚀"),
                            listOf("👑", "🎮", "👾", "🌙", "🌊", "🌸", "🍀", "💎", "🎯", "🎪"),
                            listOf("🦊", "🐱", "🐼", "🦁", "🐉", "🦋", "🍄", "☕", "🍿", "🎉"),
                        )
                        emojiRows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { emoji ->
                                    TvKeyButton(
                                        label = emoji,
                                        onClick = {
                                            insertText(text, cursorIndex, emoji, onTextChange)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar: Page Toggles, Space, Cursor Movement, Clear, Done
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Page Switcher (ABC / 123)
            TvKeyButton(
                label = if (currentPage == KeyboardPage.LETTERS) "?123" else "ABC",
                width = 62.dp,
                onClick = {
                    currentPage = if (currentPage == KeyboardPage.LETTERS) KeyboardPage.SYMBOLS else KeyboardPage.LETTERS
                },
            )

            // Emoji Toggle
            TvKeyButton(
                icon = Icons.Default.EmojiEmotions,
                contentDescription = "Emoji Page",
                width = 44.dp,
                isSelected = currentPage == KeyboardPage.EMOJI,
                onClick = {
                    currentPage = if (currentPage == KeyboardPage.EMOJI) KeyboardPage.LETTERS else KeyboardPage.EMOJI
                },
            )

            // Cursor Left
            TvKeyButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cursor Left",
                width = 38.dp,
                onClick = {
                    val newCursor = NicknamePolicy.moveCursorLeft(text, cursorIndex)
                    onTextChange(text, newCursor)
                },
            )

            // Spacebar
            TvKeyButton(
                icon = Icons.Default.SpaceBar,
                contentDescription = "Space",
                width = 130.dp,
                onClick = {
                    insertText(text, cursorIndex, " ", onTextChange)
                },
            )

            // Cursor Right
            TvKeyButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Cursor Right",
                width = 38.dp,
                onClick = {
                    val newCursor = NicknamePolicy.moveCursorRight(text, cursorIndex)
                    onTextChange(text, newCursor)
                },
            )

            // Clear
            TvKeyButton(
                icon = Icons.Default.Clear,
                contentDescription = "Clear All",
                width = 38.dp,
                onClick = {
                    onTextChange("", 0)
                },
            )

            // System IME Button
            TvKeyButton(
                icon = Icons.Default.Keyboard,
                contentDescription = "System Keyboard / Voice",
                width = 38.dp,
                onClick = onOpenSystemIme,
            )

            // Done Button
            TvKeyButton(
                icon = Icons.Default.Check,
                label = "Done",
                width = 72.dp,
                isPrimary = true,
                onClick = onDone,
            )
        }
    }
}

private fun insertText(
    currentText: String,
    cursorIndex: Int,
    insert: String,
    onTextChange: (String, Int) -> Unit,
) {
    val before = currentText.substring(0, cursorIndex.coerceIn(0, currentText.length))
    val after = currentText.substring(cursorIndex.coerceIn(0, currentText.length))
    val candidate = before + insert + after
    if (NicknamePolicy.getGraphemeCount(candidate) <= NicknamePolicy.MAX_GRAPHEMES) {
        onTextChange(candidate, before.length + insert.length)
    }
}

/**
 * Super smooth TV keyboard key button with physics-based spring hover scale,
 * glowing focus halo, and elevation z-index elevation.
 */
@Composable
private fun TvKeyButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    width: Dp = 40.dp,
    height: Dp = 40.dp,
    isSelected: Boolean = false,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 1. Spring-based physics scale animation
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "tvKeyScale",
    )

    // 2. Smooth background color transition
    val targetBgColor = when {
        isFocused -> if (isPrimary) TvColors.AccentRed else TvColors.SurfaceFocused
        isSelected -> TvColors.SurfaceSelected
        isPrimary -> TvColors.AccentRed.copy(alpha = 0.85f)
        else -> TvColors.Surface
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "tvKeyBgColor",
    )

    // 3. Smooth focus border / glow transition
    val targetBorderColor = when {
        isFocused -> if (isPrimary) Color.White else TvColors.BorderFocused
        isSelected -> TvColors.AccentRed
        else -> Color(0x22FFFFFF)
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "tvKeyBorderColor",
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 120),
        label = "tvKeyBorderWidth",
    )

    val shape = RoundedCornerShape(9.dp)

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .zIndex(if (isFocused) 10f else 1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                if (isFocused) {
                    drawCircle(
                        color = if (isPrimary) TvColors.AccentRedGlow else Color(0x33FA2D48),
                        radius = size.maxDimension * 0.75f,
                    )
                }
            }
            .border(
                border = BorderStroke(width = borderWidth, color = animatedBorderColor),
                shape = shape,
            )
            .clip(shape)
            .background(animatedBgColor)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null && label != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TvSFProDisplay,
                )
            }
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        } else if (label != null) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = TvSFProDisplay,
            )
        }
    }
}
