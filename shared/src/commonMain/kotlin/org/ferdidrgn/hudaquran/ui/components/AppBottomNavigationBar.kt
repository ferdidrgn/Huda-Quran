package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.Screen

internal data class BottomTab(val screen: Screen, val icon: ImageVector, val selectedIcon: ImageVector, val label: String)

private val bottomNavScreens = listOf(Screen.Home, Screen.SurahList, Screen.Favorites, Screen.Settings)

internal fun tabsFor(strings: Strings) = listOf(
    BottomTab(Screen.Home, Icons.Outlined.Home, Icons.Filled.Home, strings.navHome),
    BottomTab(Screen.SurahList, Icons.Outlined.AutoStories, Icons.Filled.AutoStories, strings.navSurahs),
    BottomTab(Screen.Favorites, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite, strings.navFavorites),
    BottomTab(Screen.Settings, Icons.Outlined.Settings, Icons.Filled.Settings, strings.navSettings),
)

fun Screen.isBottomNavDestination(): Boolean = this in bottomNavScreens

/**
 * Mobile tab bar: a solid surface docked at the bottom (in the thumb zone), with a pill indicator
 * behind the active icon that slides in on selection — the Material 3 pattern users already know.
 */
@Composable
fun AppBottomNavigationBar(navigator: AppNavigator, current: Screen) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    val tabs = tabsFor(stringsFor(appLanguage))
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                drawLine(accent.copy(alpha = 0.16f), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        tabs.forEach { tab ->
            val selected = current == tab.screen
            val indicatorAlpha by animateFloatAsState(if (selected) 1f else 0f, tween(220), label = "tabIndicator")
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { if (!selected) navigator.replaceAll(tab.screen) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = indicatorAlpha), CircleShape)
                        .padding(horizontal = 18.dp, vertical = 5.dp),
                ) {
                    Icon(
                        if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    tab.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
