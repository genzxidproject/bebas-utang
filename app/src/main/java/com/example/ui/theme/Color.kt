package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { false }

// Primary Brand Colors matching the uploaded reference design
val HeaderBlueStart = Color(0xFF173D7A)
val HeaderBlueMid = Color(0xFF1D63D8)
val HeaderBlueEnd = Color(0xFF2877EE)

val NavyCardDark = Color(0xFF14294B)
val NavyCardDeep = Color(0xFF0E1F3B)
val NavyTrackSlate = Color(0xFF334B70)
val RingTrackLight = Color(0xFFC2CEE0)

val ProgressGreen = Color(0xFF22C55E)
val ProgressGreenLight = Color(0xFF4ADE80)

val ActionBlueStart = Color(0xFF428DFF)
val ActionBlueEnd = Color(0xFF1E66E0)

val MenuTileSoftBlue = Color(0xFFD8E8FC)
val MenuTileDarkNavy = Color(0xFF182D4E)
val MenuTileIconBlue = Color(0xFF2272EB)
val MenuTileIconLightBlue = Color(0xFF59A5FF)

val CreditCardRoyalBlue = Color(0xFF1E70EB)
val CreditCardTrackBlue = Color(0xFF1553B5)

val ScreenBackgroundIce = Color(0xFFF1F6FD)
val ScreenBackgroundDark = Color(0xFF091220)

val CutoutNotchBg = Color(0xFFE5EFFC)
val CutoutNotchDarkBg = Color(0xFF091220)

val BottomBarSurface = Color(0xFFF7FAFF)
val BottomBarDarkSurface = Color(0xFF101E33)

val BottomNavInactive = Color(0xFF7B8CA6)
val BottomNavActive = Color(0xFF14294B)
val BottomNavActiveDark = Color(0xFF60A5FA)

val TextDarkNavy = Color(0xFF0F1E36)
val TextLightPrimary = Color(0xFFF1F5F9)
val TextMutedSlate = Color(0xFF64748B)
val TextMutedDark = Color(0xFF94A3B8)
val TextLightMuted = Color(0xFF94A3B8)
val AlertRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)

/**
 * Dynamic adaptive color tokens that automatically adjust between Light Mode
 * (matching the uploaded reference screenshot) and Dark Mode (eye-friendly low-light theme).
 */
object AppThemeColors {
    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkTheme.current

    val screenBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) ScreenBackgroundDark else ScreenBackgroundIce

    val cardSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF122138) else Color.White

    val subtleSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF1A2E4C) else Color(0xFFF1F5F9)

    val highlightedCardSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF1B3358) else Color(0xFFEFF6FF)

    val menuTileBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) MenuTileDarkNavy else MenuTileSoftBlue

    val menuTilePrimaryBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF1E56A0) else NavyCardDark

    val textPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) TextLightPrimary else TextDarkNavy

    val textSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) TextMutedDark else TextMutedSlate

    val linkBlue: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E56A0)

    val cutoutNotchBg: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) CutoutNotchDarkBg else CutoutNotchBg

    val bottomBarSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) BottomBarDarkSurface else BottomBarSurface

    val bottomNavActive: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) BottomNavActiveDark else BottomNavActive

    val centerButtonRing: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF1B3152) else Color(0xFFE7EFFA)
}
