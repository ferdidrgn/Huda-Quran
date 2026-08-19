package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
 * The tablet/desktop counterpart to [AppBottomNavigationBar]. In [expanded] mode it renders as a
 * full sidebar with a header and inline labels (desktop web); otherwise it collapses to a narrow
 * icon rail (tablet-width windows), matching the tab set the mobile bottom bar uses.
 */
@Composable
fun AppSideNavigationBar(
    navigator: AppNavigator,
    current: Screen,
    expanded: Boolean,
    appTitle: String,
    modifier: Modifier = Modifier,
) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    val tabs = tabsFor(stringsFor(appLanguage))
    val railWidth = if (expanded) 260.dp else 96.dp

    GlassSurface(
        modifier = modifier.fillMaxHeight().width(railWidth).padding(vertical = 20.dp, horizontal = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(if (expanded) 16.dp else 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            if (expanded) {
                Text(
                    appTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 10.dp, bottom = 24.dp, top = 4.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tabs.forEach { tab ->
                    val selected = current == tab.screen
                    val itemModifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!selected) navigator.replaceAll(tab.screen) }
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                            MaterialTheme.shapes.large,
                        )
                        .padding(vertical = 12.dp, horizontal = if (expanded) 12.dp else 8.dp)

                    if (expanded) {
                        Row(modifier = itemModifier, verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.background(Color.Transparent, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(tab.emoji, fontSize = 18.sp)
                            }
                            Text(
                                tab.label,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    } else {
                        Column(modifier = itemModifier, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tab.emoji, fontSize = 18.sp)
                            Text(
                                tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
