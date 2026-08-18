package org.ferdidrgn.hudaquran.analytics

/**
 * Thin wrapper over Firebase Cloud Messaging topic subscriptions. Incoming pushes are
 * handled natively per-platform (see HudaQuranMessagingService on Android).
 */
expect object PushNotifications {
    fun initialize()
    fun subscribeToTopic(topic: String)
    fun unsubscribeFromTopic(topic: String)
}
