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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import com.music.bitchord.data.settings.ThemeMode
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.personalization.AppThemeOption
import com.music.bitchord.ui.tv.theme.AppleSpringPreset
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay
import com.music.bitchord.ui.tv.theme.TvThemeColors
import com.music.bitchord.ui.tv.theme.appleSpring

/**
 * 1:1 Apple TV Settings Screen layout matching Apple Music tvOS Design Specifications.
 *
 * Left Pane: Large Apple Music/BitChord gradient squircle icon + dynamic description text.
 * Right Pane: Grouped pill-shaped settings with solid pure white hover and inverted black text.
 */
@Composable
fun TvSettingsScreen(
    viewModel: MainViewModel,
    onOpenAccountDialog: () -> Unit,
    onOpenRemoteDialog: () -> Unit,
    onOpenDiscordDialog: () -> Unit,
    onOpenScrobbleDialog: () -> Unit,
    onOpenSourcesDialog: () -> Unit,
    onOpenNicknameDialog: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onRunSetupAgain: () -> Unit,
    onOpenAboutDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val signedIn by viewModel.signedIn.collectAsState()
    val account by viewModel.account.collectAsState()

    val tvNickname by AppSettings.tvNickname.collectAsState()
    val tvThemeId by AppSettings.tvTheme.collectAsState()
    val themeMode by AppSettings.themeMode.collectAsState()
    val currentTheme = AppThemeOption.fromId(tvThemeId)

    val crossfadeDuration by AppSettings.crossfadeSeconds.collectAsState()
    val automixEnabled by AppSettings.smartFadeEnabled.collectAsState()
    val syncedLyrics by AppSettings.syncedLyrics.collectAsState()
    val showNerdStats by AppSettings.showNerdStats.collectAsState()
    val discordRpc by AppSettings.discordRpcEnabled.collectAsState()
    val lastfmEnabled by AppSettings.lastfmEnabled.collectAsState()
    val listenBrainzEnabled by AppSettings.listenBrainzEnabled.collectAsState()

    // Dynamic description on the left pane based on current focused setting
    var activeDescription by remember {
        mutableStateOf("Customize your BitChord TV audio engine, appearance, and connected services.")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = TvDimensions.SafeMarginHorizontal),
    ) {
        // Top Center Screen Title: "Music" / "Settings"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = TvSFProDisplay,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = (-0.2).sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(56.dp),
        ) {
            // Left Pane (40% width): Big App Icon Squircle + Description
            Column(
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Vibrant Apple Music / BitChord Red-Pink Gradient Squircle
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .shadow(28.dp, RoundedCornerShape(44.dp), spotColor = Color(0x99FA2D48))
                        .clip(RoundedCornerShape(44.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF375F),
                                    Color(0xFFFA2D48),
                                    Color(0xFFD61435),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logo),
                        contentDescription = "BitChord",
                        tint = Color.White,
                        modifier = Modifier.size(105.dp),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = activeDescription,
                    fontSize = 15.sp,
                    fontFamily = TvSFProDisplay,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth(0.85f),
                )
            }

            // Right Pane (60% width): Apple TV Grouped Settings List
            LazyColumn(
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Section 1: APPEARANCE
                item {
                    TvAppleSettingsSectionHeader(title = "APPEARANCE")
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Appearance Theme",
                        value = when (themeMode) {
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                            ThemeMode.SYSTEM -> "System"
                        },
                        description = "Switch between Apple Music Dark and clean Light appearance modes.",
                        onFocusChanged = { activeDescription = it },
                        onClick = {
                            val next = if (themeMode == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
                            AppSettings.setThemeMode(next)
                        },
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Accent & Color Theme",
                        value = currentTheme.title,
                        hasChevron = true,
                        description = "Choose accent highlights and dynamic artwork background styling.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenThemeDialog,
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "TV Nickname",
                        value = tvNickname,
                        hasChevron = true,
                        description = "Set the personalized greeting name shown on the TV home screen.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenNicknameDialog,
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Setup Assistant",
                        value = "Reconfigure",
                        hasChevron = true,
                        description = "Run the first-launch setup assistant to reset nickname and preferences.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onRunSetupAgain,
                    )
                }

                // Section 2: AUDIO & PLAYBACK
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    TvAppleSettingsSectionHeader(title = "AUDIO & PLAYBACK")
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Stats for Nerds",
                        value = if (showNerdStats) "On" else "Off",
                        description = "Displays real-time audio codec, sample rate, bitrate, and buffer health HUD on the player.",
                        onFocusChanged = { activeDescription = it },
                        onClick = { AppSettings.setShowNerdStats(!showNerdStats) },
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Synchronized Lyrics",
                        value = if (syncedLyrics) "On" else "Off",
                        description = "Displays real-time karaoke synchronized lyrics with singing word highlights.",
                        onFocusChanged = { activeDescription = it },
                        onClick = { AppSettings.setSyncedLyrics(!syncedLyrics) },
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Automix DJ Transitions",
                        value = if (automixEnabled) "On" else "Off",
                        description = "On-device Beat This! ONNX DSP beat-matching and harmonic tempo transitions between tracks.",
                        onFocusChanged = { activeDescription = it },
                        onClick = { AppSettings.setSmartFadeEnabled(!automixEnabled) },
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Crossfade Duration",
                        value = if (crossfadeDuration > 0) "$crossfadeDuration s" else "Off",
                        description = "Fades out the ending song while fading in the next track.",
                        onFocusChanged = { activeDescription = it },
                        onClick = {
                            val next = if (crossfadeDuration >= 12) 0 else crossfadeDuration + 3
                            AppSettings.setCrossfadeSeconds(next)
                        },
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Audio Source Plugins",
                        value = "Manage",
                        hasChevron = true,
                        description = "Configure pluggable FLAC, ALAC, and high-resolution stream modules.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenSourcesDialog,
                    )
                }

                // Section 3: ACCOUNT & INTEGRATIONS
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    TvAppleSettingsSectionHeader(title = "ACCOUNT & INTEGRATIONS")
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Mobile Web Remote",
                        value = "Pair Phone",
                        hasChevron = true,
                        description = "Scan QR code to control playback, search songs, and use the D-pad remote from your mobile browser.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenRemoteDialog,
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "YouTube Music Account",
                        value = if (signedIn && account != null) account?.name ?: "Connected" else "Sign In",
                        hasChevron = true,
                        description = "Scan a QR code with your phone to link your YouTube Music library and playlists.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenAccountDialog,
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Discord Rich Presence",
                        value = if (discordRpc) "On" else "Off",
                        hasChevron = true,
                        description = "Broadcasts your currently playing track, artist, and album art directly to Discord status.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenDiscordDialog,
                    )
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "Scrobbling (Last.fm / ListenBrainz)",
                        value = when {
                            lastfmEnabled && listenBrainzEnabled -> "2 Connected"
                            lastfmEnabled || listenBrainzEnabled -> "1 Connected"
                            else -> "Off"
                        },
                        hasChevron = true,
                        description = "Track your listening history automatically to Last.fm and ListenBrainz profiles.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenScrobbleDialog,
                    )
                }

                // Section 4: ABOUT
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    TvAppleSettingsSectionHeader(title = "ABOUT")
                }
                item {
                    TvAppleSettingsPillRow(
                        title = "BitChord TV",
                        value = "v${BuildConfig.VERSION_NAME}",
                        hasChevron = true,
                        description = "BitChord TV v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • Open-source high fidelity TV audio platform.",
                        onFocusChanged = { activeDescription = it },
                        onClick = onOpenAboutDialog,
                    )
                }
            }
        }
    }
}

@Composable
private fun TvAppleSettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = TvSFProDisplay,
        color = Color.White.copy(alpha = 0.45f),
        letterSpacing = 0.8.sp,
        modifier = modifier.padding(start = 14.dp, top = 8.dp, bottom = 4.dp),
    )
}

/**
 * 1:1 Apple TV Settings Pill Row.
 * Focused: Solid White background with inverted pure Black text.
 * Unfocused: Translucent dark grey background with pure White text.
 */
@Composable
private fun TvAppleSettingsPillRow(
    title: String,
    value: String,
    description: String,
    onFocusChanged: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasChevron: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) onFocusChanged(description)
    }

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.12f),
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "applePillBg",
    )

    val titleColor by animateColorAsState(
        targetValue = if (isFocused) Color.Black else Color.White,
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "applePillTitle",
    )

    val valueColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF444444) else Color.White.copy(alpha = 0.55f),
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "applePillValue",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.02f,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                onClick = onClick,
            )
            .background(bgColor)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = TvSFProDisplay,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = TvSFProDisplay,
                color = valueColor,
            )

            if (hasChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
