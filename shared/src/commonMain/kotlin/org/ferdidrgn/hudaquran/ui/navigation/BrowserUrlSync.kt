package org.ferdidrgn.hudaquran.ui.navigation

/**
 * Keeps the browser address bar in sync with in-app navigation so links are shareable and the
 * back/forward buttons work. A no-op on Android/iOS, where there is no address bar to update.
 */
expect fun syncBrowserUrl(path: String)
