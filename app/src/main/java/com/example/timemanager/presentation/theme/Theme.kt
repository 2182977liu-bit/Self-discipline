package com.example.timemanager.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 浅色主题配色方案
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnPrimaryDark,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = OnSecondaryDark,
    tertiary = Tertiary,
    onTertiary = OnPrimary,
    tertiaryContainer = TertiaryLight,
    onTertiaryContainer = OnPrimaryDark,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnPrimary,
    errorContainer = ErrorLight,
    onErrorContainer = Error
)

/**
 * 深色主题配色方案
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = Primary,
    onPrimaryContainer = OnPrimary,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = Secondary,
    onSecondaryContainer = OnSecondary,
    tertiary = TertiaryDark,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = Tertiary,
    onTertiaryContainer = OnPrimary,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = Error,
    onError = OnPrimary,
    errorContainer = Error,
    onErrorContainer = ErrorLight
)

/**
 * 应用主题
 *
 * @param darkTheme 是否使用深色主题
 * @param dynamicColor 是否使用动态颜色（Android 12+）
 * @param content 内容
 */
@Composable
fun TimeManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Android 12+ 支持动态颜色
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
