package org.ferdidrgn.hudaquran.ui.navigation

import kotlinx.browser.window

actual fun syncBrowserUrl(path: String) {
    jsPushState(path)
}

private fun jsPushState(path: String): Unit =
    js("window.history && window.location.pathname !== path && window.history.pushState({}, '', path)")

actual fun observeBrowserNavigation(onUrlChanged: (String) -> Unit) {
    window.addEventListener("popstate", {
        onUrlChanged(window.location.pathname + window.location.search)
    })
}
