package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReelGuardDarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletContainer,
    onPrimaryContainer = OnVioletContainer,
    secondary = IndigoAccent,
    onSecondary = Color.White,
    tertiary = CyanAccent,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF37394A),
    error = StatusErrorRed,
    errorContainer = StatusErrorRedContainer,
    onError = Color.White
)

private val ReelGuardLightColorScheme = lightColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = IndigoAccent,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = LightSurfaceContainer,
    outline = LightSurfaceBorder,
    error = StatusErrorRed,
    errorContainer = Color(0xFFFEE2E2),
    onError = Color.White
)

@Composable
fun ReelGuardTheme(
    darkTheme: Boolean = true, // Default to dark theme for wellbeing aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ReelGuardDarkColorScheme else ReelGuardLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for template compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ReelGuardTheme(darkTheme = darkTheme, content = content)
}
