package org.ferdidrgn.hudaquran.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import org.ferdidrgn.hudaquran.MainActivity
import org.ferdidrgn.hudaquran.R

/** A small, offline counter. Each widget instance keeps its own count. */
class DhikrWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_INCREMENT && intent.action != ACTION_RESET) return
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID_WIDGET_ID)
        if (widgetId == INVALID_WIDGET_ID || !isPlacedWidget(context, widgetId)) return
        val preferences = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
        val key = countKey(widgetId)
        val next = if (intent.action == ACTION_RESET) 0 else preferences.getInt(key, 0).coerceAtMost(MAX_COUNT - 1) + 1
        preferences.edit().putInt(key, next).apply()
        updateWidget(context, AppWidgetManager.getInstance(context), widgetId)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit().apply {
            ids.forEach { remove(countKey(it)) }
        }.apply()
        super.onDeleted(context, ids)
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val count = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
            .getInt(countKey(widgetId), 0)
        val views = RemoteViews(context.packageName, R.layout.widget_dhikr).apply {
            setTextViewText(R.id.dhikr_widget_count, count.toString())
            setOnClickPendingIntent(R.id.dhikr_widget_increment, actionIntent(context, ACTION_INCREMENT, widgetId))
            setOnClickPendingIntent(R.id.dhikr_widget_reset, actionIntent(context, ACTION_RESET, widgetId))
            setOnClickPendingIntent(R.id.dhikr_widget_root, openAppIntent(context))
        }
        manager.updateAppWidget(widgetId, views)
    }

    private fun actionIntent(context: Context, action: String, widgetId: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            action.hashCode() + widgetId,
            Intent(context, DhikrWidgetProvider::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        76,
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("hudaquran:/dhikr")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun countKey(widgetId: Int) = "count_$widgetId"

    private fun isPlacedWidget(context: Context, widgetId: Int): Boolean =
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, DhikrWidgetProvider::class.java))
            .contains(widgetId)

    private companion object {
        const val STORE = "huda_dhikr_widget"
        const val ACTION_INCREMENT = "org.ferdidrgn.hudaquran.widget.DHIKR_INCREMENT"
        const val ACTION_RESET = "org.ferdidrgn.hudaquran.widget.DHIKR_RESET"
        const val INVALID_WIDGET_ID = AppWidgetManager.INVALID_APPWIDGET_ID
        const val MAX_COUNT = 99_999
    }
}
