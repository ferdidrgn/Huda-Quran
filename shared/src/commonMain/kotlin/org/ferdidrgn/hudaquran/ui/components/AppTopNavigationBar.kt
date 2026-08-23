package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.Screen

/**
 * The web counterpart to [AppBottomNavigationBar]: a website-style header with the wordmark on
 * the left and the tab set as horizontal pills on the right, since a bottom tab bar reads as a
 * mobile-app affordance a website visitor doesn't expect.
 */
@Composable
fun AppTopNavigationBar(
    navigator: AppNavigator,
    current: Screen,
    appTitle: String,
    modifier: Modifier = Modifier,
) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    val tabs = tabsFor(stringsFor(appLanguage))

    GlassSurface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                appTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEach { tab ->
                    val selected = current == tab.screen
                    Row(
                        modifier = Modifier
                            .clickable { if (!selected) navigator.replaceAll(tab.screen) }
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                                MaterialTheme.shapes.large,
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(tab.emoji, fontSize = 15.sp)
                        Text(
                            tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
