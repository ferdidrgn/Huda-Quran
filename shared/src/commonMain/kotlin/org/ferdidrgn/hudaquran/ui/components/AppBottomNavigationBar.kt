package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.Screen

private data class BottomTab(val screen: Screen, val emoji: String, val label: String)

private val bottomNavScreens = listOf(Screen.Home, Screen.SurahList, Screen.Favorites, Screen.Settings)

private fun tabsFor(strings: Strings) = listOf(
    BottomTab(Screen.Home, "🏠", strings.navHome),
    BottomTab(Screen.SurahList, "📖", strings.navSurahs),
    BottomTab(Screen.Favorites, "⭐", strings.navFavorites),
    BottomTab(Screen.Settings, "⚙️", strings.navSettings),
)

fun Screen.isBottomNavDestination(): Boolean = this in bottomNavScreens

@Composable
fun AppBottomNavigationBar(navigator: AppNavigator, current: Screen) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    val tabs = tabsFor(stringsFor(appLanguage))
    GlassSurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            tabs.forEach { tab ->
                val selected = current == tab.screen
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { if (!selected) navigator.replaceAll(tab.screen) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
                                CircleShape,
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                    ) {
                        Text(tab.emoji, fontSize = 18.sp)
                    }
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
