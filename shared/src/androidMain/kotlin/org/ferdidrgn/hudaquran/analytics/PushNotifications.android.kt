package org.ferdidrgn.hudaquran.analytics

import com.google.firebase.messaging.FirebaseMessaging

actual object PushNotifications {
    actual fun initialize() {
        runCatching {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) AppAnalytics.log("fcm_token_ready")
            }
        }
    }

    actual fun subscribeToTopic(topic: String) {
        runCatching { FirebaseMessaging.getInstance().subscribeToTopic(topic) }
    }

    actual fun unsubscribeFromTopic(topic: String) {
        runCatching { FirebaseMessaging.getInstance().unsubscribeFromTopic(topic) }
    }
}
