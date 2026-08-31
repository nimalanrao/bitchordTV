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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.BuildConfig
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.ThemeMode
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.components.TvAppleCard
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.components.TvSectionHeader
import com.music.bitchord.ui.tv.display.TvRefreshRateController
import com.music.bitchord.ui.tv.display.TvRefreshRatePreference
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.personalization.AppThemeOption
import com.music.bitchord.ui.tv.theme.AppleSpringPreset
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay
import com.music.bitchord.ui.tv.theme.TvThemeColors
import com.music.bitchord.ui.tv.theme.appleSpring

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
    val context = LocalContext.current
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

    val refreshPref by TvRefreshRateController.preference.collectAsState()
    val capabilities by TvRefreshRateController.capabilities.collectAsState()

    val is120HzForced = refreshPref == TvRefreshRatePreference.ULTRA_120

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = TvDimensions.SafeMarginHorizontal,
            end = TvDimensions.SafeMarginHorizontal,
            top = 16.dp,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Page Title
        item {
            Column {
                Text(
                    text = "Settings",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = TvSFProDisplay,
                    color = TvThemeColors.current.textPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personalize your BitChord TV experience",
                    fontSize = 15.sp,
                    fontFamily = TvSFProDisplay,
                    color = TvThemeColors.current.textSecondary,
                )
            }
        }

        // Card 1: Appearance & Theme
        item {
            TvAppleCard(title = "Appearance & Personalization") {
                TvSettingsCardRow(
                    title = "Light / Dark Appearance",
                    subtitle = when (themeMode) {
                        ThemeMode.LIGHT -> "Light Mode active"
                        ThemeMode.DARK -> "Dark Mode active"
                        ThemeMode.SYSTEM -> "System default"
                    },
                    icon = Icons.Default.Palette,
                    onClick = {
                        val next = if (themeMode == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
                        AppSettings.setThemeMode(next)
                    },
                )

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardRow(
                    title = "Accent & Backdrop Theme",
                    subtitle = "${currentTheme.title} • ${currentTheme.description}",
                    icon = Icons.Default.Tune,
                    onClick = onOpenThemeDialog,
                )

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardRow(
                    title = "TV Nickname",
                    subtitle = "Greeting name: \"$tvNickname\"",
                    icon = Icons.Default.Person,
                    onClick = onOpenNicknameDialog,
                )

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardRow(
                    title = "Run Setup Assistant Again",
                    subtitle = "Reconfigure nickname, theme, and first-run defaults",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onRunSetupAgain,
                )
            }
        }

        // Card 2: Display & Motion Engine (Apple ProMotion)
        item {
            TvAppleCard(title = "Display & ProMotion Engine") {
                TvSettingsCardToggleRow(
                    title = "Force 120 Hz Motion",
                    subtitle = if (is120HzForced) "Forced 120 FPS interface rendering active" else "Standard motion",
                    isChecked = is120HzForced,
                    onToggle = {
                        val act = context as? android.app.Activity
                        if (act != null) {
                            val newPref = if (is120HzForced) TvRefreshRatePreference.SYSTEM_AUTO else TvRefreshRatePreference.ULTRA_120
                            TvRefreshRateController.setPreference(act, newPref)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(10.dp))

                val modeSubtitle = when (refreshPref) {
                    TvRefreshRatePreference.ULTRA_120 -> "Ultra 120 Hz (High performance motion)"
                    TvRefreshRatePreference.SMOOTH_60 -> "Smooth 60 Hz (Standard motion)"
                    TvRefreshRatePreference.SYSTEM_AUTO -> "System Auto (${capabilities.actualRefreshRateHz.toInt()} Hz detected)"
                }

                TvSettingsCardRow(
                    title = "Display Refresh Rate Mode",
                    subtitle = modeSubtitle,
                    icon = Icons.Default.Speed,
                    onClick = onOpenRefreshRateDialog,
                )
            }
        }

        // Card 3: Playback & Audio Engine
        item {
            TvAppleCard(title = "Playback & Audio Engine") {
                TvSettingsCardToggleRow(
                    title = "Stats for Nerds",
                    subtitle = "Show real-time codec, bitrate, sample rate, and buffer diagnostics",
                    isChecked = showNerdStats,
                    onToggle = { AppSettings.setShowNerdStats(!showNerdStats) },
                )

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardToggleRow(
                    title = "Synchronized Lyrics",
                    subtitle = "Real-time Apple Music-style lyrics and word highlighting",
                    isChecked = syncedLyrics,
                    onToggle = { AppSettings.setSyncedLyrics(!syncedLyrics) },
                )

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardToggleRow(
                    title = "Automix DJ Transitions",
                    subtitle = "On-device Beat This! ONNX DSP beat-matching & tempo transitions",
                    isChecked = automixEnabled,
                    onToggle = { AppSettings.setSmartFadeEnabled(!automixEnabled) },
                )

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardValueRow(
                    title = "Crossfade Duration",
                    subtitle = if (crossfadeDuration > 0) "$crossfadeDuration seconds" else "Gapless (Off)",
                    onStepDown = {
                        val next = (crossfadeDuration - 1).coerceAtLeast(0)
                        AppSettings.setCrossfadeSeconds(next)
                    },
                    onStepUp = {
                        val next = (crossfadeDuration + 1).coerceAtMost(12)
                        AppSettings.setCrossfadeSeconds(next)
                    },
                )

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardRow(
                    title = "Audio Source Plugins",
                    subtitle = "Manage pluggable FLAC / ALAC stream modules",
                    icon = Icons.Default.Code,
                    onClick = onOpenSourcesDialog,
                )
            }
        }

        // Card 4: Account & Integrations
        item {
            TvAppleCard(title = "Account & Integrations") {
                if (signedIn && account != null) {
                    TvSettingsCardRow(
                        title = account?.name ?: "Signed In",
                        subtitle = account?.email ?: "YouTube Music Connected",
                        icon = Icons.Default.Person,
                        onClick = onOpenAccountDialog,
                    )
                } else {
                    TvSettingsCardRow(
                        title = "Sign In with Phone QR Code",
                        subtitle = "Scan QR code with your phone to login without typing",
                        icon = Icons.Default.Person,
                        onClick = onOpenAccountDialog,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                TvSettingsCardToggleRow(
                    title = "Discord Rich Presence",
                    subtitle = if (discordRpc) "Broadcasting playing track to Discord" else "Disabled",
                    isChecked = discordRpc,
                    onToggle = { onOpenDiscordDialog() },
                )

                Spacer(modifier = Modifier.height(10.dp))

                val scrobbleSub = when {
                    lastfmEnabled && listenBrainzEnabled -> "Last.fm and ListenBrainz connected"
                    lastfmEnabled -> "Last.fm connected"
                    listenBrainzEnabled -> "ListenBrainz connected"
                    else -> "Track your listening history across services"
                }
                TvSettingsCardRow(
                    title = "Scrobbling (Last.fm / ListenBrainz)",
                    subtitle = scrobbleSub,
                    icon = Icons.Default.MusicNote,
                    onClick = onOpenScrobbleDialog,
                )
            }
        }

        // Card 5: About BitChord TV
        item {
            TvAppleCard(title = "About") {
                TvSettingsCardRow(
                    title = "BitChord TV v${BuildConfig.VERSION_NAME}",
                    subtitle = "Build ${BuildConfig.VERSION_CODE} • Open Source Apple-grade TV Audio Experience",
                    icon = Icons.Default.Info,
                    onClick = onOpenAboutDialog,
                )
            }
        }
    }
}

@Composable
private fun TvSettingsCardRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val palette = TvThemeColors.current

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) palette.surfaceFocused else palette.surfaceVariant,
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "settingsCardRowBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(14.dp),
                focusedScale = 1.02f,
                focusedBorderColor = palette.borderFocused,
                onClick = onClick,
            )
            .background(animatedBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isFocused) palette.accentRed else palette.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.White else palette.accentRed,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = palette.textPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = palette.textMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun TvSettingsCardToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val palette = TvThemeColors.current

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) palette.surfaceFocused else palette.surfaceVariant,
        animationSpec = appleSpring(AppleSpringPreset.Snappy),
        label = "settingsToggleBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(14.dp),
                focusedScale = 1.02f,
                focusedBorderColor = palette.borderFocused,
                onClick = onToggle,
            )
            .background(animatedBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = palette.textPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = palette.accentRed,
                uncheckedThumbColor = palette.textMuted,
                uncheckedTrackColor = palette.surface,
            ),
        )
    }
}

@Composable
private fun TvSettingsCardValueRow(
    title: String,
    subtitle: String,
    onStepDown: () -> Unit,
    onStepUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = TvThemeColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = palette.textPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = palette.accentRed,
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
            )

            TvButton(
                text = "+",
                onClick = onStepUp,
            )
        }
    }
}
