package com.music.bitchord.ui.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.session.MediaController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.playback.PlayerState
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.dialogs.TvAboutDialog
import com.music.bitchord.ui.tv.dialogs.TvAccountDialog
import com.music.bitchord.ui.tv.dialogs.TvDiscordDialog
import com.music.bitchord.ui.tv.dialogs.TvScrobbleDialog
import com.music.bitchord.ui.tv.dialogs.TvSourcesDialog
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.screens.TvDetailScreen
import com.music.bitchord.ui.tv.screens.TvHomeScreen
import com.music.bitchord.ui.tv.screens.TvLibraryScreen
import com.music.bitchord.ui.tv.player.TvNowPlayingScreen
import com.music.bitchord.ui.tv.screens.TvSearchScreen
import com.music.bitchord.ui.tv.screens.TvSettingsScreen
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

enum class TvDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SETTINGS("Settings", Icons.Default.Settings),
}

private data class DetailDestination(
    val browseId: String,
    val title: String,
    val subtitle: String,
    val thumbnailUrl: String?,
    val type: BrowseType,
)

@Composable
fun TvApp(
    viewModel: MainViewModel,
    mediaController: MediaController?,
    playerState: PlayerState,
    modifier: Modifier = Modifier,
) {
    var activeDestination by remember { mutableStateOf(TvDestination.HOME) }
    var activeDetail by remember { mutableStateOf<DetailDestination?>(null) }
    var isNowPlayingOpen by remember { mutableStateOf(false) }

    val setupVersionCompleted by com.music.bitchord.data.settings.AppSettings.tvSetupVersionCompleted.collectAsState()
    var isRunningSetup by remember { mutableStateOf(setupVersionCompleted == 0) }

    // Dialog States
    var showAccountDialog by remember { mutableStateOf(false) }
    var showDiscordDialog by remember { mutableStateOf(false) }
    var showScrobbleDialog by remember { mutableStateOf(false) }
    var showSourcesDialog by remember { mutableStateOf(false) }
    var showRefreshRateDialog by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvColors.Background),
    ) {
        if (isRunningSetup) {
            com.music.bitchord.ui.tv.onboarding.TvSetupScreen(
                onComplete = { isRunningSetup = false },
            )
        } else if (isNowPlayingOpen) {
            TvNowPlayingScreen(
                viewModel = viewModel,
                mediaController = mediaController,
                playerState = playerState,
                onBack = { isNowPlayingOpen = false },
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // TV Navigation Rail (Left side)
                TvNavigationRail(
                    activeDestination = activeDestination,
                    onDestinationSelected = { dest ->
                        activeDetail = null
                        activeDestination = dest
                    },
                )

                // Main Content View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    if (activeDetail != null) {
                        val detail = activeDetail!!
                        TvDetailScreen(
                            browseId = detail.browseId,
                            initialTitle = detail.title,
                            initialSubtitle = detail.subtitle,
                            initialThumbnailUrl = detail.thumbnailUrl,
                            type = detail.type,
                            viewModel = viewModel,
                            mediaController = mediaController,
                            onNavigateToNowPlaying = { isNowPlayingOpen = true },
                            onBack = { activeDetail = null },
                        )
                    } else {
                        when (activeDestination) {
                            TvDestination.HOME -> {
                                TvHomeScreen(
                                    viewModel = viewModel,
                                    mediaController = mediaController,
                                    onNavigateToDetail = { browseId, title, subtitle, thumb, type ->
                                        activeDetail = DetailDestination(browseId, title, subtitle, thumb, type)
                                    },
                                    onNavigateToNowPlaying = { isNowPlayingOpen = true },
                                )
                            }
                            TvDestination.SEARCH -> {
                                TvSearchScreen(
                                    viewModel = viewModel,
                                    mediaController = mediaController,
                                    onNavigateToDetail = { browseId, title, subtitle, thumb, type ->
                                        activeDetail = DetailDestination(browseId, title, subtitle, thumb, type)
                                    },
                                    onNavigateToNowPlaying = { isNowPlayingOpen = true },
                                )
                            }
                            TvDestination.LIBRARY -> {
                                TvLibraryScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetail = { browseId, title, subtitle, thumb, type ->
                                        activeDetail = DetailDestination(browseId, title, subtitle, thumb, type)
                                    },
                                    onNavigateToLocalMusic = {
                                        activeDetail = DetailDestination(
                                            "local:all",
                                            "Local Device Audio",
                                            "Audio files on device storage",
                                            null,
                                            BrowseType.OTHER,
                                        )
                                    },
                                    onNavigateToDownloads = {
                                        activeDetail = DetailDestination(
                                            "local:downloads",
                                            "Offline Downloads",
                                            "Downloaded high-quality tracks",
                                            null,
                                            BrowseType.OTHER,
                                        )
                                    },
                                    onNavigateToHistory = {
                                        activeDetail = DetailDestination(
                                            "FEmusic_history",
                                            "Listening History",
                                            "Recently played tracks",
                                            null,
                                            BrowseType.PLAYLIST,
                                        )
                                    },
                                    onNavigateToLiked = {
                                        activeDetail = DetailDestination(
                                            "LM",
                                            "Liked Music",
                                            "Your favorites",
                                            null,
                                            BrowseType.PLAYLIST,
                                        )
                                    },
                                )
                            }
                            TvDestination.SETTINGS -> {
                                TvSettingsScreen(
                                    viewModel = viewModel,
                                    onOpenAccountDialog = { showAccountDialog = true },
                                    onOpenDiscordDialog = { showDiscordDialog = true },
                                    onOpenScrobbleDialog = { showScrobbleDialog = true },
                                    onOpenSourcesDialog = { showSourcesDialog = true },
                                    onOpenRefreshRateDialog = { showRefreshRateDialog = true },
                                    onOpenNicknameDialog = { showNicknameDialog = true },
                                    onOpenThemeDialog = { showThemeDialog = true },
                                    onRunSetupAgain = { isRunningSetup = true },
                                    onOpenAboutDialog = { showAboutDialog = true },
                                )
                            }
                        }
                    }

                    // Global Mini Playback Bar (Visible at bottom right when a song is loaded)
                    if (playerState.song != null && !isNowPlayingOpen) {
                        TvGlobalMiniPlayer(
                            playerState = playerState,
                            mediaController = mediaController,
                            onClick = { isNowPlayingOpen = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 48.dp, bottom = 24.dp),
                        )
                    }
                }
            }
        }

        // Dialog Overlays
        if (showAccountDialog) {
            TvAccountDialog(viewModel = viewModel, onDismiss = { showAccountDialog = false })
        }
        if (showDiscordDialog) {
            TvDiscordDialog(onDismiss = { showDiscordDialog = false })
        }
        if (showScrobbleDialog) {
            TvScrobbleDialog(onDismiss = { showScrobbleDialog = false })
        }
        if (showSourcesDialog) {
            TvSourcesDialog(onDismiss = { showSourcesDialog = false })
        }
        if (showRefreshRateDialog) {
            com.music.bitchord.ui.tv.dialogs.TvRefreshRateDialog(onDismiss = { showRefreshRateDialog = false })
        }
        if (showNicknameDialog) {
            com.music.bitchord.ui.tv.dialogs.TvNicknameDialog(onDismiss = { showNicknameDialog = false })
        }
        if (showThemeDialog) {
            com.music.bitchord.ui.tv.dialogs.TvThemeDialog(onDismiss = { showThemeDialog = false })
        }
        if (showAboutDialog) {
            TvAboutDialog(onDismiss = { showAboutDialog = false })
        }
    }
}

@Composable
private fun TvNavigationRail(
    activeDestination: TvDestination,
    onDestinationSelected: (TvDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) TvDimensions.NavigationRailExpandedWidth else TvDimensions.NavigationRailWidth,
        label = "navRailWidth",
    )

    Column(
        modifier = modifier
            .width(railWidth)
            .fillMaxHeight()
            .background(TvColors.Surface)
            .padding(vertical = TvDimensions.SafeMarginVertical),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // App Logo Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TvColors.AccentRed),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "BC",
                fontWeight = FontWeight.W900,
                fontSize = 17.sp,
                fontFamily = TvSFProDisplay,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Navigation Destination Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TvDestination.entries.forEach { destination ->
                val isSelected = destination == activeDestination
                TvNavRailItem(
                    destination = destination,
                    isSelected = isSelected,
                    isExpanded = isExpanded,
                    onFocusChanged = { focused ->
                        if (focused) isExpanded = true
                    },
                    onClick = { onDestinationSelected(destination) },
                )
            }
        }
    }
}

@Composable
private fun TvNavRailItem(
    destination: TvDestination,
    isSelected: Boolean,
    isExpanded: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        onFocusChanged(isFocused)
    }

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> TvColors.SurfaceFocused
            isSelected -> TvColors.SurfaceSelected
            else -> Color.Transparent
        },
        label = "navItemBg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .tvButtonFocus(
                shape = RoundedCornerShape(14.dp),
                focusedScale = 1.05f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onClick,
            )
            .background(bgColor)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = when {
                isFocused -> Color.White
                isSelected -> TvColors.AccentRed
                else -> TvColors.TextSecondary
            },
            modifier = Modifier.size(24.dp),
        )

        if (isExpanded) {
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = destination.label,
                fontSize = 15.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                fontFamily = TvSFProDisplay,
                color = if (isFocused || isSelected) Color.White else TvColors.TextSecondary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TvGlobalMiniPlayer(
    playerState: PlayerState,
    mediaController: MediaController?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = playerState.song ?: return
    val isPlaying = playerState.isPlaying

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = modifier
            .width(340.dp)
            .height(64.dp)
            .tvButtonFocus(
                shape = RoundedCornerShape(16.dp),
                focusedScale = 1.04f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onClick,
            )
            .background(TvColors.SurfaceVariant)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
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
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = TvSFProDisplay,
                color = if (isFocused) TvColors.AccentRed else TvColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                fontSize = 12.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(TvColors.AccentRed)
                .clickable {
                    if (isPlaying) mediaController?.pause() else mediaController?.play()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
