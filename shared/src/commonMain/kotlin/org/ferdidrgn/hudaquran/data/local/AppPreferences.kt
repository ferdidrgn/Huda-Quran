package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.domain.model.QuranEditions

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class LastRead(val surahNumber: Int, val numberInSurah: Int, val surahName: String)

class AppPreferences(private val settings: Settings = createSettings()) {
    companion object {
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_FAVORITES = "favorite_ayahs"
        private const val KEY_LAST_READ_SURAH = "last_read_surah"
        private const val KEY_LAST_READ_AYAH = "last_read_ayah"
        private const val KEY_LAST_READ_SURAH_NAME = "last_read_surah_name"
        private const val KEY_RECITER = "selected_reciter"
        private const val KEY_TRANSLATION = "selected_translation"
        private const val KEY_TAFSIR = "selected_tafsir"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_PRAYER_CITY = "prayer_city"
        private const val KEY_PRAYER_COUNTRY = "prayer_country"
        private const val KEY_PRAYER_NOTIFICATIONS = "prayer_notifications_enabled"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_ADS_REMOVED_UNTIL = "ads_removed_until_millis"
        private const val KEY_LAST_MUSHAF_PAGE = "last_mushaf_page"
        private const val KEY_MUSHAF_LANDSCAPE_HINT_SEEN = "mushaf_landscape_hint_seen"
        private const val KEY_KHATM_FURTHEST_PAGE = "khatm_furthest_page"
        private const val KEY_KHATM_COMPLETED_COUNT = "khatm_completed_count"
    }

    var adsRemovedUntilMillis: Long
        get() = settings.getLong(KEY_ADS_REMOVED_UNTIL, 0L)
        private set(value) = settings.putLong(KEY_ADS_REMOVED_UNTIL, value)

    fun isAdFree(): Boolean = adsRemovedUntilMillis > Clock.System.now().toEpochMilliseconds()

    /** Extends the ad-free period by [durationMillis] from now, or from the current expiry if it's still active. */
    fun grantAdFreePeriod(durationMillis: Long) {
        val now = Clock.System.now().toEpochMilliseconds()
        val base = maxOf(adsRemovedUntilMillis, now)
        adsRemovedUntilMillis = base + durationMillis
    }

    private fun loadAppLanguage(): AppLanguage =
        runCatching { AppLanguage.valueOf(settings.getString(KEY_APP_LANGUAGE, AppLanguage.TURKISH.name)) }
            .getOrDefault(AppLanguage.TURKISH)

    private val _appLanguage = MutableStateFlow(loadAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setAppLanguage(value: AppLanguage) {
        settings.putString(KEY_APP_LANGUAGE, value.name)
        _appLanguage.value = value
    }

    var prayerCity: String
        get() = settings.getString(KEY_PRAYER_CITY, "Istanbul")
        set(value) = settings.putString(KEY_PRAYER_CITY, value)

    var prayerCountry: String
        get() = settings.getString(KEY_PRAYER_COUNTRY, "Turkey")
        set(value) = settings.putString(KEY_PRAYER_COUNTRY, value)

    private val _prayerNotificationsEnabled = MutableStateFlow(settings.getBoolean(KEY_PRAYER_NOTIFICATIONS, false))
    val prayerNotificationsEnabled: StateFlow<Boolean> = _prayerNotificationsEnabled.asStateFlow()

    fun setPrayerNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_PRAYER_NOTIFICATIONS, enabled)
        _prayerNotificationsEnabled.value = enabled
    }

    var onboardingCompleted: Boolean
        get() = settings.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = settings.putBoolean(KEY_ONBOARDING_DONE, value)

    var selectedReciter: String
        get() = settings.getString(KEY_RECITER, QuranEditions.DEFAULT_RECITER)
        set(value) = settings.putString(KEY_RECITER, value)

    var selectedTranslation: String
        get() = settings.getString(KEY_TRANSLATION, QuranEditions.DEFAULT_TRANSLATION)
        set(value) = settings.putString(KEY_TRANSLATION, value)

    /** Empty until the reader picks one (or one is auto-selected on first use of the tafsir screen). */
    var selectedTafsir: String
        get() = settings.getString(KEY_TAFSIR, "")
        set(value) = settings.putString(KEY_TAFSIR, value)

    private fun loadThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(settings.getString(KEY_THEME, ThemeMode.SYSTEM.name)) }
            .getOrDefault(ThemeMode.SYSTEM)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(value: ThemeMode) {
        settings.putString(KEY_THEME, value.name)
        _themeMode.value = value
    }

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private fun loadFavorites(): Set<String> =
        settings.getString(KEY_FAVORITES, "")
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()

    fun isFavorite(surahNumber: Int, numberInSurah: Int): Boolean =
        "$surahNumber:$numberInSurah" in _favorites.value

    fun toggleFavorite(surahNumber: Int, numberInSurah: Int) {
        val key = "$surahNumber:$numberInSurah"
        val updated = _favorites.value.toMutableSet()
        if (!updated.add(key)) updated.remove(key)
        _favorites.value = updated
        settings.putString(KEY_FAVORITES, updated.joinToString(","))
    }

    private val _lastRead = MutableStateFlow(loadLastRead())
    val lastRead: StateFlow<LastRead?> = _lastRead.asStateFlow()

    private fun loadLastRead(): LastRead? {
        val surah = settings.getIntOrNull(KEY_LAST_READ_SURAH) ?: return null
        val ayah = settings.getInt(KEY_LAST_READ_AYAH, 1)
        val name = settings.getString(KEY_LAST_READ_SURAH_NAME, "")
        return LastRead(surah, ayah, name)
    }

    fun saveLastRead(surahNumber: Int, numberInSurah: Int, surahName: String) {
        settings.putInt(KEY_LAST_READ_SURAH, surahNumber)
        settings.putInt(KEY_LAST_READ_AYAH, numberInSurah)
        settings.putString(KEY_LAST_READ_SURAH_NAME, surahName)
        _lastRead.value = LastRead(surahNumber, numberInSurah, surahName)
    }

    private val _lastMushafPage = MutableStateFlow(settings.getIntOrNull(KEY_LAST_MUSHAF_PAGE))
    val lastMushafPage: StateFlow<Int?> = _lastMushafPage.asStateFlow()

    fun saveLastMushafPage(page: Int) {
        settings.putInt(KEY_LAST_MUSHAF_PAGE, page)
        _lastMushafPage.value = page
    }

    var mushafLandscapeHintSeen: Boolean
        get() = settings.getBoolean(KEY_MUSHAF_LANDSCAPE_HINT_SEEN, false)
        set(value) = settings.putBoolean(KEY_MUSHAF_LANDSCAPE_HINT_SEEN, value)

    private val _khatmFurthestPage = MutableStateFlow(settings.getInt(KEY_KHATM_FURTHEST_PAGE, 0))
    val khatmFurthestPage: StateFlow<Int> = _khatmFurthestPage.asStateFlow()

    private val _khatmCompletedCount = MutableStateFlow(settings.getInt(KEY_KHATM_COMPLETED_COUNT, 0))
    val khatmCompletedCount: StateFlow<Int> = _khatmCompletedCount.asStateFlow()

    /**
     * Advances hatim (khatm) progress as the reader moves through Mushaf page mode. Progress only
     * ever moves forward — jumping back to re-read an earlier page doesn't undo it, matching how a
     * physical mushaf bookmark works. Reaching [totalPages] completes the khatm and starts a new
     * one from page 0, incrementing the completed count.
     */
    fun advanceKhatmProgress(pageNumber: Int, totalPages: Int) {
        if (pageNumber <= _khatmFurthestPage.value) return
        if (pageNumber >= totalPages) {
            settings.putInt(KEY_KHATM_FURTHEST_PAGE, 0)
            _khatmFurthestPage.value = 0
            val completed = _khatmCompletedCount.value + 1
            settings.putInt(KEY_KHATM_COMPLETED_COUNT, completed)
            _khatmCompletedCount.value = completed
        } else {
            settings.putInt(KEY_KHATM_FURTHEST_PAGE, pageNumber)
            _khatmFurthestPage.value = pageNumber
        }
    }
}
