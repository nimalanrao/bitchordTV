package com.music.bitchord.ui.tv.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.BuildConfig
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.components.TvDialog
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

@Composable
fun TvAccountDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val signedIn by viewModel.signedIn.collectAsState()
    val account by viewModel.account.collectAsState()

    var cookieText by remember { mutableStateOf("") }

    TvDialog(
        title = if (signedIn) "Account Details" else "YouTube Music Sign In",
        onDismissRequest = onDismiss,
    ) {
        if (signedIn && account != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Name: ${account?.name}",
                    color = TvColors.TextPrimary,
                    fontSize = 16.sp,
                    fontFamily = TvSFProDisplay,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Email: ${account?.email}",
                    color = TvColors.TextSecondary,
                    fontSize = 14.sp,
                    fontFamily = TvSFProDisplay,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TvButton(
                        text = "Sign Out",
                        isPrimary = true,
                        onClick = {
                            viewModel.signOut()
                            onDismiss()
                        },
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your YouTube Music Cookie or Session Token below for personalized library access:",
                    color = TvColors.TextSecondary,
                    fontSize = 14.sp,
                    fontFamily = TvSFProDisplay,
                )

                BasicTextField(
                    value = cookieText,
                    onValueChange = { cookieText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .tvButtonFocus(shape = RoundedCornerShape(10.dp))
                        .background(TvColors.SurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = TvSFProDisplay),
                    cursorBrush = SolidColor(TvColors.AccentRed),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                ) {
                    TvButton(
                        text = "Cancel",
                        onClick = onDismiss,
                    )
                    TvButton(
                        text = "Save & Connect",
                        isPrimary = true,
                        enabled = cookieText.isNotBlank(),
                        onClick = {
                            viewModel.saveAuthCookie(cookieText)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun TvDiscordDialog(
    onDismiss: () -> Unit,
) {
    val discordRpc by AppSettings.discordRpcEnabled.collectAsState()
    val discordToken by AppSettings.discordToken.collectAsState()
    var tokenInput by remember { mutableStateOf(discordToken) }

    TvDialog(
        title = "Discord Rich Presence",
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Show what you're listening to on BitChord in your Discord status.",
                color = TvColors.TextSecondary,
                fontSize = 14.sp,
                fontFamily = TvSFProDisplay,
            )

            BasicTextField(
                value = tokenInput,
                onValueChange = { tokenInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .tvButtonFocus(shape = RoundedCornerShape(10.dp))
                    .background(TvColors.SurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = TvSFProDisplay),
                cursorBrush = SolidColor(TvColors.AccentRed),
                singleLine = true,
                decorationBox = { inner ->
                    if (tokenInput.isEmpty()) {
                        Text("Discord User Token", color = TvColors.TextMuted, fontSize = 14.sp, fontFamily = TvSFProDisplay)
                    }
                    inner()
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            ) {
                TvButton(
                    text = "Close",
                    onClick = onDismiss,
                )
                TvButton(
                    text = if (discordRpc) "Disable RPC" else "Enable RPC",
                    isPrimary = true,
                    onClick = {
                        AppSettings.setDiscordToken(tokenInput)
                        AppSettings.setDiscordRpcEnabled(!discordRpc)
                        onDismiss()
                    },
                )
            }
        }
    }
}

@Composable
fun TvScrobbleDialog(
    onDismiss: () -> Unit,
) {
    val lastfmEnabled by AppSettings.lastfmEnabled.collectAsState()
    val lastfmUser by AppSettings.lastfmUsername.collectAsState()
    val listenBrainzEnabled by AppSettings.listenBrainzEnabled.collectAsState()
    val listenBrainzToken by AppSettings.listenBrainzToken.collectAsState()

    TvDialog(
        title = "Scrobbling Services",
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Last.fm Account: ${if (lastfmEnabled && lastfmUser.isNotBlank()) lastfmUser else "Not connected"}",
                color = TvColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
            )

            Text(
                text = "ListenBrainz: ${if (listenBrainzEnabled && listenBrainzToken.isNotBlank()) "Connected" else "Not connected"}",
                color = TvColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TvButton(
                    text = "Done",
                    isPrimary = true,
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
fun TvSourcesDialog(
    onDismiss: () -> Unit,
) {
    val moduleUrl by AppSettings.moduleIndexUrl.collectAsState()

    TvDialog(
        title = "Hi-Res Stream Module Sources",
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "BitChord uses pluggable JavaScript module sources (QuickJS runtime) for resolving lossless FLAC/ALAC tracks.",
                color = TvColors.TextSecondary,
                fontSize = 14.sp,
                fontFamily = TvSFProDisplay,
            )

            Text(
                text = "Current Index: ${moduleUrl.ifBlank { "Default bundled modules" }}",
                color = TvColors.TextPrimary,
                fontSize = 14.sp,
                fontFamily = TvSFProDisplay,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TvButton(
                    text = "Close",
                    isPrimary = true,
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
fun TvAboutDialog(
    onDismiss: () -> Unit,
) {
    TvDialog(
        title = "About BitChord TV",
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "BitChord TV Edition • Modern YouTube Music Client",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
            )
            Text(
                text = "Version 0.01 (Build 1) • TV Platform & UX by Nyxcore",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = TvSFProDisplay,
                color = TvColors.AccentRed,
            )
            Text(
                text = "TV UI/UX Architecture and Remote-First Implementation engineered by Nyxcore.",
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
            )
            Text(
                text = "Licensed under the GNU General Public License v3.0 (GPLv3). BitChord is an independent third-party client not affiliated with Google or YouTube.",
                fontSize = 13.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TvButton(
                    text = "Close",
                    isPrimary = true,
                    onClick = onDismiss,
                )
            }
        }
    }
}
