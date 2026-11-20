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
import java.util.Calendar

/** Offline daily ayah widget. It never blocks on a network request or shows advertising. */
class DailyContentWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context, manager, ids)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != Intent.ACTION_DATE_CHANGED && intent.action != Intent.ACTION_TIMEZONE_CHANGED) return
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(android.content.ComponentName(context, DailyContentWidgetProvider::class.java))
        update(context, manager, ids)
    }

    private fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = buildViews(context)
        ids.forEach { manager.updateAppWidget(it, views) }
    }

    private fun buildViews(context: Context): RemoteViews {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val item = DailyWidgetContent.forDay(context, day)
        return RemoteViews(context.packageName, R.layout.widget_daily_content).apply {
            setTextViewText(R.id.daily_widget_reference, item.reference)
            setTextViewText(R.id.daily_widget_text, item.text)
            setOnClickPendingIntent(
                R.id.daily_widget_root,
                PendingIntent.getActivity(
                    context,
                    74,
                    Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse("hudaquran:/daily")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }
    }
}
