package org.ferdidrgn.hudaquran.ui.navigation

actual fun syncBrowserUrl(path: String) {
    // No address bar on Android.
}

actual fun observeBrowserNavigation(onUrlChanged: (String) -> Unit) {
    // No browser back/forward history on Android.
}
