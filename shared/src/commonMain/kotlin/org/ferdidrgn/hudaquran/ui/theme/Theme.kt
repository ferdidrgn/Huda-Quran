package org.ferdidrgn.hudaquran.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.data.local.ThemeMode

private val DarkColors = darkColorScheme(
    primary = GiltBright,
    onPrimary = Color(0xFF2A2005),
    primaryContainer = Gilt,
    onPrimaryContainer = Color(0xFF241B02),
    secondary = EmeraldBright,
    onSecondary = Color(0xFF042919),
    secondaryContainer = Emerald,
    onSecondaryContainer = Color(0xFFE3FCEF),
    background = Canvas,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceHighlight,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDarkStrong,
    outlineVariant = BorderDark,
    error = Rose,
    onError = Color(0xFF2B0507),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8A6B22),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3E6C4),
    onPrimaryContainer = Color(0xFF3A2C0A),
    secondary = Emerald,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3F5E4),
    onSecondaryContainer = Color(0xFF04351F),
    background = CanvasLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLightMode,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceHighlightLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0x1F000000),
    outlineVariant = BorderLight,
    error = Rose,
    onError = Color(0xFFFFFFFF),
)

private val HudaTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 13.5.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
)

private val HudaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
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
        shapes = HudaShapes,
        content = content,
    )
}
