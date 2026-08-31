package com.music.bitchord.ui.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import com.music.bitchord.R

object TvColors {
    // Apple Music signature vibrant accent
    val AccentRed = Color(0xFFFA2D48)
    val AccentRedGlow = Color(0x66FA2D48)
    val AccentPink = Color(0xFFFF375F)
    val AccentPurple = Color(0xFFBF5AF2)
    val AccentBlue = Color(0xFF0A84FF)

    // Deep luxury dark canvas & surfaces for TV OLED/LED
    val Background = Color(0xFF08080B)
    val BackgroundElevated = Color(0xFF101015)
    val Surface = Color(0xFF17171E)
    val SurfaceVariant = Color(0xFF22222C)
    val SurfaceFocused = Color(0xFF323242)
    val SurfaceSelected = Color(0xFF38151D)

    // Text hierarchy optimized for 10-foot viewing distance
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA0A0AB)
    val TextMuted = Color(0xFF6E6E7A)

    // Borders & Focus indicator tokens
    val BorderSubtle = Color(0xFF282834)
    val BorderFocused = Color(0xFFFA2D48)
    val BorderFocusedWhite = Color(0xFFFFFFFF)

    // Overlays & Scrims
    val ScrimDark = Color(0xCC000000)
    val GlassOverlay = Color(0x3320202E)
    val GlassOverlayFocused = Color(0x66FA2D48)

    // Dynamic TV backdrop gradient
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF150D18),
            Color(0xFF0A090D),
            Color(0xFF050507),
        )
    )
}

val TvSFProDisplay = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.W400),
    Font(R.font.sf_pro_display_medium, FontWeight.W500),
    Font(R.font.sf_pro_display_semibold, FontWeight.W600),
    Font(R.font.sf_pro_display_bold, FontWeight.W700),
    Font(R.font.sf_pro_display_heavy, FontWeight.W800),
)

// 10-foot TV typography scale
private val TvTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W800,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.8).sp,
        color = TvColors.TextPrimary,
    ),
    displayMedium = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W800,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.6).sp,
        color = TvColors.TextPrimary,
    ),
    displaySmall = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp,
        color = TvColors.TextPrimary,
    ),
    headlineLarge = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
        color = TvColors.TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W600,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
        color = TvColors.TextPrimary,
    ),
    headlineSmall = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TvColors.TextPrimary,
    ),
    titleLarge = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TvColors.TextPrimary,
    ),
    titleMedium = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TvColors.TextPrimary,
    ),
    titleSmall = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TvColors.TextSecondary,
    ),
    bodyLarge = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TvColors.TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TvColors.TextSecondary,
    ),
    bodySmall = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TvColors.TextMuted,
    ),
    labelLarge = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W600,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TvColors.TextPrimary,
    ),
    labelMedium = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TvColors.TextSecondary,
    ),
    labelSmall = TextStyle(
        fontFamily = TvSFProDisplay,
        fontWeight = FontWeight.W600,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = TvColors.TextMuted,
    ),
)

val TvColorScheme: ColorScheme = darkColorScheme(
    primary = TvColors.AccentRed,
    onPrimary = Color.White,
    primaryContainer = TvColors.SurfaceSelected,
    onPrimaryContainer = Color.White,
    secondary = TvColors.AccentPink,
    onSecondary = Color.White,
    background = TvColors.Background,
    onBackground = TvColors.TextPrimary,
    surface = TvColors.Surface,
    onSurface = TvColors.TextPrimary,
    surfaceVariant = TvColors.SurfaceVariant,
    onSurfaceVariant = TvColors.TextSecondary,
    border = TvColors.BorderSubtle,
)

object TvDimensions {
    val SafeMarginHorizontal = 48.dp
    val SafeMarginVertical = 27.dp
    val CardSpacing = 18.dp
    val ShelfSpacing = 28.dp
    val NavigationRailWidth = 76.dp
    val NavigationRailExpandedWidth = 220.dp
}

@Composable
fun BitChordTvTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TvColorScheme,
        typography = TvTypography,
        content = content,
    )
}
