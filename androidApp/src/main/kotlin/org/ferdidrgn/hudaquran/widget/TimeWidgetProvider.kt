package org.ferdidrgn.hudaquran.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import org.ferdidrgn.hudaquran.MainActivity
import org.ferdidrgn.hudaquran.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Glanceable clock and localized date; independent instances retain their appearance. */
class TimeWidgetProvider : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        onUpdate(context, manager, intArrayOf(appWidgetId))
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_time)
            views.setTextViewText(R.id.time_widget_date, SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date()))
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("hudaquran:/prayer-times")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(R.id.time_widget_root, PendingIntent.getActivity(
                context, REQUEST_OPEN_BASE + id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ))
            WidgetAppearance.apply(context, views, id, R.id.time_widget_root,
                titleIds = intArrayOf(R.id.time_widget_clock), metaIds = intArrayOf(R.id.time_widget_date),
                textTargets = listOf(WidgetTextTarget(R.id.time_widget_clock, 34f), WidgetTextTarget(R.id.time_widget_date, 13f)))
            manager.updateAppWidget(id, views)
        }
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetSettingsStore.delete(context, it) }
        super.onDeleted(context, ids)
    }

    companion object { private const val REQUEST_OPEN_BASE = 7_300 }
}
