package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.navigation.Screen

/** Lets the footer's links navigate without every screen threading a navigator through. */
val LocalFooterNavigation = staticCompositionLocalOf<(Screen) -> Unit> { {} }

/**
 * The closing ornament every page ends on: a still shamsa, the app name, its tagline and — on the
 * website — links to the main sections, like a printed book's colophon. Pages end here instead of
 * just stopping mid-scroll.
 */
@Composable
fun SiteFooter(modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val navigate = LocalFooterNavigation.current
    val isWeb = currentPlatform == Platform.WEB
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OrnamentRule(modifier = Modifier.widthIn(max = 360.dp), centered = true)
        Spacer(Modifier.height(18.dp))
        ShamsaRosette(color = colors.primary.copy(alpha = 0.8f), modifier = Modifier.size(if (isWeb) 64.dp else 48.dp), animate = false)
        Spacer(Modifier.height(10.dp))
        Text("Huda Qur'an", style = MaterialTheme.typography.titleLarge, color = colors.onBackground)
        Text(
            strings.appTagline,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
        if (isWeb) {
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                tabsFor(strings).forEach { tab ->
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { navigate(tab.screen) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "© 2026 Huda Qur'an",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onBackground.copy(alpha = 0.45f),
        )
    }
}
