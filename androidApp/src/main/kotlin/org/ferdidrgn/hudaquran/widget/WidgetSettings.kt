package org.ferdidrgn.hudaquran.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.ColorInt
import org.ferdidrgn.hudaquran.R

/** Per-instance options. An empty locationId follows the location selected in app settings. */
data class WidgetSettings(
    val paletteId: String = WidgetPalette.SAKURA,
    val fontScale: Float = 1f,
    val density: String = DENSITY_COMFORTABLE,
    val locationId: String = "",
    val dailyKind: String = DAILY_AYAH,
    val showArabic: Boolean = true,
    val showTranslation: Boolean = true,
    val dhikrText: String = "Subhanallah",
    val dhikrGoal: Int = 33,
) {
    companion object {
        const val DENSITY_COMPACT = "compact"
        const val DENSITY_COMFORTABLE = "comfortable"
        const val DENSITY_ROOMY = "roomy"
        const val DAILY_AYAH = "ayah"
        const val DAILY_DUA = "dua"
        const val DAILY_HADITH = "hadith"
    }
}

/** Store settings by Android's appWidgetId so two copies can have different content and colors. */
object WidgetSettingsStore {
    private const val STORE = "huda_widget_settings"
    private fun key(id: Int, name: String) = "widget_${id}_$name"

    fun read(context: Context, widgetId: Int): WidgetSettings {
        val preferences = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
        return WidgetSettings(
            paletteId = WidgetPalette.resolveId(preferences.getString(key(widgetId, "palette"), null)),
            fontScale = (preferences.getFloat(key(widgetId, "font_scale"), 1f)).coerceIn(0.85f, 1.35f),
            density = preferences.getString(key(widgetId, "density"), null).orEmpty().takeIf {
                it in setOf(
                    WidgetSettings.DENSITY_COMPACT,
                    WidgetSettings.DENSITY_COMFORTABLE,
                    WidgetSettings.DENSITY_ROOMY,
                )
            } ?: WidgetSettings.DENSITY_COMFORTABLE,
            locationId = preferences.getString(key(widgetId, "location"), "").orEmpty().take(160),
            dailyKind = preferences.getString(key(widgetId, "daily_kind"), null).orEmpty().takeIf {
                it in setOf(
                    WidgetSettings.DAILY_AYAH,
                    WidgetSettings.DAILY_DUA,
                    WidgetSettings.DAILY_HADITH,
                )
            } ?: WidgetSettings.DAILY_AYAH,
            showArabic = preferences.getBoolean(key(widgetId, "show_arabic"), true),
            showTranslation = preferences.getBoolean(key(widgetId, "show_translation"), true),
            dhikrText = preferences.getString(key(widgetId, "dhikr_text"), "Subhanallah")
                .orEmpty().trim().take(80).ifBlank { "Subhanallah" },
            dhikrGoal = preferences.getInt(key(widgetId, "dhikr_goal"), 33).coerceIn(1, 999),
        )
    }

    fun save(context: Context, widgetId: Int, settings: WidgetSettings) {
        if (widgetId < 0) return
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit()
            .putString(key(widgetId, "palette"), WidgetPalette.resolveId(settings.paletteId))
            .putFloat(key(widgetId, "font_scale"), settings.fontScale.coerceIn(0.85f, 1.35f))
            .putString(key(widgetId, "density"), settings.density.takeIf {
                it in setOf(
                    WidgetSettings.DENSITY_COMPACT,
                    WidgetSettings.DENSITY_COMFORTABLE,
                    WidgetSettings.DENSITY_ROOMY,
                )
            } ?: WidgetSettings.DENSITY_COMFORTABLE)
            .putString(key(widgetId, "location"), settings.locationId.trim().take(160))
            .putString(key(widgetId, "daily_kind"), settings.dailyKind.takeIf {
                it in setOf(
                    WidgetSettings.DAILY_AYAH,
                    WidgetSettings.DAILY_DUA,
                    WidgetSettings.DAILY_HADITH,
                )
            } ?: WidgetSettings.DAILY_AYAH)
            .putBoolean(key(widgetId, "show_arabic"), settings.showArabic)
            .putBoolean(key(widgetId, "show_translation"), settings.showTranslation)
            .putString(key(widgetId, "dhikr_text"), settings.dhikrText.trim().take(80).ifBlank { "Subhanallah" })
            .putInt(key(widgetId, "dhikr_goal"), settings.dhikrGoal.coerceIn(1, 999))
            .apply()
    }

    fun delete(context: Context, widgetId: Int) {
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit().apply {
            listOf(
                "palette", "font_scale", "density", "location", "daily_kind",
                "show_arabic", "show_translation", "dhikr_text", "dhikr_goal",
            ).forEach { remove(key(widgetId, it)) }
        }.apply()
    }
}

data class WidgetPalette(
    val id: String,
    val label: String,
    val labelResource: Int,
    @ColorInt val backgroundColor: Int,
    @ColorInt val accentColor: Int,
    @ColorInt val titleColor: Int,
    @ColorInt val bodyColor: Int,
    @ColorInt val metaColor: Int,
    @DrawableRes val backgroundResource: Int,
    @DrawableRes val accentSurfaceResource: Int,
) {
    companion object {
        const val SAKURA = "sakura"
        const val EMERALD = "emerald"
        const val PARCHMENT = "parchment"
        const val AZURE = "azure"
        const val ROSE = "rose"

        val all: List<WidgetPalette> = listOf(
            WidgetPalette(
                SAKURA, "Sakura", R.string.widget_palette_sakura, 0xFF1B0C1A.toInt(), 0xFFFADCD5.toInt(), 0xFFFFF8F5.toInt(),
                0xFFF0E4E8.toInt(), 0xFFD8C3CC.toInt(), R.drawable.widget_background_sakura,
                R.drawable.widget_accent_sakura,
            ),
            WidgetPalette(
                EMERALD, "Emerald", R.string.widget_palette_emerald, 0xFF10241D.toInt(), 0xFFF2D17B.toInt(), 0xFFF8F8ED.toInt(),
                0xFFE5EDDF.toInt(), 0xFFC1D0C2.toInt(), R.drawable.widget_background_emerald,
                R.drawable.widget_accent_emerald,
            ),
            WidgetPalette(
                PARCHMENT, "Mushaf paper", R.string.widget_palette_parchment, 0xFFF2E9D7.toInt(), 0xFF765626.toInt(), 0xFF2D261D.toInt(),
                0xFF393329.toInt(), 0xFF5C554B.toInt(), R.drawable.widget_background_parchment,
                R.drawable.widget_accent_parchment,
            ),
            WidgetPalette(
                AZURE, "Azure", R.string.widget_palette_azure, 0xFF10273A.toInt(), 0xFF9DD7F3.toInt(), 0xFFF1F7FC.toInt(),
                0xFFE3EDF4.toInt(), 0xFFC1D2DE.toInt(), R.drawable.widget_background_azure,
                R.drawable.widget_accent_azure,
            ),
            WidgetPalette(
                ROSE, "Rose", R.string.widget_palette_rose, 0xFF331827.toInt(), 0xFFFFCEDA.toInt(), 0xFFFFF6F8.toInt(),
                0xFFF3E3E9.toInt(), 0xFFE5C8D2.toInt(), R.drawable.widget_background_rose,
                R.drawable.widget_accent_rose,
            ),
        )

        fun resolve(paletteId: String?): WidgetPalette = all.firstOrNull { it.id == paletteId } ?: all.first()
        fun resolveId(paletteId: String?): String = resolve(paletteId).id
    }
}

data class WidgetTextTarget(val viewId: Int, val baseSp: Float)

/** Common per-instance rendering helpers for classic RemoteViews-backed app widgets. */
object WidgetAppearance {
    fun apply(
        context: Context,
        views: RemoteViews,
        widgetId: Int,
        rootId: Int,
        accentIds: IntArray = intArrayOf(),
        titleIds: IntArray = intArrayOf(),
        bodyIds: IntArray = intArrayOf(),
        metaIds: IntArray = intArrayOf(),
        textTargets: List<WidgetTextTarget> = emptyList(),
    ) {
        val settings = WidgetSettingsStore.read(context, widgetId)
        val palette = WidgetPalette.resolve(settings.paletteId)
        views.setInt(rootId, "setBackgroundResource", palette.backgroundResource)
        accentIds.forEach { views.setTextColor(it, palette.accentColor) }
        titleIds.forEach { views.setTextColor(it, palette.titleColor) }
        bodyIds.forEach { views.setTextColor(it, palette.bodyColor) }
        metaIds.forEach { views.setTextColor(it, palette.metaColor) }
        val densityScale = when (settings.density) {
            WidgetSettings.DENSITY_COMPACT -> 0.92f
            WidgetSettings.DENSITY_ROOMY -> 1.08f
            else -> 1f
        }
        textTargets.forEach { target ->
            views.setFloat(target.viewId, "setTextSize", target.baseSp * settings.fontScale * densityScale)
        }
    }

    fun applyAccentSurface(context: Context, views: RemoteViews, widgetId: Int, vararg viewIds: Int) {
        val palette = WidgetPalette.resolve(WidgetSettingsStore.read(context, widgetId).paletteId)
        viewIds.forEach { views.setInt(it, "setBackgroundResource", palette.accentSurfaceResource) }
    }
}
