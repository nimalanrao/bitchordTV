package com.music.bitchord.ui.tv.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.music.bitchord.ui.tv.theme.appleSpring
import com.music.bitchord.ui.tv.theme.AppleSpringPreset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.BuildConfig
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.audio.TvSpatialAudioEngine
import com.music.bitchord.ui.tv.components.TvDialog
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.personalization.AppThemeOption
import com.music.bitchord.ui.tv.theme.LocalTvFontFamily
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvFontOption
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

/**
 * 1:1 Apple TV Settings Layout matching the exact reference image.
 *
 * - Left Pane: Red Squircle BitChord Music Icon card and dynamic explanatory description text.
 * - Right Pane: Grouped Pill Menu (LIBRARY, HOME SCREEN, AUDIO, CANVAS, FONTS, ABOUT).
 * - Full color inversion on focus/hover (Solid White Pill, Pure Black Text).
 */
@Composable
fun TvSettingsScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null,
    onOpenAccountDialog: () -> Unit,
    onOpenDiscordDialog: () -> Unit,
    onOpenScrobbleDialog: () -> Unit,
    onOpenSourcesDialog: () -> Unit,
    onOpenRefreshRateDialog: () -> Unit,
    onOpenNicknameDialog: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onRunSetupAgain: () -> Unit,
    onOpenAboutDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentFont = LocalTvFontFamily.current

    val tvNickname by AppSettings.tvNickname.collectAsState()
    val tvThemeId by AppSettings.tvTheme.collectAsState()
    val tvFontId by AppSettings.tvFontFamily.collectAsState()
    val currentFontOption = TvFontOption.fromId(tvFontId)
    val currentTheme = AppThemeOption.fromId(tvThemeId)

    val spatialAudio by AppSettings.spatialAudioEnabled.collectAsState()
    val soundCheck by AppSettings.soundCheckEnabled.collectAsState()
    val liveCanvas by AppSettings.animatedCanvas.collectAsState()
    val automixEnabled by AppSettings.smartFadeEnabled.collectAsState()
    val syncedLyrics by AppSettings.syncedLyrics.collectAsState()
    val showNerdStats by AppSettings.showNerdStats.collectAsState()
    val addPlaylistSongs by AppSettings.addPlaylistSongsToLibrary.collectAsState()
    val addFavoriteSongs by AppSettings.addFavoriteSongsToLibrary.collectAsState()

    var activeDescription by remember {
        mutableStateOf("Configure audio, spatial sound, video canvas, typography, and personalized living room preferences.")
    }

    var showFontDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = 22.dp,
                bottom = 24.dp,
            ),
    ) {
        // Top Header with optional Back button & centered Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .tvButtonFocus(
                            shape = CircleShape,
                            focusedScale = 1.15f,
                            focusedBorderColor = Color.White,
                            onClick = onBack,
                        )
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = "Music & TV Settings",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = currentFont,
                color = Color.White,
            )
        }

        // Split 2-Pane Apple TV Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── LEFT HERO PANE (1:1 Apple TV Squircle & Description) ───────────
            Column(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Apple Music Red Gradient Squircle Card
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 160.dp)
                        .shadow(28.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFFF2D55).copy(alpha = 0.45f))
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF335E),
                                    Color(0xFFE60039),
                                    Color(0xFFB30026),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logo),
                        contentDescription = "BitChord Logo",
                        tint = Color.White,
                        modifier = Modifier.size(76.dp),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Dynamic Caption / Explanation
                Text(
                    text = activeDescription,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontFamily = currentFont,
                    color = Color.White.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }

            // ── RIGHT PANE: CATEGORIZED APPLE TV PILL LIST ───────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ── SECTION: LIBRARY ──
                item {
                    TvSettingsSectionHeader(title = "LIBRARY")
                }
                item {
                    TvApplePillOption(
                        title = "Add Playlist Songs to Library",
                        value = if (addPlaylistSongs) "On" else "Off",
                        onFocus = {
                            activeDescription = "Songs will be automatically added to your library when you add them to your playlists."
                        },
                        onClick = { AppSettings.setAddPlaylistSongsToLibrary(!addPlaylistSongs) },
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Add Favorite Songs to Library",
                        value = if (addFavoriteSongs) "On" else "Off",
                        onFocus = {
                            activeDescription = "Liked tracks and heart favorites will be saved to your primary music library."
                        },
                        onClick = { AppSettings.setAddFavoriteSongsToLibrary(!addFavoriteSongs) },
                    )
                }

                // ── SECTION: AUDIO & SPATIAL ──
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSettingsSectionHeader(title = "AUDIO & SPATIAL")
                }
                item {
                    TvApplePillOption(
                        title = "Spatial Audio & 3D Virtualization",
                        value = if (spatialAudio) "Automatic" else "Off",
                        onFocus = {
                            activeDescription = "Expands stereo music into an immersive 3D Dolby Atmos soundstage for TV speakers and soundbars."
                        },
                        onClick = {
                            val next = !spatialAudio
                            AppSettings.setSpatialAudioEnabled(next)
                            TvSpatialAudioEngine.setEnabled(next)
                        },
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Audio Quality",
                        value = "Lossless >",
                        onFocus = {
                            activeDescription = "Streams bit-exact Hi-Res FLAC and studio master quality audio from configured sources."
                        },
                        onClick = onOpenSourcesDialog,
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Sound Check (Loudness Match)",
                        value = if (soundCheck) "On" else "Off",
                        onFocus = {
                            activeDescription = "Maintains consistent loudness volume across all albums and music sources."
                        },
                        onClick = { AppSettings.setSoundCheckEnabled(!soundCheck) },
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Automix DJ Transitions",
                        value = if (automixEnabled) "On" else "Off",
                        onFocus = {
                            activeDescription = "Beat-matches and smoothly mixes transitions between consecutive songs using on-device DSP."
                        },
                        onClick = { AppSettings.setSmartFadeEnabled(!automixEnabled) },
                    )
                }

                // ── SECTION: LIVE VIDEO CANVAS ──
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSettingsSectionHeader(title = "CANVAS & VISUALS")
                }
                item {
                    TvApplePillOption(
                        title = "Live Video Canvas",
                        value = if (liveCanvas) "On" else "Off",
                        onFocus = {
                            activeDescription = "Plays looping artist motion video artwork behind the player. Shows notification when unavailable."
                        },
                        onClick = { AppSettings.setAnimatedCanvas(!liveCanvas) },
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Synchronized Lyrics",
                        value = if (syncedLyrics) "On" else "Off",
                        onFocus = {
                            activeDescription = "Real-time word-by-word and syllable flowing highlight lyrics with background vocal support."
                        },
                        onClick = { AppSettings.setSyncedLyrics(!syncedLyrics) },
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Stats for Nerds HUD",
                        value = if (showNerdStats) "On" else "Off",
                        onFocus = {
                            activeDescription = "Displays live codec, bitrate, sample rate, and audio resolution telemetry on the player."
                        },
                        onClick = { AppSettings.setShowNerdStats(!showNerdStats) },
                    )
                }

                // ── SECTION: TYPOGRAPHY & THEMES ──
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSettingsSectionHeader(title = "TYPOGRAPHY & DISPLAY")
                }
                item {
                    TvApplePillOption(
                        title = "Font Style",
                        value = "${currentFontOption.title} >",
                        onFocus = {
                            activeDescription = "Pick between Apple SF Pro, Google Sans, Arial, and Minecraft Pixel typography."
                        },
                        onClick = { showFontDialog = true },
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Visual Theme",
                        value = "${currentTheme.title} >",
                        onFocus = {
                            activeDescription = "Select Dynamic Artwork mesh gradient, Midnight Dark, or True #000000 OLED Pure Black."
                        },
                        onClick = onOpenThemeDialog,
                    )
                }
                item {
                    TvApplePillOption(
                        title = "TV Display Refresh Rate",
                        value = "Configure >",
                        onFocus = {
                            activeDescription = "Adjust display refresh rate mode: 120Hz Ultra-Performance, 60Hz, or Cinematic Match."
                        },
                        onClick = onOpenRefreshRateDialog,
                    )
                }
                item {
                    TvApplePillOption(
                        title = "TV Nickname",
                        value = "\"$tvNickname\" >",
                        onFocus = {
                            activeDescription = "Change the living room name and greeting for this TV."
                        },
                        onClick = onOpenNicknameDialog,
                    )
                }

                // ── SECTION: ACCOUNT & ABOUT ──
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSettingsSectionHeader(title = "ACCOUNT & ABOUT")
                }
                item {
                    TvApplePillOption(
                        title = "Google & YouTube Account",
                        value = "Sign In >",
                        onFocus = {
                            activeDescription = "Sign in directly with your Google Account on TV or via phone to access your personalized library, playlists, and recommendations."
                        },
                        onClick = onOpenAccountDialog,
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Discord & Scrobbling",
                        value = "Manage >",
                        onFocus = {
                            activeDescription = "Broadcast playing tracks to Discord Rich Presence and scrobble to Last.fm / ListenBrainz."
                        },
                        onClick = onOpenDiscordDialog,
                    )
                }
                item {
                    TvApplePillOption(
                        title = "Run Setup Assistant Again",
                        value = "Start >",
                        onFocus = {
                            activeDescription = "Reconfigure nickname, theme, and first-run TV setup."
                        },
                        onClick = onRunSetupAgain,
                    )
                }
                item {
                    TvApplePillOption(
                        title = "About BitChord TV",
                        value = "v${BuildConfig.VERSION_NAME} >",
                        onFocus = {
                            activeDescription = "BitChord TV v${BuildConfig.VERSION_NAME} • Architecture & Development by Nithyanantha (Nyxcore)."
                        },
                        onClick = onOpenAboutDialog,
                    )
                }
            }
        }
    }

    // Font Selection Dialog
    if (showFontDialog) {
        TvFontSelectionDialog(
            currentFontId = tvFontId,
            onSelect = { fontId ->
                AppSettings.setTvFontFamily(fontId)
                showFontDialog = false
            },
            onDismiss = { showFontDialog = false },
        )
    }
}

/**
 * Apple TV Section Header in uppercase letter-spaced small text.
 */
@Composable
private fun TvSettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        fontFamily = LocalTvFontFamily.current,
        color = Color.White.copy(alpha = 0.45f),
        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 4.dp),
    )
}

/**
 * 1:1 Apple TV Settings Pill Option with high-contrast sharp color inversion.
 * (Unfocused: translucent dark pill with white text; Focused: pure white pill with black text).
 */
@Composable
private fun TvApplePillOption(
    title: String,
    value: String,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    androidx.compose.runtime.LaunchedEffect(isFocused) {
        if (isFocused) onFocus()
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1.0f,
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "pillScale",
    )

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.12f),
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "pillBg",
    )

    val titleColor = if (isFocused) Color.Black else Color.White
    val valueColor = if (isFocused) Color(0xFF222222) else Color.White.copy(alpha = 0.60f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .background(animatedBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = LocalTvFontFamily.current,
            color = titleColor,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = value,
            fontSize = 15.sp,
            fontFamily = LocalTvFontFamily.current,
            fontWeight = if (isFocused) FontWeight.W800 else FontWeight.Normal,
            color = valueColor,
        )
    }
}

/**
 * Font Selection Modal Dialog.
 */
@Composable
private fun TvFontSelectionDialog(
    currentFontId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    TvDialog(
        title = "Choose Font Style",
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (option in TvFontOption.entries) {
                val isSelected = option.id.equals(currentFontId, ignoreCase = true)
                TvApplePillOption(
                    title = option.title,
                    value = if (isSelected) "Active" else "",
                    onFocus = {},
                    onClick = { onSelect(option.id) },
                )
            }
        }
    }
}
