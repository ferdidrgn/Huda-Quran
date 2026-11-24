<<<<<<< Updated upstream
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
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.MainActivity
import org.ferdidrgn.hudaquran.R
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocation
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.domain.model.PrayerTime
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.notifications.Reminder
import org.ferdidrgn.hudaquran.notifications.ReminderKind
import org.ferdidrgn.hudaquran.notifications.loadPlannedReminders
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Home-screen prayer times. Each instance follows a saved city and keeps its own appearance. */
class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refreshAsync(context, manager, ids)
        scheduleNextRefresh(context)
    }

    override fun onEnabled(context: Context) {
        refreshAsync(context, AppWidgetManager.getInstance(context), allIds(context))
        scheduleNextRefresh(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        refreshAsync(context, manager, intArrayOf(appWidgetId))
        scheduleNextRefresh(context)
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
    }

    override fun onDisabled(context: Context) {
        cancelRefresh(context)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetSettingsStore.delete(context, it) }
        super.onDeleted(context, ids)
        if (allIds(context).isEmpty()) cancelRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = allIds(context)
                if (ids.isEmpty()) {
                    cancelRefresh(context)
                } else {
                    refreshAsync(context, manager, ids)
                    scheduleNextRefresh(context)
                }
            }
        }
    }

    private fun refreshAsync(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val pending = goAsync()
        val job = SupervisorJob()
        CoroutineScope(job + Dispatchers.IO).launch {
            try {
                ids.forEach { widgetId ->
                    val settings = WidgetSettingsStore.read(context, widgetId)
                    val customLocation = prayerLocation(settings.locationId)
                    val location = customLocation ?: appPrayerLocation()
                    val data = loadPrayerData(context, location, customLocation == null)
                    val options = manager.getAppWidgetOptions(widgetId)
                    manager.updateAppWidget(widgetId, buildViews(context, manager, widgetId, location, data, options))
                }
            } finally {
                pending.finish()
                job.cancel()
            }
        }
    }

    private fun buildViews(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        location: PrayerLocation,
        data: PrayerWidgetData?,
        options: android.os.Bundle,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_prayer)
        val locationLabel = location.displayName
        views.setTextViewText(R.id.prayer_widget_location, locationLabel)
        val zone = timeZone(location)
        val currentMinute = zone?.let { currentMinute(it) }
        val nextPrayer = if (currentMinute == null) null else data?.let { nextPrayer(it.times, currentMinute) }
        if (nextPrayer == null) {
            views.setTextViewText(R.id.prayer_widget_title, context.getString(R.string.prayer_widget_placeholder))
            views.setTextViewText(R.id.prayer_widget_subtitle, context.getString(R.string.widget_prayer_unavailable))
            views.setCharSequence(
                R.id.prayer_widget_root,
                "setContentDescription",
                context.getString(R.string.widget_prayer_a11y_unavailable, locationLabel),
            )
        } else {
            val (prayer, minutesUntil) = nextPrayer
            views.setTextViewText(R.id.prayer_widget_title, prayer.label)
            views.setTextViewText(R.id.prayer_widget_subtitle, remainingText(context, minutesUntil))
            views.setCharSequence(
                R.id.prayer_widget_root,
                "setContentDescription",
                context.getString(
                    R.string.widget_prayer_a11y_next,
                    locationLabel,
                    prayer.label,
                    prayer.time,
                    remainingText(context, minutesUntil),
                ),
            )
        }

        val date = zone?.let { timeZone -> data?.date?.let { date ->
            val format = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            format.timeZone = timeZone
            format.format(Date(date.timeInMillis))
        } }.orEmpty()
        views.setTextViewText(R.id.prayer_widget_date, date)

        val isLarge = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0) >= SCHEDULE_MIN_HEIGHT_DP
        val settings = WidgetSettingsStore.read(context, widgetId)
        val showSchedule = data != null && isLarge && settings.density != WidgetSettings.DENSITY_COMPACT
        views.setViewVisibility(R.id.prayer_widget_schedule, if (showSchedule) View.VISIBLE else View.GONE)
        val nameIds = intArrayOf(
            R.id.prayer_widget_name_1,
            R.id.prayer_widget_name_2,
            R.id.prayer_widget_name_3,
            R.id.prayer_widget_name_4,
            R.id.prayer_widget_name_5,
        )
        val timeIds = intArrayOf(
            R.id.prayer_widget_time_1,
            R.id.prayer_widget_time_2,
            R.id.prayer_widget_time_3,
            R.id.prayer_widget_time_4,
            R.id.prayer_widget_time_5,
        )
        data?.times?.prayers?.take(5)?.forEachIndexed { index, prayer ->
            views.setTextViewText(nameIds[index], prayer.label)
            views.setTextViewText(timeIds[index], prayer.time)
        }
        nameIds.drop(data?.times?.prayers?.size ?: 0).forEach { views.setTextViewText(it, "") }
        timeIds.drop(data?.times?.prayers?.size ?: 0).forEach { views.setTextViewText(it, "") }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            this.data = Uri.parse("hudaquran:/prayer-times")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        views.setOnClickPendingIntent(
            R.id.prayer_widget_root,
            PendingIntent.getActivity(
                context,
                REQUEST_OPEN_BASE + widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        WidgetAppearance.apply(
            context,
            views,
            widgetId,
            R.id.prayer_widget_root,
            titleIds = intArrayOf(R.id.prayer_widget_title),
            bodyIds = nameIds + timeIds + R.id.prayer_widget_subtitle,
            metaIds = intArrayOf(R.id.prayer_widget_location, R.id.prayer_widget_date),
            textTargets = listOf(
                WidgetTextTarget(R.id.prayer_widget_location, 12f),
                WidgetTextTarget(R.id.prayer_widget_title, 22f),
                WidgetTextTarget(R.id.prayer_widget_subtitle, 14f),
                WidgetTextTarget(R.id.prayer_widget_date, 11f),
            ) + nameIds.map { WidgetTextTarget(it, 12f) } + timeIds.map { WidgetTextTarget(it, 13f) },
        )
        return views
    }

    private suspend fun loadPrayerData(
        context: Context,
        location: PrayerLocation,
        isAppLocation: Boolean,
    ): PrayerWidgetData? {
        val zone = timeZone(location) ?: return null
        val calendar = Calendar.getInstance(zone)
        val localDate = kotlinx.datetime.LocalDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
        val dateKey = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply { timeZone = zone }.format(calendar.time)
        val key = "timings_${location.city}_${location.country}_$dateKey"
        val preferences = context.getSharedPreferences(CACHE_STORE, Context.MODE_PRIVATE)
        if (isAppLocation) {
            val planned = plannedForToday(context, location, calendar)
            if (planned != null) return PrayerWidgetData(planned, calendar)
        }
        val cached = preferences.getString(key, null)?.let(::decodeTimes)
        if (cached != null) return PrayerWidgetData(cached, calendar)
        val fetched = runCatching {
            AppContainer.prayerRepository.getTimings(location.city, location.country, localDate)
        }.getOrNull()
        val times = fetched ?: return null
        preferences.edit().putString(key, encodeTimes(times)).apply()
        return PrayerWidgetData(times, calendar)
    }

    private fun plannedForToday(context: Context, location: PrayerLocation, calendar: Calendar): PrayerTimes? {
        val zone = timeZone(location) ?: return null
        if (location.city != AppContainer.preferences.prayerCity || location.country != AppContainer.preferences.prayerCountry) {
            return null
        }
        val dayStart = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val dayEnd = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = zone }
        val reminders = loadPlannedReminders(context).filter {
            it.kind == ReminderKind.PRAYER_WIDGET && it.atEpochMillis in dayStart until dayEnd
        }.sortedBy { it.atEpochMillis }
        if (reminders.size < 5) return null
        return PrayerTimes(
            reminders.take(5).mapIndexed { index, reminder ->
                PrayerTime("prayer_$index", reminder.title, formatter.format(Date(reminder.atEpochMillis)))
            },
        )
    }

    private fun decodeTimes(encoded: String): PrayerTimes? = runCatching {
        val rows = JSONArray(encoded)
        val prayers = buildList {
            for (index in 0 until rows.length()) {
                val row = rows.getJSONObject(index)
                add(PrayerTime(row.getString("key"), row.getString("label"), row.getString("time")))
            }
        }
        if (prayers.size != 5 || prayers.any { it.time.isBlank() }) return null
        PrayerTimes(prayers)
    }.getOrNull()

    private fun encodeTimes(times: PrayerTimes): String = JSONArray().apply {
        times.prayers.take(5).forEach { prayer ->
            put(JSONObject().put("key", prayer.key).put("label", prayer.label).put("time", prayer.time))
        }
    }.toString()

    private fun appPrayerLocation(): PrayerLocation {
        val city = AppContainer.preferences.prayerCity
        val country = AppContainer.preferences.prayerCountry
        return PrayerLocations.all.firstOrNull { it.city == city && it.country == country }
            ?: PrayerLocation(city, country, city)
    }

    private fun prayerLocation(locationId: String): PrayerLocation? {
        if (locationId.isBlank()) return null
        val parts = locationId.split('|', limit = 2)
        val city = parts.getOrNull(0)?.trim().orEmpty()
        val country = parts.getOrNull(1)?.trim().orEmpty()
        if (city.isBlank() || country.isBlank()) return null
        return PrayerLocations.all.firstOrNull { it.city == city && it.country == country }
            ?: PrayerLocation(city, country, city)
    }

    private fun currentMinute(zone: TimeZone): Int {
        val calendar = Calendar.getInstance(zone)
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    private fun timeZone(location: PrayerLocation): TimeZone? {
        // This map intentionally covers the finite PrayerLocations catalog only. Multi-zone
        // countries are resolved by city; an unsupported/custom location never inherits the
        // device zone and therefore cannot display a misleading countdown.
        val id = if (location.country == "Turkey" && location.city in TURKEY_CITIES) {
            "Europe/Istanbul"
        } else when (location.country to location.city) {
            "Saudi Arabia" to "Mecca", "Saudi Arabia" to "Medina", "Saudi Arabia" to "Riyadh" -> "Asia/Riyadh"
            "Germany" to "Berlin", "Germany" to "Cologne", "Germany" to "Munich", "Germany" to "Frankfurt" -> "Europe/Berlin"
            "France" to "Paris", "France" to "Lyon" -> "Europe/Paris"
            "Uzbekistan" to "Tashkent" -> "Asia/Tashkent"
            "Uzbekistan" to "Samarkand" -> "Asia/Samarkand"
            "Kyrgyzstan" to "Bishkek" -> "Asia/Bishkek"
            "Kyrgyzstan" to "Osh" -> "Asia/Bishkek"
            "Turkmenistan" to "Ashgabat" -> "Asia/Ashgabat"
            "United Kingdom" to "London" -> "Europe/London"
            "United States" to "New York" -> "America/New_York"
            "Egypt" to "Cairo" -> "Africa/Cairo"
            else -> return null
        }
        return TimeZone.getTimeZone(id)
    }

    private fun nextPrayer(times: PrayerTimes, nowMinute: Int): Pair<PrayerTime, Int>? {
        val parsed = times.prayers.take(5).mapNotNull { prayer ->
            val parts = prayer.time.split(':')
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val minute = parts.getOrNull(1)?.take(2)?.toIntOrNull() ?: return@mapNotNull null
            if (hour !in 0..23 || minute !in 0..59) return@mapNotNull null
            prayer to (hour * 60 + minute)
        }
        if (parsed.isEmpty()) return null
        val upcoming = parsed.filter { it.second > nowMinute }.minByOrNull { it.second }
        val picked = upcoming ?: parsed.minByOrNull { it.second } ?: return null
        val minutesUntil = if (upcoming != null) picked.second - nowMinute else (24 * 60 - nowMinute) + picked.second
        return picked.first to minutesUntil
    }

    private fun remainingText(context: Context, minutes: Int): String {
        val hours = minutes / 60
        val remainder = minutes % 60
        return when {
            hours > 0 -> context.getString(R.string.widget_prayer_remaining_hours, hours, remainder)
            else -> context.getString(R.string.widget_prayer_remaining_minutes, maxOf(minutes, 1))
        }
    }

    private fun allIds(context: Context): IntArray =
        AppWidgetManager.getInstance(context).getAppWidgetIds(componentName(context))

    private fun componentName(context: Context) = ComponentName(context, PrayerWidgetProvider::class.java)

    private fun refreshPendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REFRESH_REQUEST_CODE,
        Intent(context, PrayerWidgetProvider::class.java).setAction(ACTION_REFRESH),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun scheduleNextRefresh(context: Context) {
        if (allIds(context).isEmpty()) {
            cancelRefresh(context)
            return
        }
        val now = System.currentTimeMillis()
        val at = ((now / MINUTE_MILLIS) + 1) * MINUTE_MILLIS
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = refreshPendingIntent(context)
        runCatching {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canExact) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent)
            else alarmManager.setWindow(AlarmManager.RTC_WAKEUP, at, MINUTE_MILLIS, pendingIntent)
        }
    }

    private fun cancelRefresh(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(refreshPendingIntent(context))
    }

    private data class PrayerWidgetData(val times: PrayerTimes, val date: Calendar)

    companion object {
        private const val ACTION_REFRESH = "org.ferdidrgn.hudaquran.widget.PRAYER_REFRESH"
        private const val CACHE_STORE = "huda_widget_prayer_times"
        private const val REFRESH_REQUEST_CODE = 72
        private const val REQUEST_OPEN_BASE = 5_000
        private const val MINUTE_MILLIS = 60_000L
        private const val SCHEDULE_MIN_HEIGHT_DP = 140
        private val TURKEY_CITIES = PrayerLocations.all.filter { it.country == "Turkey" }.map { it.city }.toSet()
    }
}

/** Pure reminder-plan logic retained for the app's existing scheduling path and regression tests. */
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

=======
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
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.MainActivity
import org.ferdidrgn.hudaquran.R
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocation
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.domain.model.PrayerTime
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.notifications.Reminder
import org.ferdidrgn.hudaquran.notifications.ReminderKind
import org.ferdidrgn.hudaquran.notifications.loadPlannedReminders
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Home-screen prayer times. Each instance follows a saved city and keeps its own appearance. */
class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refreshAsync(context, manager, ids)
        scheduleNextRefresh(context)
    }

    override fun onEnabled(context: Context) {
        refreshAsync(context, AppWidgetManager.getInstance(context), allIds(context))
        scheduleNextRefresh(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        refreshAsync(context, manager, intArrayOf(appWidgetId))
        scheduleNextRefresh(context)
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
    }

    override fun onDisabled(context: Context) {
        cancelRefresh(context)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetSettingsStore.delete(context, it) }
        super.onDeleted(context, ids)
        if (allIds(context).isEmpty()) cancelRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = allIds(context)
                if (ids.isEmpty()) {
                    cancelRefresh(context)
                } else {
                    refreshAsync(context, manager, ids)
                    scheduleNextRefresh(context)
                }
            }
        }
    }

    private fun refreshAsync(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val pending = goAsync()
        val job = SupervisorJob()
        CoroutineScope(job + Dispatchers.IO).launch {
            try {
                ids.forEach { widgetId ->
                    val settings = WidgetSettingsStore.read(context, widgetId)
                    val customLocation = prayerLocation(settings.locationId)
                    val location = customLocation ?: appPrayerLocation()
                    val data = loadPrayerData(context, location, customLocation == null)
                    val options = manager.getAppWidgetOptions(widgetId)
                    manager.updateAppWidget(widgetId, buildViews(context, manager, widgetId, location, data, options))
                }
            } finally {
                pending.finish()
                job.cancel()
            }
        }
    }

    private fun buildViews(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        location: PrayerLocation,
        data: PrayerWidgetData?,
        options: android.os.Bundle,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_prayer)
        val locationLabel = location.displayName
        views.setTextViewText(R.id.prayer_widget_location, locationLabel)
        val zone = timeZone(location)
        val currentMinute = zone?.let { currentMinute(it) }
        val nextPrayer = if (currentMinute == null) null else data?.let { nextPrayer(it.times, currentMinute) }
        if (nextPrayer == null) {
            views.setTextViewText(R.id.prayer_widget_title, context.getString(R.string.prayer_widget_placeholder))
            views.setTextViewText(R.id.prayer_widget_subtitle, context.getString(R.string.widget_prayer_unavailable))
            views.setCharSequence(
                R.id.prayer_widget_root,
                "setContentDescription",
                context.getString(R.string.widget_prayer_a11y_unavailable, locationLabel),
            )
        } else {
            val (prayer, minutesUntil) = nextPrayer
            views.setTextViewText(R.id.prayer_widget_title, prayer.label)
            views.setTextViewText(R.id.prayer_widget_subtitle, remainingText(context, minutesUntil))
            views.setCharSequence(
                R.id.prayer_widget_root,
                "setContentDescription",
                context.getString(
                    R.string.widget_prayer_a11y_next,
                    locationLabel,
                    prayer.label,
                    prayer.time,
                    remainingText(context, minutesUntil),
                ),
            )
        }

        val date = zone?.let { timeZone -> data?.date?.let { date ->
            val format = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            format.timeZone = timeZone
            format.format(Date(date.timeInMillis))
        } }.orEmpty()
        views.setTextViewText(R.id.prayer_widget_date, date)

        val isLarge = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0) >= SCHEDULE_MIN_HEIGHT_DP
        val settings = WidgetSettingsStore.read(context, widgetId)
        val showSchedule = data != null && isLarge && settings.density != WidgetSettings.DENSITY_COMPACT
        views.setViewVisibility(R.id.prayer_widget_schedule, if (showSchedule) View.VISIBLE else View.GONE)
        val nameIds = intArrayOf(
            R.id.prayer_widget_name_1,
            R.id.prayer_widget_name_2,
            R.id.prayer_widget_name_3,
            R.id.prayer_widget_name_4,
            R.id.prayer_widget_name_5,
        )
        val timeIds = intArrayOf(
            R.id.prayer_widget_time_1,
            R.id.prayer_widget_time_2,
            R.id.prayer_widget_time_3,
            R.id.prayer_widget_time_4,
            R.id.prayer_widget_time_5,
        )
        data?.times?.prayers?.take(5)?.forEachIndexed { index, prayer ->
            views.setTextViewText(nameIds[index], prayer.label)
            views.setTextViewText(timeIds[index], prayer.time)
        }
        nameIds.drop(data?.times?.prayers?.size ?: 0).forEach { views.setTextViewText(it, "") }
        timeIds.drop(data?.times?.prayers?.size ?: 0).forEach { views.setTextViewText(it, "") }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            this.data = Uri.parse("hudaquran:/prayer-times")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        views.setOnClickPendingIntent(
            R.id.prayer_widget_root,
            PendingIntent.getActivity(
                context,
                REQUEST_OPEN_BASE + widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        WidgetAppearance.apply(
            context,
            views,
            widgetId,
            R.id.prayer_widget_root,
            titleIds = intArrayOf(R.id.prayer_widget_title),
            bodyIds = nameIds + timeIds + R.id.prayer_widget_subtitle,
            metaIds = intArrayOf(R.id.prayer_widget_location, R.id.prayer_widget_date),
            textTargets = listOf(
                WidgetTextTarget(R.id.prayer_widget_location, 12f),
                WidgetTextTarget(R.id.prayer_widget_title, 22f),
                WidgetTextTarget(R.id.prayer_widget_subtitle, 14f),
                WidgetTextTarget(R.id.prayer_widget_date, 11f),
            ) + nameIds.map { WidgetTextTarget(it, 12f) } + timeIds.map { WidgetTextTarget(it, 13f) },
        )
        return views
    }

    private suspend fun loadPrayerData(
        context: Context,
        location: PrayerLocation,
        isAppLocation: Boolean,
    ): PrayerWidgetData? {
        val zone = timeZone(location) ?: return null
        val calendar = Calendar.getInstance(zone)
        val localDate = kotlinx.datetime.LocalDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
        val dateKey = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply { timeZone = zone }.format(calendar.time)
        val key = "timings_${location.city}_${location.country}_$dateKey"
        val preferences = context.getSharedPreferences(CACHE_STORE, Context.MODE_PRIVATE)
        if (isAppLocation) {
            val planned = plannedForToday(context, location, calendar)
            if (planned != null) return PrayerWidgetData(planned, calendar)
        }
        val cached = preferences.getString(key, null)?.let(::decodeTimes)
        if (cached != null) return PrayerWidgetData(cached, calendar)
        val fetched = runCatching {
            AppContainer.prayerRepository.getTimings(location.city, location.country, localDate)
        }.getOrNull()
        val times = fetched ?: return null
        preferences.edit().putString(key, encodeTimes(times)).apply()
        return PrayerWidgetData(times, calendar)
    }

    private fun plannedForToday(context: Context, location: PrayerLocation, calendar: Calendar): PrayerTimes? {
        val zone = timeZone(location) ?: return null
        if (location.city != AppContainer.preferences.prayerCity || location.country != AppContainer.preferences.prayerCountry) {
            return null
        }
        val dayStart = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val dayEnd = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = zone }
        val reminders = loadPlannedReminders(context).filter {
            it.kind == ReminderKind.PRAYER_WIDGET && it.atEpochMillis in dayStart until dayEnd
        }.sortedBy { it.atEpochMillis }
        if (reminders.size < 5) return null
        return PrayerTimes(
            reminders.take(5).mapIndexed { index, reminder ->
                PrayerTime("prayer_$index", reminder.title, formatter.format(Date(reminder.atEpochMillis)))
            },
        )
    }

    private fun decodeTimes(encoded: String): PrayerTimes? = runCatching {
        val rows = JSONArray(encoded)
        val prayers = buildList {
            for (index in 0 until rows.length()) {
                val row = rows.getJSONObject(index)
                add(PrayerTime(row.getString("key"), row.getString("label"), row.getString("time")))
            }
        }
        if (prayers.size != 5 || prayers.any { it.time.isBlank() }) return null
        PrayerTimes(prayers)
    }.getOrNull()

    private fun encodeTimes(times: PrayerTimes): String = JSONArray().apply {
        times.prayers.take(5).forEach { prayer ->
            put(JSONObject().put("key", prayer.key).put("label", prayer.label).put("time", prayer.time))
        }
    }.toString()

    private fun appPrayerLocation(): PrayerLocation {
        val city = AppContainer.preferences.prayerCity
        val country = AppContainer.preferences.prayerCountry
        return PrayerLocations.all.firstOrNull { it.city == city && it.country == country }
            ?: PrayerLocation(city, country, city)
    }

    private fun prayerLocation(locationId: String): PrayerLocation? {
        if (locationId.isBlank()) return null
        val parts = locationId.split('|', limit = 2)
        val city = parts.getOrNull(0)?.trim().orEmpty()
        val country = parts.getOrNull(1)?.trim().orEmpty()
        if (city.isBlank() || country.isBlank()) return null
        return PrayerLocations.all.firstOrNull { it.city == city && it.country == country }
            ?: PrayerLocation(city, country, city)
    }

    private fun currentMinute(zone: TimeZone): Int {
        val calendar = Calendar.getInstance(zone)
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    private fun timeZone(location: PrayerLocation): TimeZone? {
        // This map intentionally covers the finite PrayerLocations catalog only. Multi-zone
        // countries are resolved by city; an unsupported/custom location never inherits the
        // device zone and therefore cannot display a misleading countdown.
        val id = if (location.country == "Turkey" && location.city in TURKEY_CITIES) {
            "Europe/Istanbul"
        } else when (location.country to location.city) {
            "Saudi Arabia" to "Mecca", "Saudi Arabia" to "Medina", "Saudi Arabia" to "Riyadh" -> "Asia/Riyadh"
            "Germany" to "Berlin", "Germany" to "Cologne", "Germany" to "Munich", "Germany" to "Frankfurt" -> "Europe/Berlin"
            "France" to "Paris", "France" to "Lyon" -> "Europe/Paris"
            "Uzbekistan" to "Tashkent" -> "Asia/Tashkent"
            "Uzbekistan" to "Samarkand" -> "Asia/Samarkand"
            "Kyrgyzstan" to "Bishkek" -> "Asia/Bishkek"
            "Kyrgyzstan" to "Osh" -> "Asia/Bishkek"
            "Turkmenistan" to "Ashgabat" -> "Asia/Ashgabat"
            "United Kingdom" to "London" -> "Europe/London"
            "United States" to "New York" -> "America/New_York"
            "Egypt" to "Cairo" -> "Africa/Cairo"
            else -> return null
        }
        return TimeZone.getTimeZone(id)
    }

    private fun nextPrayer(times: PrayerTimes, nowMinute: Int): Pair<PrayerTime, Int>? {
        val parsed = times.prayers.take(5).mapNotNull { prayer ->
            val parts = prayer.time.split(':')
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val minute = parts.getOrNull(1)?.take(2)?.toIntOrNull() ?: return@mapNotNull null
            if (hour !in 0..23 || minute !in 0..59) return@mapNotNull null
            prayer to (hour * 60 + minute)
        }
        if (parsed.isEmpty()) return null
        val upcoming = parsed.filter { it.second > nowMinute }.minByOrNull { it.second }
        val picked = upcoming ?: parsed.minByOrNull { it.second } ?: return null
        val minutesUntil = if (upcoming != null) picked.second - nowMinute else (24 * 60 - nowMinute) + picked.second
        return picked.first to minutesUntil
    }

    private fun remainingText(context: Context, minutes: Int): String {
        val hours = minutes / 60
        val remainder = minutes % 60
        return when {
            hours > 0 -> context.getString(R.string.widget_prayer_remaining_hours, hours, remainder)
            else -> context.getString(R.string.widget_prayer_remaining_minutes, maxOf(minutes, 1))
        }
    }

    private fun allIds(context: Context): IntArray =
        AppWidgetManager.getInstance(context).getAppWidgetIds(componentName(context))

    private fun componentName(context: Context) = ComponentName(context, PrayerWidgetProvider::class.java)

    private fun refreshPendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REFRESH_REQUEST_CODE,
        Intent(context, PrayerWidgetProvider::class.java).setAction(ACTION_REFRESH),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun scheduleNextRefresh(context: Context) {
        if (allIds(context).isEmpty()) {
            cancelRefresh(context)
            return
        }
        val now = System.currentTimeMillis()
        val at = ((now / MINUTE_MILLIS) + 1) * MINUTE_MILLIS
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = refreshPendingIntent(context)
        runCatching {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canExact) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent)
            else alarmManager.setWindow(AlarmManager.RTC_WAKEUP, at, MINUTE_MILLIS, pendingIntent)
        }
    }

    private fun cancelRefresh(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(refreshPendingIntent(context))
    }

    private data class PrayerWidgetData(val times: PrayerTimes, val date: Calendar)

    companion object {
        private const val ACTION_REFRESH = "org.ferdidrgn.hudaquran.widget.PRAYER_REFRESH"
        private const val CACHE_STORE = "huda_widget_prayer_times"
        private const val REFRESH_REQUEST_CODE = 72
        private const val REQUEST_OPEN_BASE = 5_000
        private const val MINUTE_MILLIS = 60_000L
        private const val SCHEDULE_MIN_HEIGHT_DP = 140
        private val TURKEY_CITIES = PrayerLocations.all.filter { it.country == "Turkey" }.map { it.city }.toSet()
    }
}

/** Pure reminder-plan logic retained for the app's existing scheduling path and regression tests. */
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
>>>>>>> Stashed changes
