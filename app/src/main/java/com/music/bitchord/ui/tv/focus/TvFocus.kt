package com.music.bitchord.ui.tv.focus

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.music.bitchord.ui.tv.theme.TvColors

/**
 * Standard TV card focus modifier.
 * Applies a smooth scale animation (1.05x), rounded border highlight, and elevation
 * when the element receives D-pad focus.
 */
fun Modifier.tvCardFocus(
    shape: Shape = RoundedCornerShape(14.dp),
    focusedScale: Float = 1.05f,
    borderWidth: Dp = 2.5.dp,
    focusedBorderColor: Color = TvColors.BorderFocused,
    unfocusedBorderColor: Color = Color.Transparent,
    elevation: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tvCardFocusScale",
    )

    this
        .scale(scale)
        .zIndex(if (isFocused) 10f else 1f)
        .then(
            if (isFocused) {
                Modifier.shadow(elevation, shape, clip = false)
            } else Modifier
        )
        .border(
            border = BorderStroke(
                width = borderWidth,
                color = if (isFocused) focusedBorderColor else unfocusedBorderColor,
            ),
            shape = shape,
        )
        .clip(shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else Modifier
        )
}

/**
 * Standard TV button focus modifier.
 */
fun Modifier.tvButtonFocus(
    shape: Shape = RoundedCornerShape(10.dp),
    focusedScale: Float = 1.04f,
    focusedBorderColor: Color = TvColors.BorderFocused,
    unfocusedBorderColor: Color = TvColors.BorderSubtle,
    onClick: (() -> Unit)? = null,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "tvButtonFocusScale",
    )

    this
        .scale(scale)
        .zIndex(if (isFocused) 5f else 1f)
        .border(
            border = BorderStroke(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) focusedBorderColor else unfocusedBorderColor,
            ),
            shape = shape,
        )
        .clip(shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else Modifier
        )
}

/**
 * Intercepts TV remote key events (such as media keys, D-pad center, Left/Right seeking, Back).
 */
fun Modifier.onTvKeyEvent(
    onSelect: (() -> Boolean)? = null,
    onLeft: (() -> Boolean)? = null,
    onRight: (() -> Boolean)? = null,
    onUp: (() -> Boolean)? = null,
    onDown: (() -> Boolean)? = null,
    onPlayPause: (() -> Boolean)? = null,
    onBack: (() -> Boolean)? = null,
): Modifier = onKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false

    when (event.nativeKeyEvent.keyCode) {
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
            onSelect?.invoke() ?: false
        }
        KeyEvent.KEYCODE_DPAD_LEFT -> {
            onLeft?.invoke() ?: false
        }
        KeyEvent.KEYCODE_DPAD_RIGHT -> {
            onRight?.invoke() ?: false
        }
        KeyEvent.KEYCODE_DPAD_UP -> {
            onUp?.invoke() ?: false
        }
        KeyEvent.KEYCODE_DPAD_DOWN -> {
            onDown?.invoke() ?: false
        }
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_HEADSETHOOK -> {
            onPlayPause?.invoke() ?: false
        }
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_ESCAPE -> {
            onBack?.invoke() ?: false
        }
        else -> false
    }
}
