package com.example.electricianappnew.ui.theme

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
import androidx.compose.ui.graphics.Color // Add Color import
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Using the custom colors defined in Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue, // Using custom dark blue
    secondary = SecondaryOrange, // Using custom orange
    tertiary = Pink80, // Example, can customize further
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimaryDark,
    onSecondary = OnSecondaryDark,
    onTertiary = Color.Black, // Example
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue, // Using custom dark blue
    secondary = SecondaryOrange, // Using custom orange
    tertiary = Pink40, // Example, can customize further
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = OnPrimaryLight,
    onSecondary = OnSecondaryLight,
    onTertiary = Color.White, // Example
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun ElectricianAppNewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
            // window.statusBarColor = colorScheme.primary.toArgb() // Deprecated - Commented out
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme // Adjust status bar icon color
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // We'll create Typography.kt next
        content = content
    )
}
