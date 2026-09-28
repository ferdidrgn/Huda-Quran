package org.ferdidrgn.hudaquran.ui.navigation

/**
 * Keeps the browser address bar in sync with in-app navigation so links are shareable and the
 * back/forward buttons work. With [replace], the current history entry is overwritten instead of
 * a new one being added. A no-op on Android/iOS, where there is no address bar to update.
 */
expect fun syncBrowserUrl(path: String, replace: Boolean = false)

/**
 * Registers [onUrlChanged] to fire with the new path whenever the user presses the browser's
 * back/forward buttons (a `popstate` event) — otherwise the address bar updates on its own but
 * the in-app screen never follows, so back/forward silently does nothing. A no-op on Android/iOS.
 */
expect fun observeBrowserNavigation(onUrlChanged: (String) -> Unit)
