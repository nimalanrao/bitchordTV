package com.music.bitchord.ui.tv.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.cos
import kotlin.math.sin

/**
 * Apple Music-style full-bleed mesh gradient background with animated color orbs
 * and subtle noise/grain texture overlay.
 *
 * Designed for 120Hz TV rendering — all drawing happens in a single cached pass
 * with only the animation offset changing per frame.
 *
 * @param colors 3-4 dominant colors extracted from album artwork. Falls back to
 *               default purple/blue gradient when empty.
 * @param animationSpeed Animation cycle duration in ms. Set to 0 to disable.
 */
@Composable
fun TvMeshBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    animationSpeed: Long = 12000L,
    noiseOpacity: Float = 0.04f,
) {
    val effectiveColors = if (colors.size >= 3) colors.take(4) else listOf(
        Color(0xFF4A1A6B), // Deep purple
        Color(0xFF1A3A8B), // Royal blue
        Color(0xFF6B1A4A), // Berry
        Color(0xFF1A1A3A), // Deep navy
    )

    val infiniteTransition = rememberInfiniteTransition(label = "meshGradient")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationSpeed.toInt(),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "meshProgress",
    )

    // Pre-create noise bitmap once and cache it
    val noisePaint = remember {
        val noiseBitmap = createNoiseBitmap(64, 64)
        val shader = BitmapShader(noiseBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        android.graphics.Paint().apply {
            this.shader = shader
            alpha = (noiseOpacity * 255).toInt()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height

                onDrawBehind {
                    // 1. Base dark fill
                    drawRect(color = Color(0xFF08080B))

                    // 2. Animated radial gradient orbs (3-4 large blurred circles)
                    val t = animationProgress
                    val orbPositions = listOf(
                        Offset(
                            x = w * (0.2f + 0.15f * sin(t * 6.28f)),
                            y = h * (0.3f + 0.1f * cos(t * 6.28f)),
                        ),
                        Offset(
                            x = w * (0.7f + 0.12f * cos(t * 6.28f + 1f)),
                            y = h * (0.2f + 0.15f * sin(t * 6.28f + 2f)),
                        ),
                        Offset(
                            x = w * (0.5f + 0.1f * sin(t * 6.28f + 3f)),
                            y = h * (0.7f + 0.12f * cos(t * 6.28f + 1.5f)),
                        ),
                    )

                    val orbRadius = minOf(w, h) * 0.55f

                    orbPositions.forEachIndexed { index, center ->
                        val color = effectiveColors.getOrElse(index) { effectiveColors.last() }
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.6f),
                                    color.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = orbRadius,
                            ),
                            center = center,
                            radius = orbRadius,
                        )
                    }

                    // Optional 4th orb
                    if (effectiveColors.size >= 4) {
                        val center = Offset(
                            x = w * (0.35f + 0.08f * cos(t * 6.28f + 4f)),
                            y = h * (0.5f + 0.1f * sin(t * 6.28f + 3f)),
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    effectiveColors[3].copy(alpha = 0.45f),
                                    effectiveColors[3].copy(alpha = 0.1f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = orbRadius * 0.85f,
                            ),
                            center = center,
                            radius = orbRadius * 0.85f,
                        )
                    }

                    // 3. Noise texture overlay
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRect(
                            0f, 0f, w, h,
                            noisePaint,
                        )
                    }
                }
            },
    )
}

/**
 * Creates a small noise bitmap that tiles across the screen.
 * Kept tiny (64x64) so the shader is cheap to sample.
 */
private fun createNoiseBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
    val pixels = IntArray(width * height)
    val random = java.util.Random(42) // Deterministic seed for consistent grain
    for (i in pixels.indices) {
        val noise = random.nextInt(256)
        pixels[i] = (noise shl 24) or 0x00FFFFFF // White noise with varying alpha
    }
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}
