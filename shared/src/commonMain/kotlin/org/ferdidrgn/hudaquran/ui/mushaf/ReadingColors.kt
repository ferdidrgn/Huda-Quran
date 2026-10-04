package org.ferdidrgn.hudaquran.ui.mushaf

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.theme.Emerald
import org.ferdidrgn.hudaquran.ui.theme.EmeraldBright
import org.ferdidrgn.hudaquran.ui.theme.Rose
import org.ferdidrgn.hudaquran.ui.theme.SakuraBlossom
import org.ferdidrgn.hudaquran.ui.theme.TextPrimaryDark

/**
 * The colour the meal (translation) is set in, beneath the Arabic — distinct from the Arabic ink
 * so the eye can tell the two apart at a glance, and tuned per theme to stay readable on the
 * page it sits on:
 * - Sakura: a soft blossom-rose on the dark page,
 * - Dark: a softened bright emerald,
 * - Light: a deep emerald on the cream paper (≈ 6:1 contrast).
 */
@Composable
fun translationInk(): Color {
    val themeMode by AppContainer.preferences.themeMode.collectAsState()
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when {
        dark && themeMode == ThemeMode.SAKURA -> lerp(SakuraBlossom, Rose, 0.38f)
        dark -> lerp(EmeraldBright, TextPrimaryDark, 0.18f)
        else -> lerp(Emerald, Color.Black, 0.38f)
    }
}
