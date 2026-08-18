package org.ferdidrgn.hudaquran.analytics

/**
 * Firebase Cloud Messaging on iOS needs CocoaPods (pod("FirebaseMessaging")) plus APNs
 * entitlements set up in Xcode. Wired as a safe no-op for now — see androidMain for the
 * real implementation to mirror once CocoaPods is configured.
 */
actual object PushNotifications {
    actual fun initialize() {}
    actual fun subscribeToTopic(topic: String) {}
    actual fun unsubscribeFromTopic(topic: String) {}
}
