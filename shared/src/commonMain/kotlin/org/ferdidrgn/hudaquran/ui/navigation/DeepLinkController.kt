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

    // Separate from [pending]: a browser back/forward press already changed the address bar to
    // its target, so the app must jump straight to that screen (replacing the stack) rather than
    // pushing a new entry on top the way an incoming deep link does.
    private val _popped = MutableStateFlow<Screen?>(null)
    val popped: StateFlow<Screen?> = _popped.asStateFlow()

    fun handle(url: String) {
        DeepLink.parse(url)?.let { _pending.value = it }
    }

    fun handlePopState(url: String) {
        DeepLink.parse(url)?.let { _popped.value = it }
    }

    fun consumePending(): Screen? {
        val value = _pending.value
        _pending.value = null
        return value
    }

    fun consume() {
        _pending.value = null
    }

    fun consumePopped() {
        _popped.value = null
    }
}
