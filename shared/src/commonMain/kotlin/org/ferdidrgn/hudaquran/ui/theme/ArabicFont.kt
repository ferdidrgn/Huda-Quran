package org.ferdidrgn.hudaquran.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import hudaquran.shared.generated.resources.Res
import hudaquran.shared.generated.resources.noto_naskh_arabic
import org.jetbrains.compose.resources.Font

/**
 * Compose Multiplatform for Web (Skia/Wasm) doesn't inherit the browser/OS's Arabic glyph
 * coverage the way Android/iOS pick up real system fonts — without an explicitly bundled font,
 * Arabic text renders as empty boxes on web (it still renders fine on Android/iOS, which is why
 * this only ever showed up in web screenshots). Bundled once here and provided app-wide via
 * [LocalArabicFontFamily] so every Arabic Quran text composable can opt into it; Android/iOS get
 * the same bundled font too, for a consistent Arabic typeface across platforms.
 */
val LocalArabicFontFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Default }

@Composable
fun rememberArabicFontFamily(): FontFamily {
    val arabicFont = Font(Res.font.noto_naskh_arabic)
    return remember(arabicFont) { FontFamily(arabicFont) }
}
