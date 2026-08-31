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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
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
    onOpenAboutDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val signedIn by viewModel.signedIn.collectAsState()
    val account by viewModel.account.collectAsState()

    val crossfadeDuration by AppSettings.crossfadeDurationSeconds.collectAsState()
    val automixEnabled by AppSettings.automix.collectAsState()
    val syncedLyrics by AppSettings.syncedLyrics.collectAsState()
    val syllableSync by AppSettings.prioritizeSyllableSync.collectAsState()
    val discordRpc by AppSettings.discordRpcEnabled.collectAsState()
    val lastfmEnabled by AppSettings.lastfmEnabled.collectAsState()
    val listenBrainzEnabled by AppSettings.listenBrainzEnabled.collectAsState()

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
        // Account & Connectivity Section
        item {
            TvSectionHeader(title = "Account & Services")
            Spacer(modifier = Modifier.height(6.dp))

            TvSettingsRow(
                title = if (signedIn) account?.name ?: "Signed In" else "Sign in with Google",
                subtitle = if (signedIn) account?.email ?: "Personalized library connected" else "Connect your YouTube Music account",
                icon = Icons.Default.Person,
                onClick = onOpenAccountDialog,
            )

            TvSettingsRow(
                title = "Discord Rich Presence",
                subtitle = if (discordRpc) "Broadcasting music status to Discord" else "Off",
                icon = Icons.Default.Tune,
                onClick = onOpenDiscordDialog,
            )

            TvSettingsRow(
                title = "Last.fm & ListenBrainz Scrobbling",
                subtitle = when {
                    lastfmEnabled && listenBrainzEnabled -> "Last.fm & ListenBrainz Active"
                    lastfmEnabled -> "Last.fm Active"
                    listenBrainzEnabled -> "ListenBrainz Active"
                    else -> "Not Connected"
                },
                icon = Icons.Default.GraphicEq,
                onClick = onOpenScrobbleDialog,
            )
        }

        // Playback & Audio Engine Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            TvSectionHeader(title = "Playback & Audio Engine")
            Spacer(modifier = Modifier.height(6.dp))

            TvSettingsToggleRow(
                title = "Automix DJ Transitions",
                subtitle = "On-device Beat This! ONNX DSP beat-matching & tempo transitions",
                isChecked = automixEnabled,
                onToggle = { AppSettings.setAutomix(!automixEnabled) },
            )

            TvSettingsValueRow(
                title = "Crossfade Duration",
                subtitle = if (crossfadeDuration > 0) "$crossfadeDuration seconds" else "Gapless (Off)",
                onStepDown = {
                    val next = (crossfadeDuration - 1).coerceAtLeast(0)
                    AppSettings.setCrossfadeDurationSeconds(next)
                },
                onStepUp = {
                    val next = (crossfadeDuration + 1).coerceAtMost(12)
                    AppSettings.setCrossfadeDurationSeconds(next)
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
            Spacer(modifier = Modifier.height(12.dp))
            TvSectionHeader(title = "About BitChord TV")
            Spacer(modifier = Modifier.height(6.dp))

            TvSettingsRow(
                title = "BitChord TV Edition",
                subtitle = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • GNU GPLv3",
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

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) TvColors.SurfaceFocused else TvColors.Surface,
        label = "tvSettingsRowBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.02f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onClick,
            )
            .background(bgColor)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TvColors.SurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) TvColors.AccentRed else TvColors.TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                fontFamily = TvSFProDisplay,
                color = if (isFocused) TvColors.AccentRed else TvColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TvColors.TextSecondary,
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

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) TvColors.SurfaceFocused else TvColors.Surface,
        label = "tvToggleBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.02f,
                focusedBorderColor = TvColors.BorderFocused,
                onClick = onToggle,
            )
            .background(bgColor)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                fontFamily = TvSFProDisplay,
                color = if (isFocused) TvColors.AccentRed else TvColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (isChecked) TvColors.AccentRed else TvColors.SurfaceVariant)
                .padding(3.dp),
            contentAlignment = if (isChecked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
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

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) TvColors.SurfaceFocused else TvColors.Surface,
        label = "tvValueRowBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .tvButtonFocus(
                shape = RoundedCornerShape(12.dp),
                focusedScale = 1.02f,
                focusedBorderColor = TvColors.BorderFocused,
            )
            .background(bgColor)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                fontFamily = TvSFProDisplay,
                color = if (isFocused) TvColors.AccentRed else TvColors.TextPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TvButton(
                text = "-",
                onClick = onStepDown,
                modifier = Modifier.size(36.dp),
            )
            TvButton(
                text = "+",
                onClick = onStepUp,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
