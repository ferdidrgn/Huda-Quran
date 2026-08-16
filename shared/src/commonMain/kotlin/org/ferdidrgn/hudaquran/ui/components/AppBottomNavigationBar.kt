package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.Screen

private data class BottomTab(val screen: Screen, val emoji: String, val label: String)

private val tabs = listOf(
    BottomTab(Screen.Home, "🏠", "Ana Sayfa"),
    BottomTab(Screen.SurahList, "📖", "Sureler"),
    BottomTab(Screen.Favorites, "⭐", "Favoriler"),
    BottomTab(Screen.Settings, "⚙️", "Ayarlar"),
)

fun Screen.isBottomNavDestination(): Boolean = tabs.any { it.screen == this }

@Composable
fun AppBottomNavigationBar(navigator: AppNavigator, current: Screen) {
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = current == tab.screen,
                onClick = {
                    if (current != tab.screen) navigator.replaceAll(tab.screen)
                },
                icon = { Text(tab.emoji, fontSize = 20.sp) },
                label = { Text(tab.label) },
            )
        }
    }
}
