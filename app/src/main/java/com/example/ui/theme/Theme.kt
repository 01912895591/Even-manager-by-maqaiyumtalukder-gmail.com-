package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = AccentGold,
  onPrimary = PlumDark,
  primaryContainer = DeepPlum,
  onPrimaryContainer = AccentGoldLight,
  secondary = Emerald,
  onSecondary = Color.White,
  secondaryContainer = DarkSurface,
  onSecondaryContainer = DarkTextPrimary,
  tertiary = Rust,
  onTertiary = Color.White,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkCard,
  onSurfaceVariant = DarkTextMuted,
  outline = DarkBorderSubtle,
  error = DangerRed
)

private val LightColorScheme = lightColorScheme(
  primary = DeepPlum,
  onPrimary = Color.White,
  primaryContainer = PlumDark,
  onPrimaryContainer = AccentGoldLight,
  secondary = AccentGold,
  onSecondary = PlumDark,
  secondaryContainer = AccentGoldLight,
  onSecondaryContainer = PlumDark,
  tertiary = Emerald,
  onTertiary = Color.White,
  background = CreamBackground,
  onBackground = TextDark,
  surface = WhiteCard,
  onSurface = TextDark,
  surfaceVariant = CreamBackground,
  onSurfaceVariant = TextMuted,
  outline = BorderSubtle,
  error = DangerRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

