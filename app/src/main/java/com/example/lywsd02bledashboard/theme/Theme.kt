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

import androidx.compose.ui.graphics.Color

// 일관된 고시인성 프리미엄 라이트 테마 (웹 대시보드 테마와 동일)
private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = Color(0xFFE2EBE8),
    onPrimaryContainer = InkPrimary,
    secondary = CyanAccent,
    onSecondary = SurfaceWhite,
    tertiary = CoralAccent,
    background = BackgroundPaper,
    onBackground = InkPrimary,
    surface = SurfaceWhite,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = InkPrimary,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = SurfaceWhite,
    surfaceContainerLow = BackgroundPaper,
    surfaceContainerLowest = SurfaceWhite,
    outline = BorderLine,
    outlineVariant = BorderLineStrong
)

/**
 * LYWSD02 BLE 대시보드 테마.
 * 시스템의 다크 모드 설정과 무관하게 항상 선명하고 가독성이 뛰어난
 * 전용 화이트 & 틸 라이트 테마를 유지합니다.
 */
@Composable
fun LYWSD02BLEDashboardTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SurfaceWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
