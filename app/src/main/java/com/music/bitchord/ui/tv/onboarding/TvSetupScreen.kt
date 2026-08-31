package com.music.bitchord.ui.tv.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.keyboard.TvKeyboard
import com.music.bitchord.ui.tv.personalization.AppThemeOption
import com.music.bitchord.ui.tv.personalization.NicknamePolicy
import com.music.bitchord.ui.tv.personalization.NicknameValidationResult
import com.music.bitchord.ui.tv.personalization.getPalette
import com.music.bitchord.ui.tv.theme.TvColors
import com.music.bitchord.ui.tv.theme.TvDimensions
import com.music.bitchord.ui.tv.theme.TvSFProDisplay

enum class SetupStep {
    WELCOME,
    NICKNAME,
    THEME,
    COMPLETE
}

@Composable
fun TvSetupScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentStep by remember { mutableStateOf(SetupStep.WELCOME) }
    var draftNickname by remember { mutableStateOf(AppSettings.tvNickname.value.ifBlank { "Listener" }) }
    var cursorIndex by remember { mutableIntStateOf(draftNickname.length) }
    var draftTheme by remember { mutableStateOf(AppThemeOption.fromId(AppSettings.tvTheme.value)) }

    val activePalette = draftTheme.getPalette()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(activePalette.background)
            .padding(
                start = TvDimensions.SafeMarginHorizontal,
                end = TvDimensions.SafeMarginHorizontal,
                top = TvDimensions.SafeMarginVertical,
                bottom = TvDimensions.SafeMarginVertical,
            )
            .onTvKeyEvent(
                onBack = {
                    when (currentStep) {
                        SetupStep.WELCOME -> false
                        SetupStep.NICKNAME -> { currentStep = SetupStep.WELCOME; true }
                        SetupStep.THEME -> { currentStep = SetupStep.NICKNAME; true }
                        SetupStep.COMPLETE -> { currentStep = SetupStep.THEME; true }
                    }
                },
            ),
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { it / 2 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 2 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 2 } + fadeIn()).togetherWith(slideOutHorizontally { it / 2 } + fadeOut())
                }
            },
            label = "setupStepTransition",
            modifier = Modifier.fillMaxSize(),
        ) { step ->
            when (step) {
                SetupStep.WELCOME -> {
                    TvWelcomeStep(
                        onStartSetup = { currentStep = SetupStep.NICKNAME },
                        onUseDefaults = {
                            AppSettings.setTvPersonalization(
                                nickname = NicknamePolicy.DEFAULT_NICKNAME,
                                themeId = AppThemeOption.DYNAMIC_ARTWORK.id,
                                version = 1,
                            )
                            onComplete()
                        },
                    )
                }
                SetupStep.NICKNAME -> {
                    TvNicknameStep(
                        nickname = draftNickname,
                        cursorIndex = cursorIndex,
                        onNicknameChange = { newNick, newCursor ->
                            draftNickname = newNick
                            cursorIndex = newCursor
                        },
                        onContinue = { currentStep = SetupStep.THEME },
                        onSkip = {
                            draftNickname = NicknamePolicy.DEFAULT_NICKNAME
                            currentStep = SetupStep.THEME
                        },
                    )
                }
                SetupStep.THEME -> {
                    TvThemeStep(
                        selectedTheme = draftTheme,
                        onThemeSelect = { draftTheme = it },
                        onContinue = { currentStep = SetupStep.COMPLETE },
                    )
                }
                SetupStep.COMPLETE -> {
                    TvSetupCompleteStep(
                        nickname = draftNickname,
                        theme = draftTheme,
                        onFinish = {
                            AppSettings.setTvPersonalization(
                                nickname = draftNickname.ifBlank { NicknamePolicy.DEFAULT_NICKNAME },
                                themeId = draftTheme.id,
                                version = 1,
                            )
                            onComplete()
                        },
                        onChangeChoices = { currentStep = SetupStep.NICKNAME },
                    )
                }
            }
        }
    }
}

@Composable
private fun TvWelcomeStep(
    onStartSetup: () -> Unit,
    onUseDefaults: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left Column: Branding & Value Proposition
        Column(
            modifier = Modifier.weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvColors.AccentRed),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }

            Text(
                text = "Make BitChord Yours",
                fontSize = 38.sp,
                fontWeight = FontWeight.W800,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
            )

            Text(
                text = "Personalize your living room music client with custom visual themes and a nickname greeting for this TV.",
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextSecondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvButton(
                    text = "Start Setup",
                    icon = Icons.Default.ArrowForward,
                    isPrimary = true,
                    onClick = onStartSetup,
                )
                TvButton(
                    text = "Use Defaults",
                    isPrimary = false,
                    onClick = onUseDefaults,
                )
            }
        }

        // Right Column: Feature Preview Card
        Box(
            modifier = Modifier
                .weight(0.45f)
                .clip(RoundedCornerShape(24.dp))
                .background(TvColors.SurfaceVariant)
                .padding(28.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "SETUP HIGHLIGHTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TvColors.AccentRed,
                    fontFamily = TvSFProDisplay,
                )

                TvFeatureBullet(icon = Icons.Default.Person, title = "Local Nickname", desc = "Personalized greetings on your home screen")
                TvFeatureBullet(icon = Icons.Default.Palette, title = "Cinematic Themes", desc = "Dynamic Artwork, Midnight, and OLED Pure Black")
                TvFeatureBullet(icon = Icons.Default.Headphones, title = "Lossless & Automix", desc = "Hi-Res FLAC streaming & DJ beat transitions")
            }
        }
    }
}

@Composable
private fun TvFeatureBullet(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TvColors.SurfaceFocused),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TvColors.AccentRed, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TvColors.TextPrimary, fontFamily = TvSFProDisplay)
            Text(text = desc, fontSize = 12.sp, color = TvColors.TextSecondary, fontFamily = TvSFProDisplay)
        }
    }
}

@Composable
private fun TvNicknameStep(
    nickname: String,
    cursorIndex: Int,
    onNicknameChange: (String, Int) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val validation = NicknamePolicy.validate(nickname)
    val isValid = validation is NicknameValidationResult.Valid
    val graphemeCount = NicknamePolicy.getGraphemeCount(nickname)

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left Column: Prompt, Display Box, and Actions
        Column(
            modifier = Modifier.weight(0.42f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "What should BitChord call you?",
                fontSize = 28.sp,
                fontWeight = FontWeight.W800,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
            )

            Text(
                text = "Used for greetings on this TV. Stored locally on this device.",
                fontSize = 14.sp,
                color = TvColors.TextSecondary,
                fontFamily = TvSFProDisplay,
            )

            // Nickname Display Box with Cursor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvColors.SurfaceVariant)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = nickname.ifEmpty { "Enter your name..." },
                        color = if (nickname.isEmpty()) TvColors.TextMuted else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = TvSFProDisplay,
                        maxLines = 1,
                    )
                    Text(
                        text = "$graphemeCount/${NicknamePolicy.MAX_GRAPHEMES}",
                        color = TvColors.TextMuted,
                        fontSize = 12.sp,
                        fontFamily = TvSFProDisplay,
                    )
                }
            }

            if (validation is NicknameValidationResult.Invalid) {
                Text(
                    text = validation.reason,
                    color = TvColors.AccentRed,
                    fontSize = 12.sp,
                    fontFamily = TvSFProDisplay,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvButton(
                    text = "Continue",
                    isPrimary = true,
                    enabled = isValid,
                    onClick = onContinue,
                )
                TvButton(
                    text = "Skip",
                    onClick = onSkip,
                )
            }
        }

        // Right Column: Virtual Built-in TV Keyboard
        Box(
            modifier = Modifier.weight(0.58f),
        ) {
            TvKeyboard(
                text = nickname,
                cursorIndex = cursorIndex,
                onTextChange = onNicknameChange,
                onDone = { if (isValid) onContinue() },
                onOpenSystemIme = { /* Fallback to system IME */ },
            )
        }
    }
}

@Composable
private fun TvThemeStep(
    selectedTheme: AppThemeOption,
    onThemeSelect: (AppThemeOption) -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Choose Your Look",
                fontSize = 32.sp,
                fontWeight = FontWeight.W800,
                fontFamily = TvSFProDisplay,
                color = TvColors.TextPrimary,
            )
            Text(
                text = "Select a visual aesthetic for BitChord TV. You can change this anytime in Settings.",
                fontSize = 15.sp,
                color = TvColors.TextSecondary,
                fontFamily = TvSFProDisplay,
            )
        }

        // 3 Large Horizontal Theme Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(AppThemeOption.entries.toTypedArray()) { theme ->
                val isSelected = theme == selectedTheme
                val palette = theme.getPalette()

                Box(
                    modifier = Modifier
                        .size(width = 250.dp, height = 220.dp)
                        .tvButtonFocus(
                            shape = RoundedCornerShape(20.dp),
                            focusedScale = 1.05f,
                            focusedBorderColor = TvColors.BorderFocused,
                            unfocusedBorderColor = if (isSelected) palette.accent else Color.Transparent,
                            onClick = { onThemeSelect(theme) },
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.surface)
                        .padding(18.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Miniature UI Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.background)
                                .padding(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.accent),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Box(modifier = Modifier.size(width = 70.dp, height = 10.dp).clip(RoundedCornerShape(4.dp)).background(palette.textPrimary))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(modifier = Modifier.size(width = 45.dp, height = 8.dp).clip(RoundedCornerShape(4.dp)).background(palette.textSecondary))
                                }
                            }
                        }

                        // Theme Info
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = theme.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    fontFamily = TvSFProDisplay,
                                )
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = palette.accent, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = theme.description,
                                fontSize = 11.sp,
                                color = palette.textSecondary,
                                fontFamily = TvSFProDisplay,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp,
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TvButton(
                text = "Continue",
                isPrimary = true,
                onClick = onContinue,
            )
        }
    }
}

@Composable
private fun TvSetupCompleteStep(
    nickname: String,
    theme: AppThemeOption,
    onFinish: () -> Unit,
    onChangeChoices: () -> Unit,
) {
    val palette = theme.getPalette()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(palette.accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "You're all set, $nickname!",
            fontSize = 36.sp,
            fontWeight = FontWeight.W800,
            fontFamily = TvSFProDisplay,
            color = palette.textPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Theme: ${theme.title} • Settings and personalization can be updated anytime.",
            fontSize = 16.sp,
            color = palette.textSecondary,
            fontFamily = TvSFProDisplay,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TvButton(
                text = "Start Listening",
                isPrimary = true,
                onClick = onFinish,
            )
            TvButton(
                text = "Change Choices",
                isPrimary = false,
                onClick = onChangeChoices,
            )
        }
    }
}
