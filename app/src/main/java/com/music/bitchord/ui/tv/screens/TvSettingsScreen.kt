package com.music.bitchord.ui.tv.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.BuildConfig
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.components.TvSectionHeader
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

private data class SettingHeroState(
    val title: String,
    val description: String,
    val icon: ImageVector,
)

@Composable
fun TvSettingsScreen(
    viewModel: MainViewModel,
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
    val signedIn by viewModel.signedIn.collectAsState()
    val account by viewModel.account.collectAsState()

    val tvNickname by AppSettings.tvNickname.collectAsState()
    val tvThemeId by AppSettings.tvTheme.collectAsState()
    val currentTheme = com.music.bitchord.ui.tv.personalization.AppThemeOption.fromId(tvThemeId)

    val crossfadeDuration by AppSettings.crossfadeSeconds.collectAsState()
    val automixEnabled by AppSettings.smartFadeEnabled.collectAsState()
    val syncedLyrics by AppSettings.syncedLyrics.collectAsState()
    val showNerdStats by AppSettings.showNerdStats.collectAsState()
    val discordRpc by AppSettings.discordRpcEnabled.collectAsState()
    val lastfmEnabled by AppSettings.lastfmEnabled.collectAsState()
    val listenBrainzEnabled by AppSettings.listenBrainzEnabled.collectAsState()

    val refreshPref by com.music.bitchord.ui.tv.display.TvRefreshRateController.preference.collectAsState()
    val capabilities by com.music.bitchord.ui.tv.display.TvRefreshRateController.capabilities.collectAsState()

    var activeHero by remember {
        mutableStateOf(
            SettingHeroState(
                title = "Settings",
                description = "Configure personalization, playback engine, display rates, and integrations",
                icon = Icons.Default.Tune,
            ),
        )
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = TvDimensions.SafeMarginVertical,
                bottom = 24.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(48.dp),
    ) {
        // Left Pane: Large Dynamic Hero Icon with Opacity & Smooth Crossfade
        Column(
            modifier = Modifier
                .weight(0.32f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Crossfade(
                targetState = activeHero,
                animationSpec = tween(durationMillis = 280),
                label = "settingsHeroCrossfade",
            ) { hero ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = hero.icon,
                            contentDescription = hero.title,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(76.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = hero.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = TvSFProDisplay,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = hero.description,
                        fontSize = 14.sp,
                        fontFamily = TvSFProDisplay,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Right Pane: Settings Rows with High-Contrast Inversion
        LazyColumn(
            modifier = Modifier
                .weight(0.68f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Personalization Section
            item {
                TvSectionHeader(title = "Personalization")
                Spacer(modifier = Modifier.height(6.dp))

                TvSettingsRow(
                    title = "TV Nickname",
                    subtitle = "Greeting name: \"$tvNickname\"",
                    icon = Icons.Default.Person,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "TV Nickname",
                            description = "Change the name shown on your home feed greeting",
                            icon = Icons.Default.Person,
                        )
                    },
                    onClick = onOpenNicknameDialog,
                )

                TvSettingsRow(
                    title = "Visual Theme",
                    subtitle = "${currentTheme.title} • ${currentTheme.description}",
                    icon = Icons.Default.Palette,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Visual Theme",
                            description = "Switch between Dynamic Artwork, Midnight, and Pure Black OLED",
                            icon = Icons.Default.Palette,
                        )
                    },
                    onClick = onOpenThemeDialog,
                )

                TvSettingsRow(
                    title = "Run Setup Assistant Again",
                    subtitle = "Reconfigure nickname, theme, and first-run defaults",
                    icon = Icons.Default.AutoAwesome,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Setup Assistant",
                            description = "Walk through the initial setup wizard to customize BitChord TV",
                            icon = Icons.Default.AutoAwesome,
                        )
                    },
                    onClick = onRunSetupAgain,
                )
            }

            // Account Section
            item {
                TvSectionHeader(title = "Account")
                Spacer(modifier = Modifier.height(6.dp))

                if (signedIn && account != null) {
                    TvSettingsRow(
                        title = account?.name ?: "Signed In",
                        subtitle = account?.email ?: "YouTube Music Connected",
                        icon = Icons.Default.Person,
                        onFocus = {
                            activeHero = SettingHeroState(
                                title = "Account",
                                description = "Manage connected YouTube Music account and sessions",
                                icon = Icons.Default.Person,
                            )
                        },
                        onClick = onOpenAccountDialog,
                    )
                } else {
                    TvSettingsRow(
                        title = "Sign In with Phone QR Code",
                        subtitle = "Scan QR code with your phone to login without typing",
                        icon = Icons.Default.Person,
                        onFocus = {
                            activeHero = SettingHeroState(
                                title = "QR Code Login",
                                description = "Point your phone camera at the TV screen to login instantly",
                                icon = Icons.Default.Person,
                            )
                        },
                        onClick = onOpenAccountDialog,
                    )
                }
            }

            // TV Display & 120Hz Ultra Performance Section
            item {
                TvSectionHeader(title = "Display & Motion Engine")
                Spacer(modifier = Modifier.height(6.dp))

                val modeSubtitle = when (refreshPref) {
                    com.music.bitchord.ui.tv.display.TvRefreshRateMode.HIGH_REFRESH_120HZ -> "120 Hz Ultra-Smooth (8.33ms budget)"
                    com.music.bitchord.ui.tv.display.TvRefreshRateMode.CINEMATIC_MATCH -> "Cinematic Match (Direct panel sync)"
                    com.music.bitchord.ui.tv.display.TvRefreshRateMode.STANDARD_60HZ -> "Standard 60 Hz (16.67ms budget)"
                    com.music.bitchord.ui.tv.display.TvRefreshRateMode.SYSTEM_DEFAULT -> "System Default (${capabilities.currentRefreshRate.toInt()} Hz active)"
                }

                TvSettingsRow(
                    title = "TV Display Refresh Rate",
                    subtitle = modeSubtitle,
                    icon = Icons.Default.Speed,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Display Refresh Rate",
                            description = "Unlock 120 Hz ultra-fluid TV display modes and panel sync",
                            icon = Icons.Default.Speed,
                        )
                    },
                    onClick = onOpenRefreshRateDialog,
                )
            }

            // Integrations Section
            item {
                TvSectionHeader(title = "Integrations & Scrobbling")
                Spacer(modifier = Modifier.height(6.dp))

                TvSettingsRow(
                    title = "Discord Rich Presence",
                    subtitle = if (discordRpc) "Enabled • Broadcasting playing track to Discord" else "Disabled",
                    icon = Icons.Default.GraphicEq,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Discord RPC",
                            description = "Broadcast current playing tracks, artists, and elapsed time to Discord",
                            icon = Icons.Default.GraphicEq,
                        )
                    },
                    onClick = onOpenDiscordDialog,
                )

                val scrobbleSub = when {
                    lastfmEnabled && listenBrainzEnabled -> "Last.fm and ListenBrainz connected"
                    lastfmEnabled -> "Last.fm connected"
                    listenBrainzEnabled -> "ListenBrainz connected"
                    else -> "Track your listening history across services"
                }
                TvSettingsRow(
                    title = "Scrobbling (Last.fm / ListenBrainz)",
                    subtitle = scrobbleSub,
                    icon = Icons.Default.MusicNote,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Music Scrobbling",
                            description = "Sync playback history with Last.fm and ListenBrainz profiles",
                            icon = Icons.Default.MusicNote,
                        )
                    },
                    onClick = onOpenScrobbleDialog,
                )
            }

            // Audio & Playback Engine Section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                TvSectionHeader(title = "Playback & Audio Engine")
                Spacer(modifier = Modifier.height(6.dp))

                TvSettingsToggleRow(
                    title = "Automix DJ Transitions",
                    subtitle = "On-device Beat This! ONNX DSP beat-matching & tempo transitions",
                    isChecked = automixEnabled,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Automix DJ Engine",
                            description = "AI neural network beat-matching and harmonic tempo transitions",
                            icon = Icons.Default.Tune,
                        )
                    },
                    onToggle = { AppSettings.setSmartFadeEnabled(!automixEnabled) },
                )

                TvSettingsValueRow(
                    title = "Crossfade Duration",
                    subtitle = if (crossfadeDuration > 0) "$crossfadeDuration seconds" else "Gapless (Off)",
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Crossfade",
                            description = "Fade between adjacent tracks to eliminate gaps during playback",
                            icon = Icons.Default.Tune,
                        )
                    },
                    onStepDown = {
                        val next = (crossfadeDuration - 1).coerceAtLeast(0)
                        AppSettings.setCrossfadeSeconds(next)
                    },
                    onStepUp = {
                        val next = (crossfadeDuration + 1).coerceAtMost(12)
                        AppSettings.setCrossfadeSeconds(next)
                    },
                )

                TvSettingsToggleRow(
                    title = "Synchronized Lyrics",
                    subtitle = "Real-time word & syllable highlighting across sources",
                    isChecked = syncedLyrics,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Synced Lyrics",
                            description = "Real-time syllable and line synced lyrics with singing glow",
                            icon = Icons.Default.MusicNote,
                        )
                    },
                    onToggle = { AppSettings.setSyncedLyrics(!syncedLyrics) },
                )

                TvSettingsToggleRow(
                    title = "Stats for Nerds HUD",
                    subtitle = "Displays active codec, bitrate, sample rate, and audio resolution",
                    icon = Icons.Default.Info,
                    isChecked = showNerdStats,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Stats for Nerds",
                            description = "View real-time stream diagnostics, codecs, sample rate, and bit depth",
                            icon = Icons.Default.Info,
                        )
                    },
                    onToggle = { AppSettings.setShowNerdStats(!showNerdStats) },
                )

                TvSettingsRow(
                    title = "Hi-Res Audio Source Plugins",
                    subtitle = "Manage pluggable FLAC / ALAC stream modules",
                    icon = Icons.Default.Code,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "Audio Sources",
                            description = "Configure pluggable lossless FLAC & ALAC audio resolution modules",
                            icon = Icons.Default.Code,
                        )
                    },
                    onClick = onOpenSourcesDialog,
                )
            }

            // App Info & Licenses Section
            item {
                TvSectionHeader(title = "About BitChord TV")
                Spacer(modifier = Modifier.height(6.dp))

                TvSettingsRow(
                    title = "BitChord TV v${BuildConfig.VERSION_NAME}",
                    subtitle = "Build ${BuildConfig.VERSION_CODE} • Open Source Material You & TV UI",
                    icon = Icons.Default.Info,
                    onFocus = {
                        activeHero = SettingHeroState(
                            title = "About BitChord TV",
                            description = "Crafted with love for Android TV and Google TV",
                            icon = Icons.Default.Info,
                        )
                    },
                    onClick = onOpenAboutDialog,
                )
            }
        }
    }
}

/**
 * Settings Row with high-contrast sharp focus inversion (White background, Black text & icon on hover).
 */
@Composable
private fun TvSettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    androidx.compose.runtime.LaunchedEffect(isFocused) {
        if (isFocused) onFocus()
    }

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) Color.White else TvColors.SurfaceVariant,
        label = "settingsRowBg",
    )

    val textColor = if (isFocused) Color.Black else TvColors.TextPrimary
    val subtextColor = if (isFocused) Color(0xFF3C3C43).copy(alpha = 0.85f) else TvColors.TextSecondary
    val iconColor = if (isFocused) Color.Black else Color.White
    val iconBgColor = if (isFocused) Color(0xFFE5E5EA) else TvColors.Surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(14.dp),
                focusedScale = 1.02f,
                focusedBorderColor = Color.White,
                onClick = onClick,
            )
            .background(animatedBg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = textColor,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = subtextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isFocused) Color(0xFF666666) else TvColors.TextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Settings Toggle Row with high-contrast sharp focus inversion.
 */
@Composable
private fun TvSettingsToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onFocus: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    androidx.compose.runtime.LaunchedEffect(isFocused) {
        if (isFocused) onFocus()
    }

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) Color.White else TvColors.SurfaceVariant,
        label = "settingsToggleBg",
    )

    val textColor = if (isFocused) Color.Black else TvColors.TextPrimary
    val subtextColor = if (isFocused) Color(0xFF3C3C43).copy(alpha = 0.85f) else TvColors.TextSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(14.dp),
                focusedScale = 1.02f,
                focusedBorderColor = Color.White,
                onClick = onToggle,
            )
            .background(animatedBg)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (icon != null) {
            val iconBg = if (isFocused) Color(0xFFE5E5EA) else TvColors.Surface
            val iconTint = if (isFocused) Color.Black else Color.White
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = textColor,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = subtextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isFocused) Color.Black else Color.White,
                checkedTrackColor = if (isFocused) Color(0xFFE5E5EA) else Color.White.copy(alpha = 0.4f),
                uncheckedThumbColor = if (isFocused) Color.Gray else TvColors.TextMuted,
                uncheckedTrackColor = if (isFocused) Color(0xFFE5E5EA) else TvColors.Surface,
            ),
        )
    }
}

/**
 * Settings Value Row with high-contrast sharp focus inversion.
 */
@Composable
private fun TvSettingsValueRow(
    title: String,
    subtitle: String,
    onStepDown: () -> Unit,
    onStepUp: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    androidx.compose.runtime.LaunchedEffect(isFocused) {
        if (isFocused) onFocus()
    }

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) Color.White else TvColors.SurfaceVariant,
        label = "settingsValueBg",
    )

    val textColor = if (isFocused) Color.Black else TvColors.TextPrimary
    val subtextColor = if (isFocused) Color(0xFF3C3C43).copy(alpha = 0.85f) else TvColors.TextSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(animatedBg)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = textColor,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = subtextColor,
                fontWeight = FontWeight.Medium,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButton(
                text = "-",
                onClick = onStepDown,
                isFocusedDefault = false,
            )

            TvButton(
                text = "+",
                onClick = onStepUp,
                isFocusedDefault = false,
            )
        }
    }
}
