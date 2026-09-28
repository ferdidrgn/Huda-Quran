package org.ferdidrgn.hudaquran.ui.navigation

actual fun syncBrowserUrl(path: String) {
    // No address bar on iOS.
}

actual fun observeBrowserNavigation(onUrlChanged: (String) -> Unit) {
    // No browser back/forward history on iOS.
}
