package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElegantDarkColorScheme =
  darkColorScheme(
    primary = ElegantPurpleAccent,
    onPrimary = ElegantPurpleOnAccent,
    primaryContainer = ElegantDarkSurfaceVariant,
    onPrimaryContainer = ElegantPurpleAccent,
    secondary = ElegantPurpleSecondary,
    onSecondary = ElegantPurpleOnAccent,
    secondaryContainer = ElegantDarkCardElevated,
    onSecondaryContainer = ElegantPurpleSecondary,
    tertiary = ElegantPinkTertiary,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF492532),
    onTertiaryContainer = ElegantPinkTertiary,
    background = ElegantDarkBg,
    onBackground = ElegantTextPrimary,
    surface = ElegantDarkSurface,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantTextSecondary,
    surfaceContainerHighest = ElegantDarkCardElevated,
    outline = ElegantDarkBorder,
    outlineVariant = ElegantDarkBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ElegantDarkColorScheme,
    typography = Typography,
    content = content
  )
}
