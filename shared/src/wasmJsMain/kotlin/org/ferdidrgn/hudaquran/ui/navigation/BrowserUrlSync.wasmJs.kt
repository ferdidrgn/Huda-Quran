package org.ferdidrgn.hudaquran.ui.navigation

actual fun syncBrowserUrl(path: String) {
    jsPushState(path)
}

private fun jsPushState(path: String): Unit =
    js("window.history && window.location.pathname !== path && window.history.pushState({}, '', path)")
