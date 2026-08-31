package com.music.bitchord.ui.tv.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.HomeShelf
import com.music.bitchord.data.model.ShelfItem
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.UiState
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.components.TvCard
import com.music.bitchord.ui.tv.components.TvEmptyState
import com.music.bitchord.ui.tv.components.TvErrorState
import com.music.bitchord.ui.tv.components.TvSectionHeader
import com.music.bitchord.ui.tv.components.TvShelfSkeleton
import com.music.bitchord.ui.tv.theme.TvDimensions

@Composable
fun TvHomeScreen(
    viewModel: MainViewModel,
    mediaController: MediaController?,
    onNavigateToDetail: (browseId: String, title: String, subtitle: String, thumbnailUrl: String?, type: BrowseType) -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val homeState by viewModel.home.collectAsState()

    when (val state = homeState) {
        is UiState.Loading -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = TvDimensions.SafeMarginHorizontal,
                        vertical = TvDimensions.SafeMarginVertical,
                    ),
            ) {
                TvShelfSkeleton(itemCount = 5)
                Spacer(modifier = Modifier.height(TvDimensions.ShelfSpacing))
                TvShelfSkeleton(itemCount = 5)
            }
        }
        is UiState.Error -> {
            TvErrorState(
                message = state.message,
                onRetry = { viewModel.refresh() },
                modifier = modifier,
            )
        }
        is UiState.Success -> {
            val shelves = state.data
            if (shelves.isEmpty()) {
                TvEmptyState(
                    title = "Nothing to display",
                    message = "Connect to the internet or sign in to load your music feed.",
                    modifier = modifier,
                )
            } else {
                TvHomeFeed(
                    shelves = shelves,
                    onPlaySong = { song ->
                        viewModel.play(song)
                        onNavigateToNowPlaying()
                    },
                    onNavigateToDetail = onNavigateToDetail,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun TvHomeFeed(
    shelves: List<HomeShelf>,
    onPlaySong: (Song) -> Unit,
    onNavigateToDetail: (browseId: String, title: String, subtitle: String, thumbnailUrl: String?, type: BrowseType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val verticalListState = rememberLazyListState()

    LazyColumn(
        state = verticalListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = TvDimensions.SafeMarginHorizontal,
            end = TvDimensions.SafeMarginHorizontal,
            top = TvDimensions.SafeMarginVertical,
            bottom = 100.dp, // room for global playback bar
        ),
        verticalArrangement = Arrangement.spacedBy(TvDimensions.ShelfSpacing),
    ) {
        items(
            items = shelves,
            key = { shelf -> shelf.title.ifBlank { shelf.hashCode().toString() } },
        ) { shelf ->
            TvShelfRow(
                shelf = shelf,
                onPlaySong = onPlaySong,
                onNavigateToDetail = onNavigateToDetail,
            )
        }
    }
}

@Composable
private fun TvShelfRow(
    shelf: HomeShelf,
    onPlaySong: (Song) -> Unit,
    onNavigateToDetail: (browseId: String, title: String, subtitle: String, thumbnailUrl: String?, type: BrowseType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TvSectionHeader(
            title = shelf.title,
            subtitle = shelf.subtitle.takeIf { it.isNotBlank() },
        )

        Spacer(modifier = Modifier.height(10.dp))

        val horizontalListState = rememberLazyListState()

        LazyRow(
            state = horizontalListState,
            horizontalArrangement = Arrangement.spacedBy(TvDimensions.CardSpacing),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        ) {
            items(
                items = shelf.items,
                key = { item -> item.videoId ?: item.browseId ?: item.title },
            ) { item ->
                TvCard(
                    title = item.title,
                    subtitle = item.subtitle,
                    artworkUrl = item.thumbnailUrl,
                    cardWidth = 175.dp,
                    onClick = {
                        if (item.videoId != null) {
                            val song = Song(
                                videoId = item.videoId,
                                title = item.title,
                                artist = item.subtitle,
                                album = null,
                                durationSeconds = 0,
                                thumbnailUrl = item.thumbnailUrl,
                            )
                            onPlaySong(song)
                        } else if (item.browseId != null) {
                            val browseType = when {
                                item.browseId.startsWith("VL") || item.browseId.startsWith("PL") || item.browseId.startsWith("RDAMPL") -> BrowseType.PLAYLIST
                                item.browseId.startsWith("MPRE") || item.browseId.startsWith("FEmusic_album") -> BrowseType.ALBUM
                                item.browseId.startsWith("UC") || item.browseId.startsWith("FEmusic_artist") -> BrowseType.ARTIST
                                else -> BrowseType.OTHER
                            }
                            onNavigateToDetail(
                                item.browseId,
                                item.title,
                                item.subtitle,
                                item.thumbnailUrl,
                                browseType,
                            )
                        }
                    },
                )
            }
        }
    }
}
