package org.ferdidrgn.hudaquran.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import org.ferdidrgn.hudaquran.data.local.AppPreferences
import org.ferdidrgn.hudaquran.data.local.PrayerNotificationSound

/** Gentle channel: default importance (a sound, no pop-over), the app's soft chime. */
private const val CHANNEL_PREFIX = "huda_prayer_v2_"
private const val LEGACY_CHANNEL_ID = "huda_quran_prayer"

private const val EXTRA_ID = "reminder_id"
private const val EXTRA_TITLE = "reminder_title"
private const val EXTRA_BODY = "reminder_body"

private const val STORE = "huda_reminders"
private const val KEY_PLANNED = "planned"
private const val LEGACY_REQUEST_CODE_BASE = 5000

private val json = Json { ignoreUnknownKeys = true }

actual class PrayerNotificationScheduler actual constructor() {
    private val context get() = AppContextHolder.context

    actual fun schedule(reminders: List<Reminder>) {
        ensureChannel(context)
        cancelAll()
        reminders.filter { it.scheduleNotification }.forEach { arm(context, it) }
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit()
            .putString(KEY_PLANNED, json.encodeToString(ListSerializer(Reminder.serializer()), reminders))
            .apply()
    }

    actual fun cancelAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        plannedReminders(context).forEach { alarmManager.cancel(pendingIntentFor(context, it.id, null)) }
        // Alarms armed by the previous version of this scheduler.
        repeat(5) { alarmManager.cancel(pendingIntentFor(context, LEGACY_REQUEST_CODE_BASE + it, null)) }
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit().remove(KEY_PLANNED).apply()
    }

    actual fun preview(title: String, body: String) {
        ensureChannel(context)
        postNotification(context, -42, title, body)
    }
}

/** Reads the latest plan for platform surfaces such as the Android prayer widget. */
fun loadPlannedReminders(context: Context): List<Reminder> {
    val raw = context.getSharedPreferences(STORE, Context.MODE_PRIVATE).getString(KEY_PLANNED, null) ?: return emptyList()
    return runCatching { json.decodeFromString(ListSerializer(Reminder.serializer()), raw) }.getOrDefault(emptyList())
}

private fun plannedReminders(context: Context): List<Reminder> = loadPlannedReminders(context)

private fun pendingIntentFor(context: Context, id: Int, reminder: Reminder?): PendingIntent {
    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
        if (reminder != null) {
            putExtra(EXTRA_ID, reminder.id)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_BODY, reminder.body)
        }
    }
    return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

/**
 * Exact when the user allows exact alarms (granted by default before Android 14), otherwise a
 * two-minute window — still on time for a 10-minute heads-up, and allowed without the permission.
 */
private fun arm(context: Context, reminder: Reminder) {
    if (reminder.atEpochMillis <= System.currentTimeMillis()) return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = pendingIntentFor(context, reminder.id, reminder)
    runCatching {
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.atEpochMillis, pendingIntent)
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, reminder.atEpochMillis - 60_000L, 120_000L, pendingIntent)
        }
    }
}

private fun channelId(context: Context): String {
    val prefs = AppPreferences()
    return CHANNEL_PREFIX + prefs.prayerNotificationSound.value.name.lowercase() +
        if (prefs.prayerNotificationVibration.value) "_v" else "_s"
}

private fun ensureChannel(context: Context): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "legacy"
    val manager = context.getSystemService(NotificationManager::class.java)
    // The old channel was high-importance with the default system sound — the harsh alert users
    // disliked. A channel's sound can't be changed after creation, hence the new id.
    manager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
    val id = channelId(context)
    if (manager.getNotificationChannel(id) != null) return id
    val prefs = AppPreferences()
    val channel = NotificationChannel(id, "Namaz vakti ve hatırlatmalar", NotificationManager.IMPORTANCE_DEFAULT).apply {
        description = "Namazdan önce ve mübarek günlerde yumuşak bir çan sesiyle hatırlatır."
        val soundName = when (prefs.prayerNotificationSound.value) {
            PrayerNotificationSound.ADHAN_1 -> "azan_1"
            PrayerNotificationSound.ADHAN_2 -> "azan_2"
            PrayerNotificationSound.ADHAN_3 -> "azan_3"
            PrayerNotificationSound.BEEP -> "noti_beep"
            PrayerNotificationSound.DOUBLE_BEEP -> "noti_beep_beep"
            else -> "huda_chime_soft"
        }
        val requestedSoundId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        val fallbackSoundId = context.resources.getIdentifier("huda_chime_soft", "raw", context.packageName)
        val soundId = when {
            prefs.prayerNotificationSound.value == PrayerNotificationSound.SILENT -> 0
            requestedSoundId != 0 -> requestedSoundId
            fallbackSoundId != 0 -> fallbackSoundId
            else -> 0
        }
        if (soundId != 0) {
            setSound(
                Uri.parse("android.resource://${context.packageName}/$soundId"),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
        }
        // If neither the selected file nor the bundled fallback exists, explicitly silence the
        // channel instead of allowing Android to substitute the user's default alert sound.
        if (soundId == 0) setSound(null, null)
        enableVibration(prefs.prayerNotificationVibration.value)
        vibrationPattern = if (prefs.prayerNotificationVibration.value) longArrayOf(0, 60, 140, 60) else longArrayOf(0)
        lightColor = NOTIFICATION_ACCENT_COLOR
        enableLights(true)
    }
    manager.createNotificationChannel(channel)
    return id
}

private fun postNotification(context: Context, id: Int, title: String, body: String) {
    val channel = ensureChannel(context)
    val notification = NotificationCompat.Builder(context, channel)
        .setContentTitle(title).setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setSmallIcon(context.notificationIconRes()).setColor(NOTIFICATION_ACCENT_COLOR)
        .setContentIntent(context.openAppPendingIntent(id))
        .setCategory(NotificationCompat.CATEGORY_REMINDER).setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true).setTimeoutAfter(60 * 60_000L).build()
    (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(id, notification)
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        runCatching { postNotification(context, id, title, body) }
    }
}

/**
 * Alarms don't survive a reboot or a clock/time-zone change: re-arm the planned reminders that
 * are still ahead (no network needed — the plan is stored when it's made).
 */
class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppContextHolder.init(context.applicationContext)
        if (!AppPreferences().prayerBootReschedule.value || !AppPreferences().prayerNotificationsEnabled.value) return
        ensureChannel(context)
        plannedReminders(context).filter { it.scheduleNotification }.forEach { arm(context, it) }
    }
}
