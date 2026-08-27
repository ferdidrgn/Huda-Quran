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

/**
 * Home-screen widget showing where the reader last left off, reading the very same
 * "huda_quran_prefs" SharedPreferences file that [org.ferdidrgn.hudaquran.data.local.AppPreferences]
 * writes to via multiplatform-settings (SharedPreferencesSettings is a thin wrapper over the same
 * android.content.SharedPreferences APIs used here), so no new storage or cross-process channel
 * is needed to keep the widget in sync.
 */
class LastReadWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildRemoteViews(context))
        }
    }

    companion object {
        private const val PREFS_NAME = "huda_quran_prefs"
        private const val KEY_LAST_READ_SURAH = "last_read_surah"
        private const val KEY_LAST_READ_AYAH = "last_read_ayah"
        private const val KEY_LAST_READ_SURAH_NAME = "last_read_surah_name"

        /** Pushes a fresh RemoteViews to every placed instance of this widget, if any. */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, LastReadWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val views = buildRemoteViews(context)
            for (id in ids) manager.updateAppWidget(id, views)
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val surahNumber = prefs.getInt(KEY_LAST_READ_SURAH, 0)
            val surahName = prefs.getString(KEY_LAST_READ_SURAH_NAME, null).orEmpty()
            val ayahNumber = prefs.getInt(KEY_LAST_READ_AYAH, 1)
            val hasLastRead = surahNumber > 0 && surahName.isNotBlank()

            val views = RemoteViews(context.packageName, R.layout.widget_last_read)
            views.setTextViewText(
                R.id.widget_title,
                if (hasLastRead) context.getString(R.string.widget_continue_reading_title) else context.getString(R.string.widget_title_placeholder),
            )
            views.setTextViewText(
                R.id.widget_subtitle,
                if (hasLastRead) "$surahName · $ayahNumber. ayet" else context.getString(R.string.widget_subtitle_placeholder),
            )

            val deepLinkUri = if (hasLastRead) {
                Uri.parse("hudaquran:/surah/$surahNumber/$ayahNumber")
            } else {
                Uri.parse("hudaquran:/")
            }
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = deepLinkUri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            return views
        }
    }
}
