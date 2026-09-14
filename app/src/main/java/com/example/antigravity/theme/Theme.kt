package com.example.antigravity.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AntigravityDarkColorScheme = darkColorScheme(
    primary = AntigravityColors.ElectricCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004E58),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = AntigravityColors.NeonViolet,
    onSecondary = Color(0xFF2C0B60),
    secondaryContainer = Color(0xFF452086),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = Color(0xFF00E676),
    background = AntigravityColors.BackgroundDark,
    onBackground = AntigravityColors.TextPrimary,
    surface = AntigravityColors.SurfaceDark,
    onSurface = AntigravityColors.TextPrimary,
    surfaceVariant = AntigravityColors.SurfaceElevated,
    onSurfaceVariant = AntigravityColors.TextSecondary,
    outline = AntigravityColors.CardBorder,
    error = AntigravityColors.StatusError
)

private val AntigravityLightColorScheme = lightColorScheme(
    primary = Color(0xFF006876),
    onPrimary = Color.White,
    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFE5E7EB)
)

@Composable
fun AntigravityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AntigravityDarkColorScheme else AntigravityLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
