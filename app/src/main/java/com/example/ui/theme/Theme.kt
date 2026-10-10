package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuxuryDarkColorScheme = darkColorScheme(
  primary = SoftGold,
  onPrimary = DeepCharcoal,
  primaryContainer = WarmCaramelDark,
  onPrimaryContainer = CreamBackground,
  secondary = WarmCaramelLight,
  onSecondary = DeepCharcoal,
  secondaryContainer = CharcoalCard,
  onSecondaryContainer = SoftGoldLight,
  tertiary = LeafGreenLight,
  onTertiary = DeepCharcoal,
  background = DeepCharcoal,
  onBackground = CreamBackground,
  surface = CharcoalSurface,
  onSurface = CreamBackground,
  surfaceVariant = CharcoalCard,
  onSurfaceVariant = SoftGoldLight,
  outline = SoftGoldMuted,
  outlineVariant = DividerGold,
  error = Color(0xFFCF6679),
  onError = DeepCharcoal
)

private val LuxuryLightColorScheme = lightColorScheme(
  primary = EspressoBrown,
  onPrimary = PureWhite,
  primaryContainer = EspressoBrownLight,
  onPrimaryContainer = CreamBackground,
  secondary = WarmCaramel,
  onSecondary = PureWhite,
  secondaryContainer = CreamSurface,
  onSecondaryContainer = EspressoBrown,
  tertiary = LeafGreen,
  onTertiary = PureWhite,
  background = CreamBackground,
  onBackground = DeepCharcoal,
  surface = PureWhite,
  onSurface = DeepCharcoal,
  surfaceVariant = CreamSurface,
  onSurfaceVariant = TextSecondaryLight,
  outline = SoftGold,
  outlineVariant = DividerGold,
  error = StatusClosed,
  onError = PureWhite
)

@Composable
fun ArtesanoTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) LuxuryDarkColorScheme else LuxuryLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
