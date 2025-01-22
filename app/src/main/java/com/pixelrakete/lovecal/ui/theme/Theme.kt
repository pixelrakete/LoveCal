package com.pixelrakete.lovecal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63),      // Pink
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE4EC),
    onPrimaryContainer = Color(0xFF442C2E),
    secondary = Color(0xFFAD1457),     // Dark Pink
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8BBD0),
    onSecondaryContainer = Color(0xFF442C2E),
    tertiary = Color(0xFFC2185B),      // Another Pink shade
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF48FB1),
    onTertiaryContainer = Color(0xFF442C2E),
    background = Color(0xFFFCE4EC),    // Very light pink
    onBackground = Color(0xFF442C2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF442C2E),
    surfaceVariant = Color(0xFFF8BBD0),
    onSurfaceVariant = Color(0xFF442C2E),
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFDE7E7),
    onErrorContainer = Color(0xFF442C2E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF4081),      // Light Pink
    onPrimary = Color.White,
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFFCE4EC),
    secondary = Color(0xFFF48FB1),     // Medium Pink
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF880E4F),
    onSecondaryContainer = Color(0xFFFCE4EC),
    tertiary = Color(0xFFF06292),      // Another Pink shade
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF880E4F),
    onTertiaryContainer = Color(0xFFFCE4EC),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFFCE4EC),
    surface = Color(0xFF2D2D2D),
    onSurface = Color(0xFFFCE4EC),
    surfaceVariant = Color(0xFF3D2D2F),
    onSurfaceVariant = Color(0xFFFCE4EC),
    error = Color(0xFFCF6679),
    onError = Color.White,
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = Color(0xFFFCE4EC)
)

@Composable
fun LoveCalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}