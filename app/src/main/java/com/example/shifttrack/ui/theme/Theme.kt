package com.example.shifttrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandBlueDark,
    secondary = Slate700,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose100,
    onErrorContainer = Rose600
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = BrandBlueDark,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color.White,
    secondary = Slate200,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = Color.White,
    background = Slate900,
    onBackground = Color.White,
    surface = Slate800,
    onSurface = Color.White,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose100,
    onErrorContainer = Color.White
)

@Composable
fun SHIFTTRACKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
