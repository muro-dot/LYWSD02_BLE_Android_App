package com.example.lywsd02bledashboard.theme

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

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = DeepTeal,
    onPrimaryContainer = SurfaceWhite,
    secondary = CyanAccent,
    onSecondary = SurfaceWhite,
    tertiary = CoralAccent,
    background = BackgroundPaper,
    onBackground = InkPrimary,
    surface = SurfaceWhite,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = InkSecondary,
    outline = BorderLine,
    outlineVariant = BorderLineStrong
)

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = DeepTeal2,
    primaryContainer = DeepTeal,
    onPrimaryContainer = SurfaceWhite,
    secondary = TealPrimary,
    onSecondary = SurfaceWhite,
    background = DeepTeal2,
    onBackground = SurfaceWhite,
    surface = DeepTeal,
    onSurface = SurfaceWhite,
    surfaceVariant = InkSecondary,
    onSurfaceVariant = SurfaceSoft,
    outline = TealDark,
    outlineVariant = InkMuted
)

@Composable
fun LYWSD02BLEDashboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SurfaceWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
