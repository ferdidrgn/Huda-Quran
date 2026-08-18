package org.ferdidrgn.hudaquran.analytics

/**
 * Thin wrapper over Firebase Analytics + Crashlytics. On Android this talks to the
 * real Firebase SDKs (a no-op if no google-services.json / FirebaseApp was configured).
 * Other platforms are documented no-op stubs until their native Firebase SDK is wired.
 */
expect object AppAnalytics {
    fun initialize()
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun setUserProperty(name: String, value: String)
    fun log(message: String)
    fun recordException(throwable: Throwable)
}
