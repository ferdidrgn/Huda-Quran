package org.ferdidrgn.hudaquran.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.MainActivity
import org.ferdidrgn.hudaquran.R
import java.util.Calendar

/** Attributed daily Ayah, Dua, or Hadith cards. Each widget instance has its own kind and style. */
class DailyContentWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        val today = todayKey()
        widgetIds.forEach { id ->
            val settings = WidgetSettingsStore.read(context, id)
            val kind = DailyContentKind.parse(settings.dailyKind)
            val cached = DailyWidgetContent.forDay(context, today, kind)
            manager.updateAppWidget(id, render(context, manager, id, cached, loading = cached == null))
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                widgetIds.forEach { id ->
                    val kind = DailyContentKind.parse(WidgetSettingsStore.read(context, id).dailyKind)
                    val cached = DailyWidgetContent.forDay(context, today, kind)
                        ?: DailyWidgetContent.loadForDay(context, today, kind)?.also {
                            DailyWidgetContent.cacheForDay(context, today, it)
                        }
                    manager.updateAppWidget(id, render(context, manager, id, cached))
                }
            } finally {
                pending.finish()
            }
        }
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
        val kind = DailyContentKind.parse(WidgetSettingsStore.read(context, appWidgetId).dailyKind)
        manager.updateAppWidget(
            appWidgetId,
            render(context, manager, appWidgetId, DailyWidgetContent.forDay(context, todayKey(), kind), loading = false),
        )
    }

    companion object {
        private fun todayKey(): Int = Calendar.getInstance().let { it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR) }

        private fun render(
            context: Context,
            manager: AppWidgetManager,
            id: Int,
            item: DailyWidgetItem?,
            loading: Boolean = false,
        ): RemoteViews {
            val settings = WidgetSettingsStore.read(context, id)
            val kind = DailyContentKind.parse(settings.dailyKind)
            val views = RemoteViews(context.packageName, R.layout.widget_daily_content)
            val compact = manager.getAppWidgetOptions(id).let { options ->
                (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180) < 180) ||
                    (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 100) < 100)
            }

            views.setTextViewText(R.id.daily_widget_title, context.getString(kind.titleResource))
            WidgetAppearance.applyAccentSurface(context, views, id, R.id.daily_widget_title)
            val arabic = item?.arabic.orEmpty()
            val showArabic = settings.showArabic && arabic.isNotBlank() && (!compact || !settings.showTranslation)
            views.setViewVisibility(R.id.daily_widget_arabic, if (showArabic) android.view.View.VISIBLE else android.view.View.GONE)
            views.setTextViewText(R.id.daily_widget_arabic, arabic)

            val text = when {
                item == null -> context.getString(if (loading) R.string.widget_daily_loading else R.string.widget_daily_unavailable)
                !settings.showTranslation && !showArabic -> context.getString(R.string.widget_daily_selected_text_unavailable)
                !settings.showTranslation -> item.reference.ifBlank { context.getString(R.string.widget_daily_resize_hint) }
                else -> item.text
            }
            views.setTextViewText(R.id.daily_widget_text, text)
            views.setViewVisibility(
                R.id.daily_widget_text,
                if (settings.showTranslation || item == null || !showArabic) android.view.View.VISIBLE else android.view.View.GONE,
            )
            views.setTextViewText(R.id.daily_widget_reference, item?.reference.orEmpty())
            views.setViewVisibility(R.id.daily_widget_reference, if (item?.reference.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE)
            val attribution = item?.let {
                buildList {
                    add(it.source)
                    if (it.author.isNotBlank()) add(it.author)
                    if (it.licenseNotice.isNotBlank()) add(it.licenseNotice)
                }.joinToString(" · ")
            }.orEmpty()
            views.setTextViewText(R.id.daily_widget_attribution, attribution)
            if (attribution.isNotBlank()) {
                views.setContentDescription(
                    R.id.daily_widget_attribution,
                    context.getString(R.string.widget_daily_source_accessibility, attribution),
                )
            }
            views.setViewVisibility(R.id.daily_widget_attribution, if (attribution.isBlank()) android.view.View.GONE else android.view.View.VISIBLE)

            WidgetAppearance.apply(
                context = context,
                views = views,
                widgetId = id,
                rootId = R.id.daily_widget_root,
                titleIds = intArrayOf(R.id.daily_widget_title),
                bodyIds = intArrayOf(R.id.daily_widget_text, R.id.daily_widget_arabic),
                metaIds = intArrayOf(R.id.daily_widget_reference, R.id.daily_widget_attribution),
                accentIds = intArrayOf(),
                textTargets = listOf(
                    WidgetTextTarget(R.id.daily_widget_title, 14f),
                    WidgetTextTarget(R.id.daily_widget_arabic, 20f),
                    WidgetTextTarget(R.id.daily_widget_text, 14f),
                    WidgetTextTarget(R.id.daily_widget_reference, 12f),
                    WidgetTextTarget(R.id.daily_widget_attribution, 10f),
                ),
            )
            views.setTextColor(R.id.daily_widget_title, WidgetPalette.resolve(settings.paletteId).accentColor)
            item?.sourceUrl?.takeIf { it.startsWith("https://") }?.let { sourceUrl ->
                val sourcePending = PendingIntent.getActivity(
                    context,
                    40_000 + id * 3 + kind.ordinal,
                    Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                views.setOnClickPendingIntent(R.id.daily_widget_attribution, sourcePending)
            }

            val path = when (kind) {
                DailyContentKind.AYAH -> item?.reference?.let { reference ->
                    val match = Regex("(\\d+)\\s*:\\s*(\\d+)").find(reference)
                    match?.let { "hudaquran:/surah/${it.groupValues[1]}/${it.groupValues[2]}" }
                } ?: "hudaquran:/surah"
                DailyContentKind.DUA -> "hudaquran:/dua"
                DailyContentKind.HADITH -> "hudaquran:/hadith"
            }
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(path)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context,
                20_000 + id * 3 + kind.ordinal,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.daily_widget_root, pending)
            return views
        }
    }
}
