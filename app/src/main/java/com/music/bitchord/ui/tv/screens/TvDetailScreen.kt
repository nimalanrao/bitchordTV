package com.music.bitchord.ui.tv.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.DetailPage
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.UiState
import com.music.bitchord.playback.playSongs
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.components.TvEmptyState
import com.music.bitchord.ui.tv.components.TvErrorState
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

@Composable
fun TvDetailScreen(
    browseId: String,
    initialTitle: String,
    initialSubtitle: String,
    initialThumbnailUrl: String?,
    type: BrowseType,
    viewModel: MainViewModel,
    mediaController: MediaController?,
    onNavigateToNowPlaying: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailStack by viewModel.detailStack.collectAsState()
    val page = detailStack.lastOrNull { it.browseId == browseId }

    LaunchedEffect(browseId) {
        if (page == null) {
            viewModel.openDetail(browseId, initialTitle, initialSubtitle, initialThumbnailUrl, type)
        }
    }

    if (page == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = TvColors.AccentRed)
        }
    } else {
        TvDetailContent(
            page = page,
            onPlaySongAt = { songs, index ->
                mediaController?.playSongs(songs, index)
                onNavigateToNowPlaying()
            },
            onShufflePlay = { songs ->
                if (songs.isNotEmpty()) {
                    mediaController?.playSongs(songs.shuffled(), 0)
                    onNavigateToNowPlaying()
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun TvDetailContent(
    page: DetailPage,
    onPlaySongAt: (List<Song>, Int) -> Unit,
    onShufflePlay: (List<Song>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val songs = when (val s = page.songs) {
        is UiState.Success -> s.data
        else -> emptyList()
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = TvDimensions.SafeMarginVertical,
                bottom = 40.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        // Left Pane: Metadata & Action Buttons
        Column(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(TvColors.SurfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (!page.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(page.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = page.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = page.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.W800,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = page.subtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.W500,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (songs.isNotEmpty()) {
                Text(
                    text = "${songs.size} tracks",
                    fontSize = 13.sp,
                    fontFamily = TvSFProDisplay,
                    color = TvColors.TextMuted,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvButton(
                    text = "Play",
                    icon = Icons.Default.PlayArrow,
                    isPrimary = true,
                    enabled = songs.isNotEmpty(),
                    onClick = { onPlaySongAt(songs, 0) },
                )

                TvButton(
                    text = "Shuffle",
                    icon = Icons.Default.Shuffle,
                    isPrimary = false,
                    enabled = songs.isNotEmpty(),
                    onClick = { onShufflePlay(songs) },
                )
            }
        }

        // Right Pane: Track List
        Column(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight(),
        ) {
            when (val s = page.songs) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TvColors.AccentRed)
                    }
                }
                is UiState.Error -> {
                    TvErrorState(
                        message = s.message,
                        onRetry = { },
                    )
                }
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        TvEmptyState(
                            title = "No tracks available",
                            message = "This collection does not contain any playable tracks.",
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                        ) {
                            itemsIndexed(
                                items = s.data,
                                key = { index, song -> "${song.videoId}_$index" },
                            ) { index, song ->
                                TvTrackRow(
                                    index = index + 1,
                                    song = song,
                                    onClick = { onPlaySongAt(s.data, index) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvTrackRow(
    index: Int,
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) TvColors.SurfaceFocused else TvColors.SurfaceVariant,
        label = "trackRowBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.02f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onClick,
            )
            .background(animatedBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "$index",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = TvSFProDisplay,
            color = if (isFocused) TvColors.AccentRed else TvColors.TextMuted,
            modifier = Modifier.width(28.dp),
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TvColors.Surface),
            contentAlignment = Alignment.Center,
        ) {
            if (!song.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = TvColors.TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (!song.durationText.isNullOrBlank()) {
            Text(
                text = song.durationText,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextMuted,
            )
        }
    }
}
