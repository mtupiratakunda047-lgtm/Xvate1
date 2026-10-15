package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = DeloGold,
    onPrimary = DeloGoldDark,
    primaryContainer = DeloSurfaceVariant,
    onPrimaryContainer = DeloGold,
    secondary = DeloGreen,
    onSecondary = DeloBackground,
    secondaryContainer = DeloGreenContainer,
    onSecondaryContainer = DeloGreen,
    tertiary = DeloAmber,
    onTertiary = DeloBackground,
    tertiaryContainer = DeloAmberContainer,
    onTertiaryContainer = DeloAmber,
    error = DeloRed,
    onError = DeloBackground,
    errorContainer = DeloRedContainer,
    onErrorContainer = DeloRed,
    background = DeloBackground,
    onBackground = DeloOnBackground,
    surface = DeloSurface,
    onSurface = DeloOnSurface,
    surfaceVariant = DeloSurfaceVariant,
    onSurfaceVariant = DeloOnSurfaceMuted,
    outline = DeloBorder
  )

private val LightColorScheme = DarkColorScheme // Delo Minds primarily uses its distinctive dark olive-gold palette

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

