package com.music.bitchord.ui.tv.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.SearchFilter
import com.music.bitchord.data.model.SearchResult
import com.music.bitchord.data.model.UiState
import com.music.bitchord.playback.playSongs
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.components.TvCard
import com.music.bitchord.ui.tv.components.TvEmptyState
import com.music.bitchord.ui.tv.components.TvErrorState
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

@Composable
fun TvSearchScreen(
    viewModel: MainViewModel,
    mediaController: MediaController?,
    onNavigateToDetail: (browseId: String, title: String, subtitle: String, thumbnailUrl: String?, type: BrowseType) -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val query by viewModel.query.collectAsState()
    val resultsState by viewModel.results.collectAsState()
    val activeFilter by viewModel.filter.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = 16.dp,
            ),
    ) {
        // Search Input Bar
        TvSearchBar(
            query = query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
                viewModel.submitSearch()
            },
            onClear = {
                viewModel.onQueryChange("")
            },
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(SearchFilter.entries.toTypedArray()) { filter ->
                val isSelected = filter == activeFilter
                TvSearchFilterChip(
                    label = filter.label,
                    isSelected = isSelected,
                    onClick = {
                        viewModel.onFilterChange(filter)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Content / Results
        when (val state = resultsState) {
            null -> {
                // Show Recent Searches or Suggestions
                if (query.isNotBlank() && suggestions.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Suggestions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = TvSFProDisplay,
                            color = TvColors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(suggestions) { suggestion ->
                                TvSearchFilterChip(
                                    label = suggestion,
                                    isSelected = false,
                                    onClick = {
                                        viewModel.searchFor(suggestion)
                                    },
                                )
                            }
                        }
                    }
                } else if (searchHistory.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Recent Searches",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = TvSFProDisplay,
                            color = TvColors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(searchHistory) { recent ->
                                TvSearchFilterChip(
                                    label = recent,
                                    isSelected = false,
                                    onClick = {
                                        viewModel.searchFor(recent)
                                    },
                                )
                            }
                        }
                    }
                } else {
                    TvEmptyState(
                        title = "Search YouTube Music",
                        message = "Type a song, album, artist, or playlist to start listening on TV.",
                    )
                }
            }
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
                    message = state.message,
                    onRetry = { viewModel.submitSearch() },
                )
            }
            is UiState.Success -> {
                val results = state.data
                if (results.isEmpty()) {
                    TvEmptyState(
                        title = "No results found",
                        message = "Try searching for a different track, artist, or album.",
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(170.dp),
                        horizontalArrangement = Arrangement.spacedBy(TvDimensions.CardSpacing),
                        verticalArrangement = Arrangement.spacedBy(TvDimensions.ShelfSpacing),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                    ) {
                        items(
                            items = results,
                            key = { result ->
                                when (result) {
                                    is SearchResult.Track -> result.song.videoId
                                    is SearchResult.Browse -> result.item.browseId
                                }
                            },
                        ) { result ->
                            when (result) {
                                is SearchResult.Track -> {
                                    TvCard(
                                        title = result.song.title,
                                        subtitle = result.song.artist,
                                        artworkUrl = result.song.thumbnailUrl,
                                        onClick = {
                                            viewModel.recordSearch()
                                            mediaController?.playSongs(listOf(result.song), 0)
                                            onNavigateToNowPlaying()
                                        },
                                    )
                                }
                                is SearchResult.Browse -> {
                                    TvCard(
                                        title = result.item.title,
                                        subtitle = result.item.subtitle,
                                        artworkUrl = result.item.thumbnailUrl,
                                        isCircle = result.item.type == BrowseType.ARTIST,
                                        badge = when (result.item.type) {
                                            BrowseType.ALBUM -> "Album"
                                            BrowseType.PLAYLIST -> "Playlist"
                                            BrowseType.ARTIST -> "Artist"
                                            else -> null
                                        },
                                        onClick = {
                                            viewModel.recordSearch()
                                            onNavigateToDetail(
                                                result.item.browseId,
                                                result.item.title,
                                                result.item.subtitle,
                                                result.item.thumbnailUrl,
                                                result.item.type,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .tvButtonFocus(
                shape = RoundedCornerShape(16.dp),
                focusedScale = 1.01f,
                focusedBorderColor = TvColors.BorderFocused,
            )
            .background(TvColors.SurfaceVariant)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = if (isFocused) TvColors.AccentRed else TvColors.TextSecondary,
            modifier = Modifier.size(24.dp),
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = TvSFProDisplay,
            ),
            singleLine = true,
            cursorBrush = SolidColor(TvColors.AccentRed),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search songs, albums, artists...",
                        color = TvColors.TextMuted,
                        fontSize = 18.sp,
                        fontFamily = TvSFProDisplay,
                    )
                }
                innerTextField()
            },
        )

        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onClear() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = TvColors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun TvSearchFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .tvButtonFocus(
                shape = RoundedCornerShape(20.dp),
                focusedScale = 1.06f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onClick,
            )
            .background(
                when {
                    isSelected -> TvColors.AccentRed
                    isFocused -> TvColors.SurfaceFocused
                    else -> TvColors.SurfaceVariant
                }
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
            fontFamily = TvSFProDisplay,
            color = if (isSelected) Color.White else TvColors.TextPrimary,
        )
    }
}
