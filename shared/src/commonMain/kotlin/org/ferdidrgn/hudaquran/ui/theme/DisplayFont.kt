package org.ferdidrgn.hudaquran.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import hudaquran.shared.generated.resources.Res
import hudaquran.shared.generated.resources.marcellus_regular
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.jetbrains.compose.resources.Font

/**
 * Marcellus — an inscriptional roman, used only for headings so screen and section titles carry
 * the carved, manuscript-like character of the brand while body text stays in the platform face.
 *
 * It covers Latin (incl. Turkish/German/French/Turkmen) but not Cyrillic or Arabic, and the web
 * canvas has no system glyph fallback, so Kyrgyz and Arabic UI keep the default face.
 */
@Composable
fun rememberDisplayFontFamily(language: AppLanguage): FontFamily {
    val supported = language != AppLanguage.KYRGYZ && language != AppLanguage.ARABIC
    if (!supported) return FontFamily.Default
    val font = Font(Res.font.marcellus_regular)
    return remember(font) { FontFamily(font) }
}
