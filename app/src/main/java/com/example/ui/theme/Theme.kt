package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BebasUtangLightColorScheme = lightColorScheme(
    primary = HeaderBlueMid,
    onPrimary = Color.White,
    primaryContainer = MenuTileSoftBlue,
    onPrimaryContainer = NavyCardDark,
    secondary = ProgressGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF14532D),
    tertiary = CreditCardRoyalBlue,
    onTertiary = Color.White,
    background = ScreenBackgroundIce,
    onBackground = TextDarkNavy,
    surface = Color.White,
    onSurface = TextDarkNavy,
    surfaceVariant = MenuTileSoftBlue,
    onSurfaceVariant = NavyCardDark,
    error = AlertRed,
    onError = Color.White
)

private val BebasUtangDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF081426),
    primaryContainer = MenuTileDarkNavy,
    onPrimaryContainer = Color(0xFFDCEBFF),
    secondary = ProgressGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = Color(0xFFDCFCE7),
    tertiary = CreditCardRoyalBlue,
    onTertiary = Color.White,
    background = ScreenBackgroundDark,
    onBackground = TextLightPrimary,
    surface = Color(0xFF122138),
    onSurface = TextLightPrimary,
    surfaceVariant = MenuTileDarkNavy,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = AlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BebasUtangDarkColorScheme else BebasUtangLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
