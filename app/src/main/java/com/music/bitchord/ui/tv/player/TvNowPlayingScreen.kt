package com.music.bitchord.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import com.music.bitchord.data.LikeState
import com.music.bitchord.data.model.LikeStatus
import com.music.bitchord.playback.PlayerState
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

@Composable
fun TvNowPlayingScreen(
    viewModel: MainViewModel,
    mediaController: MediaController?,
    playerState: PlayerState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = playerState.song
    val isPlaying = playerState.isPlaying
    val currentPositionMs = playerState.position.positionMs
    val durationMs = playerState.durationMs
    val repeatMode = playerState.repeatMode

    val likeOverrides by LikeState.overrides.collectAsState()
    val isLiked = song?.let { likeOverrides[it.videoId] == LikeStatus.LIKE } ?: false

    val lyrics by viewModel.lyrics.collectAsState()
    val lyricsChecked by viewModel.lyricsChecked.collectAsState()

    var isLyricsActive by remember { mutableStateOf(false) }

    LaunchedEffect(song?.videoId) {
        if (song != null && durationMs > 0) {
            viewModel.loadLyrics(
                videoId = song.videoId,
                title = song.title,
                artist = song.artist,
                durationMs = durationMs,
                album = song.album,
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvColors.Background)
            .onTvKeyEvent(
                onPlayPause = {
                    if (isPlaying) mediaController?.pause() else mediaController?.play()
                    true
                },
                onBack = {
                    if (isLyricsActive) {
                        isLyricsActive = false
                        true
                    } else {
                        onBack()
                        true
                    }
                },
            ),
    ) {
        if (song == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No track currently playing",
                    color = TvColors.TextSecondary,
                    fontSize = 18.sp,
                    fontFamily = TvSFProDisplay,
                )
            }
            return@Box
        }

        // Full-bleed Cinematic Background with gradient scrims
        TvPlayerBackground(
            artworkUrl = song.thumbnailUrl,
            isLyricsMode = isLyricsActive,
        )

        // View Mode: Standard Cinematic Player vs. Full Synchronized Lyrics Layout
        Crossfade(
            targetState = isLyricsActive,
            label = "tvPlayerModeCrossfade",
        ) { lyricsMode ->
            if (lyricsMode) {
                TvLyricsOverlay(
                    song = song,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    lyrics = lyrics,
                    isLoadingLyrics = !lyricsChecked && lyrics == null,
                    lyricsError = if (lyricsChecked && lyrics == null) "Lyrics not available" else null,
                    onRetryLyrics = {
                        viewModel.loadLyrics(
                            videoId = song.videoId,
                            title = song.title,
                            artist = song.artist,
                            durationMs = durationMs,
                            album = song.album,
                        )
                    },
                    onSeek = { targetMs -> mediaController?.seekTo(targetMs) },
                    onPlayPause = {
                        if (isPlaying) mediaController?.pause() else mediaController?.play()
                    },
                    onPrevious = { mediaController?.seekToPreviousMediaItem() },
                    onNext = { mediaController?.seekToNextMediaItem() },
                    onCloseLyrics = { isLyricsActive = false },
                )
            } else {
                TvPlayerLayout(
                    song = song,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    isLiked = isLiked,
                    isShuffleActive = mediaController?.shuffleModeEnabled ?: false,
                    repeatMode = repeatMode,
                    isLyricsActive = false,
                    hasPrevious = playerState.hasPrevious,
                    hasNext = playerState.hasNext,
                    onToggleLike = { viewModel.toggleLike(song) },
                    onToggleShuffle = {
                        mediaController?.shuffleModeEnabled = !(mediaController?.shuffleModeEnabled ?: false)
                    },
                    onPrevious = { mediaController?.seekToPreviousMediaItem() },
                    onPlayPause = {
                        if (isPlaying) mediaController?.pause() else mediaController?.play()
                    },
                    onNext = { mediaController?.seekToNextMediaItem() },
                    onToggleQueue = { /* Queue overlay */ },
                    onToggleLyrics = { isLyricsActive = true },
                    onSeek = { targetMs -> mediaController?.seekTo(targetMs) },
                )
            }
        }
    }
}
