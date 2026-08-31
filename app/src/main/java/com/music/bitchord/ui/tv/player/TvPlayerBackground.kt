package com.music.bitchord.ui.tv.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.ui.tv.theme.TvColors

@Composable
fun TvPlayerBackground(
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    isLyricsMode: Boolean = false,
) {
    Box(modifier = modifier.fillMaxSize().background(TvColors.Background)) {
        // Dynamic artwork background with crossfade transition
        Crossfade(
            targetState = artworkUrl,
            animationSpec = tween(durationMillis = 600),
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
                        .background(TvColors.BackgroundGradient)
                )
            }
        }

        // Global darkening scrim for TV cinematic contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(if (isLyricsMode) 0xDD08080B else 0x9908080B))
        )

        // Top Scrim: ensures context label is readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC000000),
                            Color(0x66000000),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = 220f,
                    )
                )
        )

        // Bottom Scrim: ensures metadata, seekbar, and transport controls are perfectly legible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x88000000),
                            Color(0xFA050508),
                        ),
                        startY = 320f,
                    )
                )
        )
    }
}
