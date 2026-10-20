package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyberYellow,
    onPrimary = CyberTextDark,
    primaryContainer = CyberYellowDark,
    onPrimaryContainer = CyberTextDark,
    secondary = CyberTeal,
    onSecondary = CyberTextDark,
    background = CyberTeal,
    onBackground = CyberTextDark,
    surface = CyberCardDark,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberCardSurface,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberYellow
)

private val LightColorScheme = lightColorScheme(
    primary = CyberYellow,
    onPrimary = CyberTextDark,
    primaryContainer = CyberYellowLight,
    onPrimaryContainer = CyberTextDark,
    secondary = CyberTeal,
    onSecondary = CyberTextDark,
    background = CyberTeal,
    onBackground = CyberTextDark,
    surface = CyberCardDark,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberCardSurface,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberYellow
)

@Composable
fun MonoVoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CyberTeal.toArgb()
                window.navigationBarColor = CyberTeal.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MonoVoiceTheme(darkTheme = darkTheme, content = content)
}
