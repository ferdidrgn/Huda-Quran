package org.ferdidrgn.hudaquran.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.icu.util.IslamicCalendar
import android.widget.RemoteViews
import org.ferdidrgn.hudaquran.MainActivity
import org.ferdidrgn.hudaquran.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Offline locale-aware Hijri date with a correctly advancing Ramadan countdown. */
class HijriRamadanWidgetProvider : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        onUpdate(context, manager, intArrayOf(appWidgetId))
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val calendar = IslamicCalendar().apply { timeInMillis = System.currentTimeMillis() }
            val month = calendar[IslamicCalendar.MONTH]
            val day = calendar[IslamicCalendar.DAY_OF_MONTH]
            val year = calendar[IslamicCalendar.YEAR]
            val monthName = android.icu.text.SimpleDateFormat(
                "MMMM",
                android.icu.util.ULocale.forLocale(Locale.getDefault()),
            ).apply { setCalendar(calendar) }.format(Date())
            val status = when {
                month == RAMADAN_MONTH -> context.getString(R.string.hijri_widget_ramadan_day, day)
                else -> context.getString(R.string.hijri_widget_until_ramadan, daysUntilRamadan(calendar))
            }
            val views = RemoteViews(context.packageName, R.layout.widget_hijri_ramadan).apply {
                setTextViewText(R.id.hijri_widget_date, "$day $monthName $year")
                setTextViewText(R.id.hijri_widget_gregorian, context.getString(
                    R.string.widget_hijri_gregorian_date,
                    SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date()),
                ))
                setTextViewText(R.id.hijri_widget_status, status)
                setOnClickPendingIntent(R.id.hijri_widget_root, openAppIntent(context, id))
            }
            WidgetAppearance.apply(context, views, id, R.id.hijri_widget_root,
                titleIds = intArrayOf(R.id.hijri_widget_date), bodyIds = intArrayOf(R.id.hijri_widget_status),
                metaIds = intArrayOf(R.id.hijri_widget_gregorian),
                textTargets = listOf(WidgetTextTarget(R.id.hijri_widget_date, 22f),
                    WidgetTextTarget(R.id.hijri_widget_status, 14f), WidgetTextTarget(R.id.hijri_widget_gregorian, 11f)))
            manager.updateAppWidget(id, views)
        }
    }

    private fun daysUntilRamadan(current: IslamicCalendar): Int {
        val today = current.clone() as IslamicCalendar
        today.set(IslamicCalendar.HOUR_OF_DAY, 0); today.set(IslamicCalendar.MINUTE, 0)
        today.set(IslamicCalendar.SECOND, 0); today.set(IslamicCalendar.MILLISECOND, 0)
        val start = today.clone() as IslamicCalendar
        start.set(IslamicCalendar.MONTH, RAMADAN_MONTH)
        start.set(IslamicCalendar.DAY_OF_MONTH, 1)
        if (start.timeInMillis <= today.timeInMillis) start.add(IslamicCalendar.YEAR, 1)
        var days = 0
        while (today.timeInMillis < start.timeInMillis && days < 400) {
            today.add(IslamicCalendar.DAY_OF_MONTH, 1)
            days++
        }
        return days
    }

    private fun openAppIntent(context: Context, id: Int): PendingIntent = PendingIntent.getActivity(
        context, REQUEST_OPEN_BASE + id,
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW; data = Uri.parse("hudaquran:/calendar")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetSettingsStore.delete(context, it) }
        super.onDeleted(context, ids)
    }

    private companion object {
        const val RAMADAN_MONTH = 8
        const val REQUEST_OPEN_BASE = 7_700
    }
}
