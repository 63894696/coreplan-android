package com.example.fitness.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// === 设计规范（2026-07-13）===

// 活力橙（健身能量）+ 运动绿（成就）+ 暗色渐变（沉浸感）
val Orange500 = Color(0xFFFF6B35)  // 主色
val Orange400 = Color(0xFFFF8A5C)
val Orange600 = Color(0xFFE5541D)
val Green500 = Color(0xFF00C896)    // 强调
val Green400 = Color(0xFF33D4AB)

// 暗色背景（深紫到深蓝渐变）
val BgDeep1 = Color(0xFF1A1330)
val BgDeep2 = Color(0xFF2A1B45)
val BgDeep3 = Color(0xFF3D2C5F)

// 浅色背景
val BgLight1 = Color(0xFFFFFAF5)
val BgLight2 = Color(0xFFFFF1E8)

// 文字
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF6B6B6B)
val TextInverse = Color(0xFFFAF7F2)

private val LightColors = lightColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3D5),
    onPrimaryContainer = Color(0xFF5A1A0A),
    secondary = Green500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCEFE5),
    onSecondaryContainer = Color(0xFF003B2C),
    tertiary = Color(0xFF7B5BD6),
    onTertiary = Color.White,
    background = BgLight1,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5E8DC),
    onSurfaceVariant = TextSecondary,
    error = Color(0xFFE53935),
    onError = Color.White,
    outline = Color(0xFFD0C5BA),
    outlineVariant = Color(0xFFE8DDD0)
)

private val DarkColors = darkColorScheme(
    primary = Orange400,
    onPrimary = Color(0xFF1A0A05),
    primaryContainer = Color(0xFF7A2810),
    onPrimaryContainer = Color(0xFFFFE3D5),
    secondary = Green400,
    onSecondary = Color(0xFF003326),
    secondaryContainer = Color(0xFF00523F),
    onSecondaryContainer = Color(0xFFCCEFE5),
    tertiary = Color(0xFFB5A1F0),
    onTertiary = Color(0xFF1B0A52),
    background = BgDeep1,
    onBackground = TextInverse,
    surface = BgDeep2,
    onSurface = TextInverse,
    surfaceVariant = BgDeep3,
    onSurfaceVariant = Color(0xFFC8B8D8),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF1A0808),
    outline = Color(0xFF6A5A7A),
    outlineVariant = Color(0xFF4A3A5A)
)

@Composable
fun FitnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
