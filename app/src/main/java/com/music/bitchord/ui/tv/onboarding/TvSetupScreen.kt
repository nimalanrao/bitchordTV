package com.music.bitchord.ui.tv.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.tv.components.TvButton
import com.music.bitchord.ui.tv.focus.onTvKeyEvent
import com.music.bitchord.ui.tv.focus.tvButtonFocus
import com.music.bitchord.ui.tv.personalization.AppThemeOption
import com.music.bitchord.ui.tv.personalization.NicknamePolicy
import com.music.bitchord.ui.tv.personalization.getPalette
import com.music.bitchord.ui.tv.theme.LocalTvFontFamily
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
    var draftNickname by remember { mutableStateOf(AppSettings.tvNickname.value.ifBlank { "Living Room TV" }) }
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
                        onNicknameSelect = { draftNickname = it },
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
    val currentFont = LocalTvFontFamily.current

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
            // BitChord Logo Badge (No pink, sleek white icon)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "BitChord Logo",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }

            Text(
                text = "Welcome to BitChord TV",
                fontSize = 38.sp,
                fontWeight = FontWeight.W800,
                fontFamily = currentFont,
                color = Color.White,
            )

            Text(
                text = "Experience YouTube Music crafted exclusively for television screens with 120Hz smooth animations, live video canvas, and spatial audio.",
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontFamily = currentFont,
                color = Color.White.copy(alpha = 0.70f),
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
                .background(Color.White.copy(alpha = 0.08f))
                .padding(28.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "SETUP HIGHLIGHTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.60f),
                    fontFamily = currentFont,
                )

                TvFeatureBullet(icon = Icons.Default.Person, title = "TV Nickname", desc = "Personalized greetings on your home screen")
                TvFeatureBullet(icon = Icons.Default.Palette, title = "Visual Themes", desc = "Dynamic Artwork, Midnight, and OLED Pure Black")
                TvFeatureBullet(icon = Icons.Default.Headphones, title = "Spatial Audio", desc = "3D Virtualizer soundstage and lossless streams")
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
    val currentFont = LocalTvFontFamily.current

    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = currentFont)
            Text(text = desc, fontSize = 12.sp, color = Color.White.copy(alpha = 0.60f), fontFamily = currentFont)
        }
    }
}

@Composable
private fun TvNicknameStep(
    nickname: String,
    onNicknameSelect: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val currentFont = LocalTvFontFamily.current
    val presets = listOf("Living Room TV", "Bedroom TV", "Studio TV", "Family Room", "Theater", "Listener")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Choose TV Nickname",
                fontSize = 32.sp,
                fontWeight = FontWeight.W800,
                fontFamily = currentFont,
                color = Color.White,
            )
            Text(
                text = "Select a name for this television. Used for friendly home screen greetings.",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.70f),
                fontFamily = currentFont,
            )
        }

        // Preset Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(presets) { preset ->
                val isSelected = preset == nickname
                Box(
                    modifier = Modifier
                        .tvButtonFocus(
                            shape = RoundedCornerShape(20.dp),
                            focusedScale = 1.06f,
                            focusedBorderColor = Color.White,
                            onClick = { onNicknameSelect(preset) },
                        )
                        .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = preset,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = currentFont,
                        color = if (isSelected) Color.Black else Color.White,
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TvButton(
                text = "Continue",
                isPrimary = true,
                onClick = onContinue,
            )
            TvButton(
                text = "Skip",
                onClick = onSkip,
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
    val currentFont = LocalTvFontFamily.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Choose Your Look",
                fontSize = 32.sp,
                fontWeight = FontWeight.W800,
                fontFamily = currentFont,
                color = Color.White,
            )
            Text(
                text = "Select a visual aesthetic for BitChord TV. You can change this anytime in Settings.",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.70f),
                fontFamily = currentFont,
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(AppThemeOption.entries.toTypedArray()) { theme ->
                val isSelected = theme == selectedTheme
                val palette = theme.getPalette()

                Box(
                    modifier = Modifier
                        .size(width = 250.dp, height = 200.dp)
                        .tvButtonFocus(
                            shape = RoundedCornerShape(20.dp),
                            focusedScale = 1.05f,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = if (isSelected) Color.White else Color.Transparent,
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
                        Column {
                            Text(
                                text = theme.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = currentFont,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.description,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                fontFamily = currentFont,
                            )
                        }

                        if (isSelected) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text(text = "Selected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = currentFont)
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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
    val currentFont = LocalTvFontFamily.current

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Text(
                text = "You're All Set!",
                fontSize = 38.sp,
                fontWeight = FontWeight.W800,
                fontFamily = currentFont,
                color = Color.White,
            )

            Text(
                text = "BitChord TV is ready for \"$nickname\" with the ${theme.title} theme.",
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontFamily = currentFont,
                color = Color.White.copy(alpha = 0.70f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvButton(
                    text = "Start Listening",
                    icon = Icons.Default.PlayArrow,
                    isPrimary = true,
                    onClick = onFinish,
                )
                TvButton(
                    text = "Change",
                    onClick = onChangeChoices,
                )
            }
        }
    }
}
