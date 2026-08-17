package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private const val KEY_THEME = "theme_mode"
        private const val KEY_PRAYER_CITY = "prayer_city"
        private const val KEY_PRAYER_COUNTRY = "prayer_country"
        private const val KEY_PRAYER_NOTIFICATIONS = "prayer_notifications_enabled"
        private const val KEY_APP_LANGUAGE = "app_language"
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
}
