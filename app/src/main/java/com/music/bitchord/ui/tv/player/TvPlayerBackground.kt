package com.music.bitchord.ui.tv.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.music.bitchord.ui.tv.components.TvMeshBackground

/**
 * Ultra-performance cinematic Apple Music TV background.
 * Automatically samples colors from the active track artwork and renders
 * full-bleed animated blur mesh gradient with noise grain.
 */
@Composable
fun TvPlayerBackground(
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    isLyricsMode: Boolean = false,
) {
    TvMeshBackground(
        artworkUrl = artworkUrl,
        modifier = modifier,
    )
}
