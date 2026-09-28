package org.ferdidrgn.hudaquran.ui.navigation

import kotlinx.browser.window

actual fun syncBrowserUrl(path: String, replace: Boolean) {
    jsPushState(path, replace)
}

private fun jsPushState(path: String, replace: Boolean): Unit =
    js("window.history && window.location.pathname !== path && (replace ? window.history.replaceState({}, '', path) : window.history.pushState({}, '', path))")

actual fun observeBrowserNavigation(onUrlChanged: (String) -> Unit) {
    window.addEventListener("popstate", {
        onUrlChanged(window.location.pathname + window.location.search)
    })
}
