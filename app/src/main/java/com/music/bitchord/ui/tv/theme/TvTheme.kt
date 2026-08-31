package com.music.bitchord.ui.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
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
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
// Immutable palette data class — one instance per theme variant
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class TvColorPalette(
    // Accents
    val accentRed: Color,
    val accentRedGlow: Color,
    val accentPink: Color,
    val accentPurple: Color,
    val accentBlue: Color,

    // Canvas & surfaces
    val background: Color,
    val backgroundElevated: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceFocused: Color,
    val surfaceSelected: Color,

    // Text hierarchy
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,

    // Borders & focus
    val borderSubtle: Color,
    val borderFocused: Color,
    val borderFocusedWhite: Color,

    // Overlays & scrims
    val scrimDark: Color,
    val glassOverlay: Color,
    val glassOverlayFocused: Color,

    // Background gradient
    val backgroundGradient: Brush,

    // Whether this is a dark palette
    val isDark: Boolean,
)

// ─────────────────────────────────────────────────────────────────────────────
// Dark Palette (default TV OLED/LED luxury dark canvas)
// ─────────────────────────────────────────────────────────────────────────────

val TvDarkPalette = TvColorPalette(
    accentRed = Color(0xFFFA2D48),
    accentRedGlow = Color(0x66FA2D48),
    accentPink = Color(0xFFFF375F),
    accentPurple = Color(0xFFBF5AF2),
    accentBlue = Color(0xFF0A84FF),

    background = Color(0xFF08080B),
    backgroundElevated = Color(0xFF101015),
    surface = Color(0xFF17171E),
    surfaceVariant = Color(0xFF22222C),
    surfaceFocused = Color(0xFF323242),
    surfaceSelected = Color(0xFF38151D),

    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA0A0AB),
    textMuted = Color(0xFF6E6E7A),

    borderSubtle = Color(0xFF282834),
    borderFocused = Color(0xFFFA2D48),
    borderFocusedWhite = Color(0xFFFFFFFF),

    scrimDark = Color(0xCC000000),
    glassOverlay = Color(0x3320202E),
    glassOverlayFocused = Color(0x66FA2D48),

    backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF150D18),
            Color(0xFF0A090D),
            Color(0xFF050507),
        ),
    ),

    isDark = true,
)

// ─────────────────────────────────────────────────────────────────────────────
// Light Palette (Apple Music-inspired clean white)
// ─────────────────────────────────────────────────────────────────────────────

val TvLightPalette = TvColorPalette(
    accentRed = Color(0xFFE5233D),
    accentRedGlow = Color(0x44E5233D),
    accentPink = Color(0xFFE8314F),
    accentPurple = Color(0xFF9B41D4),
    accentBlue = Color(0xFF007AFF),

    background = Color(0xFFF2F2F7),
    backgroundElevated = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8E8ED),
    surfaceFocused = Color(0xFFD6D6DE),
    surfaceSelected = Color(0xFFFCE4E7),

    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF636366),
    textMuted = Color(0xFF8E8E93),

    borderSubtle = Color(0xFFD1D1D6),
    borderFocused = Color(0xFFE5233D),
    borderFocusedWhite = Color(0xFF1C1C1E),

    scrimDark = Color(0x88000000),
    glassOverlay = Color(0x22F2F2F7),
    glassOverlayFocused = Color(0x44E5233D),

    backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8F0FA),
            Color(0xFFF4F4F9),
            Color(0xFFF2F2F7),
        ),
    ),

    isDark = false,
)

// ─────────────────────────────────────────────────────────────────────────────
// CompositionLocal — runtime accessor
// ─────────────────────────────────────────────────────────────────────────────

val LocalTvColors = compositionLocalOf { TvDarkPalette }

// ─────────────────────────────────────────────────────────────────────────────
// Backward-compatible static accessor — keeps ALL existing TvColors.X references working.
// This object always returns dark-mode colors for compile-time constants (typography, etc.)
// Runtime code that needs theme-awareness should use TvThemeColors (composable).
// ─────────────────────────────────────────────────────────────────────────────

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
        ),
    )
}

/**
 * Runtime-aware theme colors accessor. Use inside @Composable functions
 * for theme-responsive colors (dark/light mode).
 */
object TvThemeColors {
    val current: TvColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalTvColors.current
}

// ─────────────────────────────────────────────────────────────────────────────
// Font family
// ─────────────────────────────────────────────────────────────────────────────

val TvSFProDisplay = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.W400),
    Font(R.font.sf_pro_display_medium, FontWeight.W500),
    Font(R.font.sf_pro_display_semibold, FontWeight.W600),
    Font(R.font.sf_pro_display_bold, FontWeight.W700),
    Font(R.font.sf_pro_display_heavy, FontWeight.W800),
)

// ─────────────────────────────────────────────────────────────────────────────
// 10-foot TV typography scale
// ─────────────────────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────────────────────
// MaterialTheme color scheme (TV)
// ─────────────────────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────────────────────
// Dimensions — updated for top-nav layout
// ─────────────────────────────────────────────────────────────────────────────

object TvDimensions {
    val SafeMarginHorizontal = 48.dp
    val SafeMarginVertical = 27.dp
    val CardSpacing = 18.dp
    val ShelfSpacing = 28.dp
    val TopNavBarHeight = 64.dp

    // Legacy — kept for backward compatibility but no longer used for nav rail
    val NavigationRailWidth = 76.dp
    val NavigationRailExpandedWidth = 220.dp
}

// ─────────────────────────────────────────────────────────────────────────────
// Theme wrapper — provides CompositionLocal palette based on user preference
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BitChordTvTheme(
    content: @Composable () -> Unit,
) {
    val themeMode by AppSettings.themeMode.collectAsState()
    val palette = when (themeMode) {
        ThemeMode.LIGHT -> TvLightPalette
        ThemeMode.DARK -> TvDarkPalette
        ThemeMode.SYSTEM -> TvDarkPalette // TV is almost always dark
    }

    CompositionLocalProvider(LocalTvColors provides palette) {
        MaterialTheme(
            colorScheme = TvColorScheme,
            typography = TvTypography,
            content = content,
        )
    }
}
