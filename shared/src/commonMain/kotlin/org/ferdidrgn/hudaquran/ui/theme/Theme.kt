package org.ferdidrgn.hudaquran.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.data.local.ThemeMode

private val LightColors = lightColorScheme(
    primary = HudaGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = HudaGreenLight,
    onPrimaryContainer = HudaGreenDark,
    secondary = HudaGold,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = HudaGoldLight,
    onSecondaryContainer = Color(0xFF4A3B00),
    background = HudaCream,
    onBackground = HudaInk,
    surface = Color(0xFFFFFFFF),
    onSurface = HudaInk,
    error = HudaErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD9A6),
    onPrimary = HudaGreenDark,
    primaryContainer = HudaGreenDark,
    onPrimaryContainer = HudaGreenLight,
    secondary = HudaGold,
    onSecondary = Color(0xFF2B2200),
    secondaryContainer = Color(0xFF4A3B00),
    onSecondaryContainer = HudaGoldLight,
    background = Color(0xFF0B1A12),
    onBackground = Color(0xFFE6EFE9),
    surface = HudaSurfaceDark,
    onSurface = Color(0xFFE6EFE9),
    error = Color(0xFFFFB4AB),
)

private val HudaTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

@Composable
fun HudaQuranTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = HudaTypography,
        content = content,
    )
}
