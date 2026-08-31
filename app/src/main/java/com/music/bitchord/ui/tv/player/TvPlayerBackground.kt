package com.music.bitchord.ui.tv.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.ui.tv.components.TvMeshBackground
import com.music.bitchord.ui.tv.theme.TvColors

/**
 * Ultra-performance cinematic Apple Music TV background.
 * In Lyrics mode: Renders full-bleed animated mesh gradient with noise grain.
 * In Standard Player mode: Single-pass cached cinematic artwork scrim.
 */
@Composable
fun TvPlayerBackground(
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    isLyricsMode: Boolean = false,
) {
    if (isLyricsMode) {
        // Apple Music 1:1 animated mesh gradient with noise grain
        TvMeshBackground(
            colors = listOf(
                Color(0xFF4A1060), // Royal Plum
                Color(0xFF16256A), // Deep Sapphire
                Color(0xFF6B1A3F), // Crimson Berry
                Color(0xFF101026), // Dark Indigo
            ),
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier.fillMaxSize().background(TvColors.Background)) {
            Crossfade(
                targetState = artworkUrl,
                animationSpec = tween(durationMillis = 500),
                label = "tvBackgroundCrossfade",
            ) { url ->
                if (!url.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TvColors.BackgroundGradient),
                    )
                }
            }

            // Single-pass cached gradient overlay (0 GPU overdraw overhead)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height

                        val topBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xCC000000), Color(0x55000000), Color.Transparent),
                            startY = 0f,
                            endY = h * 0.35f,
                        )

                        val bottomBrush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x88000000), Color(0xFB050508)),
                            startY = h * 0.45f,
                            endY = h,
                        )

                        val globalDarken = Color(0x9908080B)

                        onDrawWithContent {
                            drawContent()
                            drawRect(color = globalDarken, size = size)
                            drawRect(brush = topBrush, size = Size(w, h * 0.35f))
                            drawRect(
                                brush = bottomBrush,
                                topLeft = Offset(0f, h * 0.45f),
                                size = Size(w, h * 0.55f),
                            )
                        }
                    },
            )
        }
    }
}
