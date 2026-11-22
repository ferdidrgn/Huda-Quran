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

/** Per-instance offline dhikr counter with a customizable phrase and goal. */
class DhikrWidgetProvider : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        updateWidget(context, manager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = ids.forEach { updateWidget(context, manager, it) }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_INCREMENT && intent.action != ACTION_RESET) return
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID_WIDGET_ID)
        if (id == INVALID_WIDGET_ID || !isPlacedWidget(context, id)) return
        val prefs = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
        val key = countKey(id)
        val next = if (intent.action == ACTION_RESET) 0 else (prefs.getInt(key, 0) + 1).coerceAtMost(MAX_COUNT)
        prefs.edit().putInt(key, next).apply()
        updateWidget(context, AppWidgetManager.getInstance(context), id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit().apply { ids.forEach { remove(countKey(it)) } }.apply()
        ids.forEach { WidgetSettingsStore.delete(context, it) }
        super.onDeleted(context, ids)
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
        val settings = WidgetSettingsStore.read(context, id)
        val count = context.getSharedPreferences(STORE, Context.MODE_PRIVATE).getInt(countKey(id), 0)
        val views = RemoteViews(context.packageName, R.layout.widget_dhikr).apply {
            setTextViewText(R.id.dhikr_widget_label, settings.dhikrText)
            setTextViewText(R.id.dhikr_widget_count, count.toString())
            setTextViewText(R.id.dhikr_widget_goal, context.getString(R.string.widget_dhikr_progress, count, settings.dhikrGoal))
            setProgressBar(R.id.dhikr_widget_progress, settings.dhikrGoal, count.coerceAtMost(settings.dhikrGoal), false)
            setCharSequence(R.id.dhikr_widget_increment, "setContentDescription",
                context.getString(R.string.widget_dhikr_increment_accessibility_label, settings.dhikrText, count, settings.dhikrGoal))
            setCharSequence(R.id.dhikr_widget_reset, "setContentDescription",
                context.getString(R.string.widget_dhikr_reset_accessibility_label, settings.dhikrText))
            setOnClickPendingIntent(R.id.dhikr_widget_increment, actionIntent(context, ACTION_INCREMENT, id))
            setOnClickPendingIntent(R.id.dhikr_widget_reset, actionIntent(context, ACTION_RESET, id))
            setOnClickPendingIntent(R.id.dhikr_widget_root, openAppIntent(context, id))
        }
        WidgetAppearance.apply(context, views, id, R.id.dhikr_widget_root,
            accentIds = intArrayOf(R.id.dhikr_widget_count), titleIds = intArrayOf(R.id.dhikr_widget_label),
            bodyIds = intArrayOf(R.id.dhikr_widget_goal), metaIds = intArrayOf(R.id.dhikr_widget_reset),
            textTargets = listOf(WidgetTextTarget(R.id.dhikr_widget_label, 16f),
                WidgetTextTarget(R.id.dhikr_widget_count, 28f), WidgetTextTarget(R.id.dhikr_widget_goal, 12f)))
        WidgetAppearance.applyAccentSurface(context, views, id, R.id.dhikr_widget_increment)
        manager.updateAppWidget(id, views)
    }

    private fun actionIntent(context: Context, action: String, id: Int) = PendingIntent.getBroadcast(
        context, action.hashCode() + id, Intent(context, DhikrWidgetProvider::class.java).apply {
            this.action = action; putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun openAppIntent(context: Context, id: Int) = PendingIntent.getActivity(context, OPEN_BASE + id,
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW; data = Uri.parse("hudaquran:/dhikr")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun countKey(id: Int) = "count_$id"
    private fun isPlacedWidget(context: Context, id: Int) = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, DhikrWidgetProvider::class.java)).contains(id)

    private companion object {
        const val STORE = "huda_dhikr_widget"
        const val ACTION_INCREMENT = "org.ferdidrgn.hudaquran.widget.DHIKR_INCREMENT"
        const val ACTION_RESET = "org.ferdidrgn.hudaquran.widget.DHIKR_RESET"
        const val INVALID_WIDGET_ID = AppWidgetManager.INVALID_APPWIDGET_ID
        const val MAX_COUNT = 99_999
        const val OPEN_BASE = 7_600
    }
}
