package org.ferdidrgn.hudaquran.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
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

private val SakuraColors = darkColorScheme(
    primary = SakuraBlossom,
    onPrimary = SakuraWine,
    primaryContainer = SakuraPlum,
    onPrimaryContainer = SakuraBlossom,
    secondary = SakuraMauve,
    onSecondary = SakuraInk,
    secondaryContainer = SakuraWine,
    onSecondaryContainer = SakuraBlossom,
    background = SakuraInk,
    onBackground = SakuraBlossom,
    surface = SakuraSurface,
    onSurface = SakuraBlossom,
    surfaceVariant = SakuraWine,
    onSurfaceVariant = SakuraMauve,
    outline = SakuraMauve,
    outlineVariant = SakuraPlum,
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

/**
 * Headings (display/headline/titleLarge) use [display] — an inscriptional face with a single
 * weight, so synthesis is off: a caller's `fontWeight = Bold` keeps the true letterforms instead of
 * a smeared faux-bold. Everything read in quantity stays in the platform face.
 */
private fun hudaTypography(display: FontFamily): Typography {
    val heading = TextStyle(fontFamily = display, fontSynthesis = FontSynthesis.None)
    return Typography(
        displaySmall = heading.copy(fontSize = 36.sp, lineHeight = 44.sp),
        headlineLarge = heading.copy(fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = heading.copy(fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = heading.copy(fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge = heading.copy(fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    )
}

private val HudaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HudaQuranTheme(
    themeMode: ThemeMode = ThemeMode.SAKURA,
    displayFontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit,
) {
    val colors = when (themeMode) {
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) DarkColors else LightColors
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.SAKURA -> SakuraColors
    }
    val typography = remember(displayFontFamily) { hudaTypography(displayFontFamily) }
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = HudaShapes,
    ) {
        // Material only sets LocalContentColor inside a Surface; everywhere else a Text or Icon
        // without an explicit colour fell back to black, which was unreadable on the Sakura and
        // Dark backgrounds (lesson titles, Esma names, "continue reading" rows).
        SystemBarsEffect(isDarkTheme = colors.background.luminance() < 0.5f, barColor = colors.background)
        CompositionLocalProvider(LocalContentColor provides colors.onBackground, content = content)
    }
}
