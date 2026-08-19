package org.ferdidrgn.hudaquran.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Platform entry points (Android intent data, web `window.location`, iOS URL open callbacks)
 * push incoming links here; [org.ferdidrgn.hudaquran.App] observes [pending] and navigates once
 * it has a live [org.ferdidrgn.hudaquran.ui.navigation.AppNavigator] to route with.
 */
object DeepLinkController {
    private val _pending = MutableStateFlow<Screen?>(null)
    val pending: StateFlow<Screen?> = _pending.asStateFlow()

    fun handle(url: String) {
        DeepLink.parse(url)?.let { _pending.value = it }
    }

    fun consumePending(): Screen? {
        val value = _pending.value
        _pending.value = null
        return value
    }

    fun consume() {
        _pending.value = null
    }
}
