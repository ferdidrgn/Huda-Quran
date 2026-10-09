package org.ferdidrgn.hudaquran.analytics

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.ferdidrgn.hudaquran.notifications.NOTIFICATION_ACCENT_COLOR
import org.ferdidrgn.hudaquran.notifications.notificationIconRes
import org.ferdidrgn.hudaquran.notifications.openAppPendingIntent

// Versioned: Android never raises an existing channel's importance, so bumping it needs a new ID.
// Also bumped when the channel's sound changes, since that likewise can't be edited in place.
private const val CHANNEL_ID = "huda_quran_push_v3"
private const val LEGACY_CHANNEL_ID = "huda_quran_push_v2"
private const val SOUND_RES_NAME = "huda_chime_soft"

class HudaQuranMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AppAnalytics.log("fcm_token_refreshed")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Huda Qur'an"
        val body = message.notification?.body ?: message.data["body"] ?: return
        ensureChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(notificationIconRes())
            .setColor(NOTIFICATION_ACCENT_COLOR)
            .setContentIntent(openAppPendingIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(CHANNEL_ID, "Bildirimler", NotificationManager.IMPORTANCE_HIGH).apply {
            val soundId = resources.getIdentifier(SOUND_RES_NAME, "raw", packageName)
            if (soundId != 0) {
                setSound(
                    Uri.parse("android.resource://$packageName/$soundId"),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
            }
        }
        manager.createNotificationChannel(channel)
    }
}
