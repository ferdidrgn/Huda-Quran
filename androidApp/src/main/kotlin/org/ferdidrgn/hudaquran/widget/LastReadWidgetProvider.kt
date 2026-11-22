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

/** Shows the user's most recently opened Quran passage. */
class LastReadWidgetProvider : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        manager.updateAppWidget(appWidgetId, buildRemoteViews(context, appWidgetId))
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, buildRemoteViews(context, it)) }
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetSettingsStore.delete(context, it) }
        super.onDeleted(context, ids)
    }

    companion object {
        private const val PREFS_NAME = "huda_quran_prefs"
        private const val KEY_LAST_READ_SURAH = "last_read_surah"
        private const val KEY_LAST_READ_AYAH = "last_read_ayah"
        private const val KEY_LAST_READ_SURAH_NAME = "last_read_surah_name"
        private const val OPEN_BASE = 8_000

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, LastReadWidgetProvider::class.java))
            ids.forEach { manager.updateAppWidget(it, buildRemoteViews(context, it)) }
        }

        private fun buildRemoteViews(context: Context, id: Int): RemoteViews {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val surahNumber = prefs.getInt(KEY_LAST_READ_SURAH, 0)
            val surahName = prefs.getString(KEY_LAST_READ_SURAH_NAME, null).orEmpty()
            val ayahNumber = prefs.getInt(KEY_LAST_READ_AYAH, 1)
            val hasLastRead = surahNumber in 1..114 && surahName.isNotBlank() && ayahNumber > 0
            val views = RemoteViews(context.packageName, R.layout.widget_last_read)
            views.setTextViewText(R.id.widget_title, if (hasLastRead)
                context.getString(R.string.widget_continue_reading_title) else context.getString(R.string.widget_title_placeholder))
            views.setTextViewText(R.id.widget_subtitle, if (hasLastRead)
                "$surahName · $ayahNumber. ayet" else context.getString(R.string.widget_subtitle_placeholder))
            val link = if (hasLastRead) Uri.parse("hudaquran:/surah/$surahNumber/$ayahNumber") else Uri.parse("hudaquran:/")
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW; data = link
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, OPEN_BASE + id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            WidgetAppearance.apply(context, views, id, R.id.widget_root,
                titleIds = intArrayOf(R.id.widget_title), bodyIds = intArrayOf(R.id.widget_subtitle),
                textTargets = listOf(WidgetTextTarget(R.id.widget_title, 18f), WidgetTextTarget(R.id.widget_subtitle, 14f)))
            return views
        }
    }
}
