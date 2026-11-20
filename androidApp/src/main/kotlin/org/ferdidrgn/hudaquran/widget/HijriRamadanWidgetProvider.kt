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

/** Offline Hijri date tile with a Ramadan-aware status line. */
class HijriRamadanWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val calendar = IslamicCalendar().apply { timeInMillis = System.currentTimeMillis() }
        val month = calendar[IslamicCalendar.MONTH]
        val day = calendar[IslamicCalendar.DAY_OF_MONTH]
        val year = calendar[IslamicCalendar.YEAR]
        val monthName = MONTHS[month.coerceIn(MONTHS.indices)]
        val status = when {
            month == RAMADAN_MONTH -> context.getString(R.string.hijri_widget_ramadan_day, day)
            month < RAMADAN_MONTH -> context.getString(R.string.hijri_widget_until_ramadan, daysUntilRamadan(calendar))
            else -> context.getString(R.string.hijri_widget_ramadan_next_year)
        }
        val views = RemoteViews(context.packageName, R.layout.widget_hijri_ramadan).apply {
            setTextViewText(R.id.hijri_widget_date, "$day $monthName $year")
            setTextViewText(R.id.hijri_widget_status, status)
            setOnClickPendingIntent(R.id.hijri_widget_root, openAppIntent(context))
        }
        ids.forEach { manager.updateAppWidget(it, views) }
    }

    private fun daysUntilRamadan(calendar: IslamicCalendar): Int {
        val currentDay = calendar[IslamicCalendar.DAY_OF_YEAR]
        val ramadanStart = calendar.clone() as IslamicCalendar
        ramadanStart.set(IslamicCalendar.MONTH, RAMADAN_MONTH)
        ramadanStart.set(IslamicCalendar.DAY_OF_MONTH, 1)
        val targetDay = ramadanStart[IslamicCalendar.DAY_OF_YEAR]
        return (targetDay - currentDay).coerceAtLeast(1)
    }

    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        77,
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("hudaquran:/calendar")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val RAMADAN_MONTH = 8
        val MONTHS = arrayOf(
            "Muharrem", "Safer", "Rebîülevvel", "Rebîülâhir", "Cemaziyelevvel", "Cemaziyelâhir",
            "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce",
        )
    }
}
