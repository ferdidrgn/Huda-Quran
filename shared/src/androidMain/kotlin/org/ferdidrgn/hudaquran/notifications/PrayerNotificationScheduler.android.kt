package org.ferdidrgn.hudaquran.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import java.util.Calendar

private const val CHANNEL_ID = "huda_quran_prayer"
private const val EXTRA_LABEL = "prayer_label"
private const val REQUEST_CODE_BASE = 5000

actual class PrayerNotificationScheduler actual constructor() {
    private val context get() = AppContextHolder.context
    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    actual fun scheduleToday(prayerTimes: PrayerTimes) {
        ensureChannel()
        cancelAll()
        val now = Calendar.getInstance()
        prayerTimes.prayers.forEachIndexed { index, prayer ->
            val parts = prayer.time.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@forEachIndexed
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: return@forEachIndexed

            val trigger = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (trigger.before(now)) return@forEachIndexed

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(EXTRA_LABEL, prayer.label)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            runCatching {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pendingIntent)
            }
        }
    }

    actual fun cancelAll() {
        repeat(5) { index ->
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Namaz Vakti Bildirimleri", NotificationManager.IMPORTANCE_HIGH),
            )
        }
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra(EXTRA_LABEL) ?: "Namaz"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("$label Vakti")
            .setContentText("$label vakti girdi.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(label.hashCode(), notification)
    }
}
