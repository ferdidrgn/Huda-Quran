package org.ferdidrgn.hudaquran.analytics

/**
 * Web push via Firebase Cloud Messaging isn't wired up for the wasmJs target yet. Safe no-op for now.
 */
actual object PushNotifications {
    actual fun initialize() {}
    actual fun subscribeToTopic(topic: String) {}
    actual fun unsubscribeFromTopic(topic: String) {}
}
