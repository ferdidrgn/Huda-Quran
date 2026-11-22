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

/** A focused reading companion for a Quran-dedicated launcher page. */
class QuranFocusWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        widgetIds.forEach { manager.updateAppWidget(it, build(context, it)) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetSettingsStore.delete(context, it) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        manager.updateAppWidget(appWidgetId, build(context, appWidgetId))
    }

    companion object {
        /** Refresh after reading-state changes; call alongside LastReadWidgetProvider.refreshAll. */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(android.content.ComponentName(context, QuranFocusWidgetProvider::class.java))
                .forEach { manager.updateAppWidget(it, QuranFocusWidgetProvider().build(context, it)) }
        }
    }

    private fun build(context: Context, widgetId: Int): RemoteViews {
        val preferences = context.getSharedPreferences(Keys.PREFS_NAME, Context.MODE_PRIVATE)
        val surah = preferences.getInt(Keys.KEY_SURAH, 0).coerceIn(0, 114)
        val ayah = preferences.getInt(Keys.KEY_AYAH, 1).coerceAtLeast(1)
        val surahName = preferences.getString(Keys.KEY_SURAH_NAME, null).orEmpty().take(48)
        val page = preferences.getInt(Keys.KEY_FURTHEST_PAGE, 0).coerceIn(0, Keys.TOTAL_PAGES)
        val hasLastRead = surah > 0 && surahName.isNotBlank()

        val views = RemoteViews(context.packageName, R.layout.widget_quran_focus)
        views.setTextViewText(R.id.quran_focus_title, context.getString(R.string.widget_quran_focus_title))
        WidgetAppearance.applyAccentSurface(context, views, widgetId, R.id.quran_focus_title)
        views.setTextViewText(
            R.id.quran_focus_reading,
            if (hasLastRead) context.getString(R.string.widget_quran_focus_last_read, surahName, ayah)
            else context.getString(R.string.widget_quran_focus_not_started),
        )
        views.setTextViewText(
            R.id.quran_focus_progress_label,
            context.getString(R.string.widget_quran_focus_progress, page, Keys.TOTAL_PAGES),
        )
        views.setProgressBar(R.id.quran_focus_progress, Keys.TOTAL_PAGES, page, false)

        val target = if (hasLastRead) "hudaquran:/surah/$surah/$ayah" else "hudaquran:/surah"
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(target)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            30_000 + widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.quran_focus_root, pending)
        WidgetAppearance.apply(
            context = context,
            views = views,
            widgetId = widgetId,
            rootId = R.id.quran_focus_root,
            accentIds = intArrayOf(),
            titleIds = intArrayOf(R.id.quran_focus_title),
            bodyIds = intArrayOf(R.id.quran_focus_reading),
            metaIds = intArrayOf(R.id.quran_focus_progress_label),
            textTargets = listOf(
                WidgetTextTarget(R.id.quran_focus_title, 16f),
                WidgetTextTarget(R.id.quran_focus_reading, 15f),
                WidgetTextTarget(R.id.quran_focus_progress_label, 12f),
            ),
        )
        views.setTextColor(
            R.id.quran_focus_title,
            WidgetPalette.resolve(WidgetSettingsStore.read(context, widgetId).paletteId).accentColor,
        )
        return views
    }

    private object Keys {
        const val PREFS_NAME = "huda_quran_prefs"
        const val KEY_SURAH = "last_read_surah"
        const val KEY_AYAH = "last_read_ayah"
        const val KEY_SURAH_NAME = "last_read_surah_name"
        const val KEY_FURTHEST_PAGE = "khatm_furthest_page"
        const val TOTAL_PAGES = 604
    }
}
