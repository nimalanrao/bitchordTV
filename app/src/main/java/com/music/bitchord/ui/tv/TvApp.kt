package com.music.bitchord.ui.tv

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.bitchord.R
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.playback.PlayerState
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.dialogs.TvAboutDialog
import com.music.bitchord.ui.tv.dialogs.TvAccountDialog
import com.music.bitchord.ui.tv.dialogs.TvDiscordDialog
import com.music.bitchord.ui.tv.dialogs.TvNicknameDialog
import com.music.bitchord.ui.tv.dialogs.TvScrobbleDialog
import com.music.bitchord.ui.tv.dialogs.TvSourcesDialog
import com.music.bitchord.ui.tv.dialogs.TvThemeDialog
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.player.TvNowPlayingScreen
import com.music.bitchord.ui.tv.screens.TvDetailScreen
import com.music.bitchord.ui.tv.screens.TvHomeScreen
import com.music.bitchord.ui.tv.screens.TvLibraryScreen
import com.music.bitchord.ui.tv.screens.TvSearchScreen
import com.music.bitchord.ui.tv.screens.TvSettingsScreen
import com.music.bitchord.ui.tv.theme.AppleSpringPreset
import com.music.bitchord.ui.tv.theme.BitChordTvTheme
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay
import com.music.bitchord.ui.tv.theme.TvThemeColors
import com.music.bitchord.ui.tv.theme.appleSpring

enum class TvDestination(val label: String, val icon: ImageVector) {
    FOR_YOU("For You", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SEARCH("Search", Icons.Default.Search),
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
    BitChordTvTheme {
        var activeDestination by remember { mutableStateOf(TvDestination.FOR_YOU) }
        var activeDetail by remember { mutableStateOf<DetailDestination?>(null) }
        var isNowPlayingOpen by remember { mutableStateOf(false) }

        val setupVersionCompleted by com.music.bitchord.data.settings.AppSettings.tvSetupVersionCompleted.collectAsState()
        var isRunningSetup by remember { mutableStateOf(setupVersionCompleted == 0) }

        // Dialog States
        var showAccountDialog by remember { mutableStateOf(false) }
        var showDiscordDialog by remember { mutableStateOf(false) }
        var showScrobbleDialog by remember { mutableStateOf(false) }
        var showSourcesDialog by remember { mutableStateOf(false) }
        var showNicknameDialog by remember { mutableStateOf(false) }
        var showThemeDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }

        val palette = TvThemeColors.current

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.background),
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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Apple Music Top Navigation Bar
                    TvTopNavigationBar(
                        activeDestination = activeDestination,
                        hasNowPlaying = playerState.song != null,
                        onDestinationSelected = { dest ->
                            activeDetail = null
                            activeDestination = dest
                        },
                        onOpenNowPlaying = { isNowPlayingOpen = true },
                    )

                    // Main Screen Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
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
                                TvDestination.FOR_YOU -> {
                                    TvHomeScreen(
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
                                TvDestination.SETTINGS -> {
                                    TvSettingsScreen(
                                        viewModel = viewModel,
                                        onOpenAccountDialog = { showAccountDialog = true },
                                        onOpenDiscordDialog = { showDiscordDialog = true },
                                        onOpenScrobbleDialog = { showScrobbleDialog = true },
                                        onOpenSourcesDialog = { showSourcesDialog = true },
                                        onOpenNicknameDialog = { showNicknameDialog = true },
                                        onOpenThemeDialog = { showThemeDialog = true },
                                        onRunSetupAgain = { isRunningSetup = true },
                                        onOpenAboutDialog = { showAboutDialog = true },
                                    )
                                }
                            }
                        }

                        // Floating Mini Playback Bar (bottom right)
                        if (playerState.song != null && !isNowPlayingOpen) {
                            TvGlobalMiniPlayer(
                                playerState = playerState,
                                mediaController = mediaController,
                                onClick = { isNowPlayingOpen = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = TvDimensions.SafeMarginHorizontal, bottom = 24.dp),
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
            if (showNicknameDialog) {
                TvNicknameDialog(onDismiss = { showNicknameDialog = false })
            }
            if (showThemeDialog) {
                TvThemeDialog(onDismiss = { showThemeDialog = false })
            }
            if (showAboutDialog) {
                TvAboutDialog(onDismiss = { showAboutDialog = false })
            }
        }
    }
}

/**
 * Apple Music-style Top Horizontal Navigation Bar.
 * Left: BitChord brand logo with wavy bars icon.
 * Center: Instant auto-selecting tabs (For You, Library, Now Playing).
 * Right: Quick action buttons (Search, Settings).
 */
@Composable
private fun TvTopNavigationBar(
    activeDestination: TvDestination,
    hasNowPlaying: Boolean,
    onDestinationSelected: (TvDestination) -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = TvThemeColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = 20.dp,
                bottom = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: BitChord Brand Logo (Pink circle badge with 3 wavy bars + BitChord text)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(palette.accentRed),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "BitChord Logo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = "BitChord",
                fontSize = 20.sp,
                fontWeight = FontWeight.W800,
                fontFamily = TvSFProDisplay,
                color = palette.textPrimary,
                letterSpacing = (-0.4).sp,
            )
        }

        // Center: Navigation Pill Tabs (Instant switch on D-pad focus!)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val mainTabs = listOf(
                TvDestination.FOR_YOU,
                TvDestination.LIBRARY,
            )

            mainTabs.forEach { destination ->
                val isSelected = activeDestination == destination
                TvNavPillItem(
                    label = destination.label,
                    isSelected = isSelected,
                    onFocus = { onDestinationSelected(destination) },
                    onClick = { onDestinationSelected(destination) },
                )
            }

            if (hasNowPlaying) {
                TvNavPillItem(
                    label = "Now Playing",
                    isSelected = false,
                    onFocus = {},
                    onClick = onOpenNowPlaying,
                )
            }
        }

        // Right: Icon action buttons (Search, Settings)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvNavIconButton(
                icon = Icons.Default.Search,
                contentDescription = "Search",
                isSelected = activeDestination == TvDestination.SEARCH,
                onFocus = { onDestinationSelected(TvDestination.SEARCH) },
                onClick = { onDestinationSelected(TvDestination.SEARCH) },
            )

            TvNavIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                isSelected = activeDestination == TvDestination.SETTINGS,
                onFocus = { onDestinationSelected(TvDestination.SETTINGS) },
                onClick = { onDestinationSelected(TvDestination.SETTINGS) },
            )
        }
    }
}

@Composable
private fun TvNavPillItem(
    label: String,
    isSelected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val palette = TvThemeColors.current

    // Instant tab switch on focus without requiring extra click
    LaunchedEffect(isFocused) {
        if (isFocused) onFocus()
    }

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color.White.copy(alpha = 0.22f)
            else -> Color.Transparent
        },
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "navPillBg",
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.Black
            isSelected -> Color.White
            else -> palette.textSecondary
        },
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "navPillText",
    )

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "navPillScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
            fontFamily = TvSFProDisplay,
            color = textColor,
        )
    }
}

@Composable
private fun TvNavIconButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val palette = TvThemeColors.current

    LaunchedEffect(isFocused) {
        if (isFocused) onFocus()
    }

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> palette.accentRed
            else -> Color.White.copy(alpha = 0.08f)
        },
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "navIconBg",
    )

    val iconTint by animateColorAsState(
        targetValue = when {
            isFocused -> Color.Black
            isSelected -> Color.White
            else -> palette.textSecondary
        },
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "navIconTint",
    )

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "navIconScale",
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
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
    val palette = TvThemeColors.current

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = modifier
            .width(360.dp)
            .height(68.dp)
            .tvButtonFocus(
                shape = RoundedCornerShape(18.dp),
                focusedScale = 1.04f,
                focusedBorderColor = palette.borderFocused,
                onClick = onClick,
            )
            .background(palette.surface)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.surfaceVariant),
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
                color = if (isFocused) palette.accentRed else palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                fontSize = 12.sp,
                fontFamily = TvSFProDisplay,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(palette.accentRed)
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
