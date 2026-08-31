package com.music.bitchord.ui.tv.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.focus.tvCardFocus
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

/**
 * Standard TV Content Card for albums, playlists, artists, and songs.
 */
@Composable
fun TvCard(
    title: String,
    subtitle: String?,
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 160.dp,
    aspectRatio: Float = 1.0f,
    isCircle: Boolean = false,
    isPlaying: Boolean = false,
    badge: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape: Shape = if (isCircle) CircleShape else RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .width(cardWidth)
            .tvCardFocus(
                shape = shape,
                focusedScale = 1.06f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(shape)
                .background(TvColors.SurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (!artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artworkUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            // Dark gradient scrim at the bottom of the artwork
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x88000000)),
                            startY = 100f,
                        )
                    )
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(TvColors.AccentRed),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TvColors.ScrimDark)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = badge,
                        color = TvColors.TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = TvSFProDisplay,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = if (isFocused) TvColors.AccentRed else TvColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
            fontFamily = TvSFProDisplay,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = TvColors.TextSecondary,
                fontSize = 12.sp,
                fontFamily = TvSFProDisplay,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Standard TV focusable button.
 */
@Composable
fun TvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            !enabled -> TvColors.SurfaceVariant.copy(alpha = 0.5f)
            isFocused -> if (isPrimary) TvColors.AccentRed else TvColors.SurfaceFocused
            isPrimary -> TvColors.AccentRed.copy(alpha = 0.85f)
            else -> TvColors.SurfaceVariant
        },
        label = "tvButtonBg",
    )

    Row(
        modifier = modifier
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.05f,
                onClick = if (enabled) onClick else null,
            )
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else TvColors.TextMuted,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = if (enabled) Color.White else TvColors.TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.W600,
            fontFamily = TvSFProDisplay,
        )
    }
}

/**
 * Focusable TV icon button.
 */
@Composable
fun TvIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    isPrimary: Boolean = false,
    isActive: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> if (isPrimary) TvColors.AccentRed else TvColors.SurfaceFocused
            isActive -> TvColors.AccentRed.copy(alpha = 0.3f)
            isPrimary -> TvColors.AccentRed
            else -> TvColors.SurfaceVariant
        },
        label = "tvIconBtnBg",
    )

    Box(
        modifier = modifier
            .size(size)
            .tvButtonFocus(
                shape = CircleShape,
                focusedScale = 1.1f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onClick,
            )
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = when {
                isFocused -> Color.White
                isActive -> TvColors.AccentRed
                isPrimary -> Color.White
                else -> TvColors.TextPrimary
            },
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * TV D-pad Remote Seek / Value Slider.
 * Press Left to decrease value, Right to increase value.
 */
@Composable
fun TvSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    step: Float = 0.05f,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .focusable(interactionSource = interactionSource)
            .onTvKeyEvent(
                onLeft = {
                    val newVal = (value - step).coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChange(newVal)
                    true
                },
                onRight = {
                    val newVal = (value + step).coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChange(newVal)
                    true
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isFocused) 8.dp else 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(TvColors.SurfaceVariant)
        )

        // Active Track
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(if (isFocused) 8.dp else 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isFocused) TvColors.AccentRed else TvColors.TextPrimary)
        )

        // Thumb Indicator (visible on focus)
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .padding(end = 0.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

/**
 * TV Shelf Section Header.
 */
@Composable
fun TvSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontFamily = TvSFProDisplay,
                    color = TvColors.TextSecondary,
                )
            }
        }

        if (actionLabel != null && onActionClick != null) {
            TvButton(
                text = actionLabel,
                onClick = onActionClick,
                modifier = Modifier.height(36.dp),
            )
        }
    }
}

/**
 * TV Loading Shimmer Skeletons for shelves.
 */
@Composable
fun TvShelfSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
    cardWidth: Dp = 160.dp,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        repeat(itemCount) {
            Column(modifier = Modifier.width(cardWidth)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(TvColors.SurfaceVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TvColors.SurfaceVariant)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TvColors.SurfaceVariant)
                )
            }
        }
    }
}

/**
 * TV Recoverable Error View with focusable Retry button.
 */
@Composable
fun TvErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Something went wrong",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = TvSFProDisplay,
            color = TvColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            fontFamily = TvSFProDisplay,
            color = TvColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.6f),
        )
        Spacer(modifier = Modifier.height(20.dp))
        TvButton(
            text = "Retry",
            icon = Icons.Default.Refresh,
            isPrimary = true,
            onClick = onRetry,
        )
    }
}

/**
 * TV Empty State View.
 */
@Composable
fun TvEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = TvSFProDisplay,
            color = TvColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            fontFamily = TvSFProDisplay,
            color = TvColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * TV Modal Dialog Container with focus trap and back press handling.
 */
@Composable
fun TvDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TvColors.ScrimDark),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth(0.55f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TvColors.Surface)
                    .padding(28.dp),
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TvSFProDisplay,
                    color = TvColors.TextPrimary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}
