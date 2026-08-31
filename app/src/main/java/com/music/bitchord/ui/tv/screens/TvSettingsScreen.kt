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
import androidx.compose.material.icons.filled.PowerSettingsNew
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
    val syllableSync by AppSettings.prioritizeSyllableSync.collectAsState()
    val discordRpc by AppSettings.discordRpcEnabled.collectAsState()
    val lastfmEnabled by AppSettings.lastfmEnabled.collectAsState()
    val listenBrainzEnabled by AppSettings.listenBrainzEnabled.collectAsState()

    val refreshPref by com.music.bitchord.ui.tv.display.TvRefreshRateController.preference.collectAsState()
    val capabilities by com.music.bitchord.ui.tv.display.TvRefreshRateController.capabilities.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = TvDimensions.SafeMarginHorizontal,
            end = TvDimensions.SafeMarginHorizontal,
            top = TvDimensions.SafeMarginVertical,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Personalization Section
        item {
            TvSectionHeader(title = "Personalization")
            Spacer(modifier = Modifier.height(6.dp))

            TvSettingsRow(
                title = "TV Nickname",
                subtitle = "Greeting name: \"$tvNickname\"",
                icon = Icons.Default.Person,
                onClick = onOpenNicknameDialog,
            )

            TvSettingsRow(
                title = "Visual Theme",
                subtitle = "${currentTheme.title} • ${currentTheme.description}",
                icon = Icons.Default.Palette,
                onClick = onOpenThemeDialog,
            )

            TvSettingsRow(
                title = "Run Setup Assistant Again",
                subtitle = "Reconfigure nickname, theme, and first-run defaults",
                icon = Icons.Default.AutoAwesome,
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
                    onClick = onOpenAccountDialog,
                )
            } else {
                TvSettingsRow(
                    title = "Sign In with Phone QR Code",
                    subtitle = "Scan QR code with your phone to login without typing",
                    icon = Icons.Default.Person,
                    onClick = onOpenAccountDialog,
                )
            }
        }

        // TV Display & 120Hz Ultra Performance Section
        item {
            TvSectionHeader(title = "Display & Motion Engine")
            Spacer(modifier = Modifier.height(6.dp))

            val modeSubtitle = when (refreshPref) {
                com.music.bitchord.ui.tv.display.TvRefreshRatePreference.ULTRA_120 -> "Ultra 120 Hz (High performance motion)"
                com.music.bitchord.ui.tv.display.TvRefreshRatePreference.SMOOTH_60 -> "Smooth 60 Hz (Standard motion)"
                com.music.bitchord.ui.tv.display.TvRefreshRatePreference.SYSTEM_AUTO -> "System Auto (${capabilities.actualRefreshRateHz.toInt()} Hz active)"
            }

            TvSettingsRow(
                title = "TV Display Refresh Rate",
                subtitle = modeSubtitle,
                icon = Icons.Default.Speed,
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
                onToggle = { AppSettings.setSmartFadeEnabled(!automixEnabled) },
            )

            TvSettingsValueRow(
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

            TvSettingsToggleRow(
                title = "Synchronized Lyrics",
                subtitle = "Real-time word & syllable highlighting across sources",
                isChecked = syncedLyrics,
                onToggle = { AppSettings.setSyncedLyrics(!syncedLyrics) },
            )

            TvSettingsRow(
                title = "Hi-Res Audio Source Plugins",
                subtitle = "Manage pluggable FLAC / ALAC stream modules",
                icon = Icons.Default.Code,
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
                onClick = onOpenAboutDialog,
            )
        }
    }
}

@Composable
private fun TvSettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) TvColors.SurfaceFocused else TvColors.SurfaceVariant,
        label = "settingsRowBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(14.dp),
                focusedScale = 1.02f,
                focusedBorderColor = TvColors.BorderFocused,
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
                .background(if (isFocused) TvColors.AccentRed else TvColors.Surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TvColors.TextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun TvSettingsToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) TvColors.SurfaceFocused else TvColors.SurfaceVariant,
        label = "settingsToggleBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .tvButtonFocus(
                shape = RoundedCornerShape(14.dp),
                focusedScale = 1.02f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onToggle,
            )
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
                color = TvColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TvColors.AccentRed,
                uncheckedThumbColor = TvColors.TextMuted,
                uncheckedTrackColor = TvColors.Surface,
            ),
        )
    }
}

@Composable
private fun TvSettingsValueRow(
    title: String,
    subtitle: String,
    onStepDown: () -> Unit,
    onStepUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) TvColors.SurfaceFocused else TvColors.SurfaceVariant,
        label = "settingsValueBg",
    )

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
                color = TvColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.AccentRed,
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
