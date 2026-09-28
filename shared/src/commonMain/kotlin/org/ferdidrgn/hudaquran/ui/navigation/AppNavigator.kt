package org.ferdidrgn.hudaquran.ui.navigation

import androidx.compose.runtime.mutableStateListOf

class AppNavigator(start: Screen = Screen.Splash) {
    private val backStack = mutableStateListOf(start)

    val current: Screen
        get() = backStack.last()

    fun canGoBack(): Boolean = backStack.size > 1

    fun navigate(screen: Screen) {
        backStack.add(screen)
    }

    fun replaceAll(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
    }

    /**
     * Starts a fresh history at [screen] with Home beneath it, so an in-app back arrow always has
     * somewhere to go — even on screens (like Mushaf) that hide the navigation bars.
     */
    fun resetTo(screen: Screen) {
        backStack.clear()
        if (screen != Screen.Home) backStack.add(Screen.Home)
        backStack.add(screen)
    }

    /** Swaps the current screen in place, keeping everything beneath it so "back" still works. */
    fun replaceTop(screen: Screen) {
        if (backStack.last() != screen) backStack[backStack.lastIndex] = screen
    }

    fun back(): Boolean {
        if (!canGoBack()) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}
