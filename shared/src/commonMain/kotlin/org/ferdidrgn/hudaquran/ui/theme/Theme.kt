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

private val LightColors = lightColorScheme(
    primary = SageSoft,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = SageLight,
    onPrimaryContainer = SageDeepText,
    secondary = AmberSoft,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = AmberLight,
    onSecondaryContainer = AmberDeep,
    background = CreamBg,
    onBackground = InkSoft,
    surface = SurfaceWarm,
    onSurface = InkSoft,
    surfaceVariant = SageLight,
    onSurfaceVariant = SageDeepText,
    error = ErrorSoft,
)

private val DarkColors = darkColorScheme(
    primary = NightSagePrimary,
    onPrimary = Color(0xFF1D2B22),
    primaryContainer = SageDark,
    onPrimaryContainer = SageLight,
    secondary = AmberSoft,
    onSecondary = Color(0xFF3A2705),
    secondaryContainer = AmberDeep,
    onSecondaryContainer = AmberLight,
    background = NightBg,
    onBackground = Color(0xFFE7EEE9),
    surface = NightSurface,
    onSurface = Color(0xFFE7EEE9),
    surfaceVariant = Color(0xFF2C3A33),
    onSurfaceVariant = Color(0xFFCBD8D0),
    error = Color(0xFFE0A6A9),
)

private val HudaTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

private val HudaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
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
