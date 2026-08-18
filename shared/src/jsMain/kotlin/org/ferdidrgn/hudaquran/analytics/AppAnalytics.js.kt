package org.ferdidrgn.hudaquran.analytics

/**
 * Firebase Web SDK isn't wired up for the web target yet (would need the firebase npm
 * package + a web app config from the Firebase console). Safe no-op for now.
 */
actual object AppAnalytics {
    actual fun initialize() {}
    actual fun logEvent(name: String, params: Map<String, String>) {}
    actual fun setUserProperty(name: String, value: String) {}
    actual fun log(message: String) {}
    actual fun recordException(throwable: Throwable) {}
}
