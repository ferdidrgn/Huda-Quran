package org.ferdidrgn.hudaquran.analytics

/**
 * Firebase iOS SDK (FirebaseAnalytics/FirebaseCrashlytics) needs CocoaPods integration
 * (kotlin("native.cocoapods") + pod("FirebaseAnalytics")/pod("FirebaseCrashlytics")) that this
 * project doesn't have set up, plus a GoogleService-Info.plist from your Firebase project.
 * Wired as a safe no-op for now — see androidMain for the real implementation to mirror.
 */
actual object AppAnalytics {
    actual fun initialize() {}
    actual fun logEvent(name: String, params: Map<String, String>) {}
    actual fun setUserProperty(name: String, value: String) {}
    actual fun log(message: String) {}
    actual fun recordException(throwable: Throwable) {}
}
