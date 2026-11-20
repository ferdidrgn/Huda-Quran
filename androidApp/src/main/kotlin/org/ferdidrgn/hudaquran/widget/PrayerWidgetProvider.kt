package org.ferdidrgn.hudaquran.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import org.ferdidrgn.hudaquran.MainActivity
import org.ferdidrgn.hudaquran.R
import org.ferdidrgn.hudaquran.notifications.Reminder
import org.ferdidrgn.hudaquran.notifications.ReminderKind
import org.ferdidrgn.hudaquran.notifications.loadPlannedReminders
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** A network-free prayer widget backed by the scheduler's next real prayer time. */
class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context, manager, ids)
        scheduleNextRefresh(context)
    }

    override fun onEnabled(context: Context) {
        scheduleNextRefresh(context)
    }

    override fun onDisabled(context: Context) {
        cancelRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(componentName(context))
                if (ids.isEmpty()) {
                    cancelRefresh(context)
                } else {
                    update(context, manager, ids)
                    scheduleNextRefresh(context)
                }
            }
        }
    }

    private fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = buildViews(context)
        ids.forEach { manager.updateAppWidget(it, views) }
    }

    companion object {
        private const val ACTION_REFRESH = "org.ferdidrgn.hudaquran.widget.PRAYER_REFRESH"
        private const val REFRESH_REQUEST_CODE = 72
        private const val MINUTE_MILLIS = 60_000L
        private const val EMPTY_PLAN_REFRESH_MILLIS = 30 * MINUTE_MILLIS
        private const val STORE = "huda_reminders"
        private const val KEY_PLANNED = "planned"

        private fun buildViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer)
            val next = PrayerWidgetLogic.display(loadPlannedReminders(context), System.currentTimeMillis())
            if (next == null) {
                views.setTextViewText(R.id.prayer_widget_title, context.getString(R.string.prayer_widget_placeholder))
                views.setTextViewText(R.id.prayer_widget_subtitle, context.getString(R.string.prayer_widget_subtitle_placeholder))
            } else {
                views.setTextViewText(R.id.prayer_widget_title, next.first)
                views.setTextViewText(R.id.prayer_widget_subtitle, next.second)
            }
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("hudaquran:/prayer-times")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.prayer_widget_root,
                PendingIntent.getActivity(context, 71, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
            )
            return views
        }

        private fun componentName(context: Context) = ComponentName(context, PrayerWidgetProvider::class.java)

        private fun refreshPendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            REFRESH_REQUEST_CODE,
            Intent(context, PrayerWidgetProvider::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        private fun scheduleNextRefresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            if (manager.getAppWidgetIds(componentName(context)).isEmpty()) {
                cancelRefresh(context)
                return
            }
            val now = System.currentTimeMillis()
            val at = PrayerWidgetLogic.nextRefreshAt(loadPlannedReminders(context), now, MINUTE_MILLIS, EMPTY_PLAN_REFRESH_MILLIS)
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val pendingIntent = refreshPendingIntent(context)
            runCatching {
                val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
                if (canExact) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent)
                } else {
                    // A one-minute window avoids SCHEDULE_EXACT_ALARM permission while keeping
                    // the countdown honest on devices that deny exact alarms.
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, at, MINUTE_MILLIS, pendingIntent)
                }
            }
        }

        private fun cancelRefresh(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            alarmManager.cancel(refreshPendingIntent(context))
        }
    }
}

/** Pure widget decisions kept separate so time-boundary and stale-plan cases can be tested. */
internal object PrayerWidgetLogic {
    fun next(reminders: List<Reminder>, now: Long): Reminder? = reminders
        .asSequence()
        .filter { it.kind == ReminderKind.PRAYER_WIDGET && it.atEpochMillis > now }
        .minByOrNull { it.atEpochMillis }

    fun display(reminders: List<Reminder>, now: Long): Pair<String, String>? {
        val candidate = next(reminders, now) ?: return null
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(candidate.atEpochMillis))
        val remaining = candidate.atEpochMillis - now
        val totalMinutes = ((remaining + 59_999L) / 60_000L).coerceAtLeast(1L)
        val remainingText = when {
            totalMinutes >= 60L -> "${totalMinutes / 60} sa ${totalMinutes % 60} dk kaldı"
            else -> "$totalMinutes dk kaldı"
        }
        return candidate.title.trim().take(80) to "$time · $remainingText"
    }

    fun nextRefreshAt(reminders: List<Reminder>, now: Long, minuteMillis: Long, emptyPlanMillis: Long): Long {
        val nextAt = next(reminders, now)?.atEpochMillis
        val desired = nextAt?.let { minOf(it, now + minuteMillis) } ?: (now + emptyPlanMillis)
        return desired.coerceAtLeast(now + 1_000L)
    }
}
