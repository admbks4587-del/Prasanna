package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AnimeDarkColorScheme = darkColorScheme(
    primary = CyberVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceHighlight,
    onSecondaryContainer = ElectricCyan,
    tertiary = ShonenCrimson,
    onTertiary = Color.White,
    background = NeonIndigoBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderGlow,
    error = DangerRuby,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cinematic anime dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AnimeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
