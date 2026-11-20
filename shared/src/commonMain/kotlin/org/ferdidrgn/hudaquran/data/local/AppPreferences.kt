package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.domain.model.QuranEditions

enum class ThemeMode { SYSTEM, LIGHT, DARK, SAKURA }

/** A global text-size multiplier for readers who need larger type — elderly users especially. */
enum class TextSizeOption(val scale: Float) {
    NORMAL(1f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f),
}

enum class PrayerNotificationSound { SILENT, DEFAULT, ADHAN_1, ADHAN_2, ADHAN_3, BEEP, DOUBLE_BEEP }

data class LastRead(val surahNumber: Int, val numberInSurah: Int, val surahName: String)

class AppPreferences(private val settings: Settings = createSettings()) {
    companion object {
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_THEME_INTRO_SEEN = "theme_intro_seen"
        private const val KEY_FAVORITES = "favorite_ayahs"
        private const val KEY_HADITH_FAVORITES = "favorite_hadith_ids"
        private const val KEY_DUA_FAVORITES = "favorite_dua_ids"
        private const val KEY_ESMA_FAVORITES = "favorite_esma_ids"
        private const val KEY_LAST_READ_SURAH = "last_read_surah"
        private const val KEY_LAST_READ_AYAH = "last_read_ayah"
        private const val KEY_LAST_READ_SURAH_NAME = "last_read_surah_name"
        private const val KEY_RECITER = "selected_reciter"
        private const val KEY_RECITER_BASFAR_MIGRATED = "reciter_basfar_migrated"
        private const val KEY_TRANSLATION = "selected_translation"
        private const val KEY_TAFSIR = "selected_tafsir"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_PRAYER_CITY = "prayer_city"
        private const val KEY_PRAYER_COUNTRY = "prayer_country"
        private const val KEY_PRAYER_NOTIFICATIONS = "prayer_notifications_enabled"
        private const val KEY_PRAYER_LEAD_MINUTES = "prayer_reminder_lead_minutes"
        private const val KEY_PRAYER_AT_TIME = "prayer_reminder_at_time"
        private const val KEY_PRAYER_SOUND = "prayer_notification_sound"
        private const val KEY_PRAYER_VIBRATION = "prayer_notification_vibration"
        private const val KEY_PRAYER_BOOT_RESCHEDULE = "prayer_boot_reschedule"
        private const val KEY_PRAYER_ENABLED_PREFIX = "prayer_enabled_"
        private const val KEY_OCCASION_REMINDERS = "occasion_reminders_enabled"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_ADS_REMOVED_UNTIL = "ads_removed_until_millis"
        private const val KEY_LAST_MUSHAF_PAGE = "last_mushaf_page"
        private const val KEY_KHATM_FURTHEST_PAGE = "khatm_furthest_page"
        private const val KEY_KHATM_COMPLETED_COUNT = "khatm_completed_count"
        private const val KEY_STREAK_CURRENT = "reading_streak_current"
        private const val KEY_STREAK_LAST_DAY = "reading_streak_last_day"
        private const val KEY_MEMORIZATION_DAY = "memorization_day"
        private const val KEY_MEMORIZATION_REPEATS = "memorization_repeats"
        private const val KEY_MEMORIZATION_COMPLETED_DAY = "memorization_completed_day"
        private const val KEY_TEXT_SIZE = "text_size_option"
        private const val KEY_TEXT_SIZE_BEFORE_ELDERLY = "text_size_before_elderly_profile"
        private const val KEY_ELDERLY_PROFILE = "elderly_profile_enabled"
        private const val KEY_HIGH_CONTRAST = "high_contrast_enabled"
        private const val KEY_LARGE_TOUCH_TARGETS = "large_touch_targets_enabled"
        private const val KEY_REDUCED_MOTION = "reduced_motion_enabled"
        private const val KEY_MUSHAF_SHOW_TRANSLATION = "mushaf_show_translation"
        private const val KEY_SURAH_SHOW_TRANSLATION = "surah_show_translation"
        private const val KEY_MUSHAF_GESTURE_HINT_SEEN = "mushaf_gesture_hint_seen"
    }

    var adsRemovedUntilMillis: Long
        get() = settings.getLong(KEY_ADS_REMOVED_UNTIL, 0L)
        private set(value) {
            settings.putLong(KEY_ADS_REMOVED_UNTIL, value)
            _adFree.value = value > Clock.System.now().toEpochMilliseconds()
        }

    private val _adFree = MutableStateFlow(adsRemovedUntilMillis > Clock.System.now().toEpochMilliseconds())

    /** Observable ad-free state, so Settings and ad slots update right after a purchase or restore. */
    val adFree: StateFlow<Boolean> = _adFree.asStateFlow()

    fun isAdFree(): Boolean = adsRemovedUntilMillis > Clock.System.now().toEpochMilliseconds()

    /** Extends the ad-free period by [durationMillis] from now, or from the current expiry if it's still active. */
    fun grantAdFreePeriod(durationMillis: Long) {
        val now = Clock.System.now().toEpochMilliseconds()
        val base = maxOf(adsRemovedUntilMillis, now)
        adsRemovedUntilMillis = base + durationMillis
    }

    /** Sets the ad-free expiry reported by the store (0 = not ad-free). */
    fun setAdFreeUntil(epochMillis: Long) {
        adsRemovedUntilMillis = epochMillis
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

    /** Minutes before each prayer for the gentle pre-reminder; 0 = off. */
    private val _prayerReminderLeadMinutes = MutableStateFlow(settings.getInt(KEY_PRAYER_LEAD_MINUTES, 10))
    val prayerReminderLeadMinutes: StateFlow<Int> = _prayerReminderLeadMinutes.asStateFlow()

    fun setPrayerReminderLeadMinutes(minutes: Int) {
        settings.putInt(KEY_PRAYER_LEAD_MINUTES, minutes)
        _prayerReminderLeadMinutes.value = minutes
    }

    /** Also notify at the moment the prayer time begins. */
    private val _prayerAtTimeEnabled = MutableStateFlow(settings.getBoolean(KEY_PRAYER_AT_TIME, true))
    val prayerAtTimeEnabled: StateFlow<Boolean> = _prayerAtTimeEnabled.asStateFlow()

    fun setPrayerAtTimeEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_PRAYER_AT_TIME, enabled)
        _prayerAtTimeEnabled.value = enabled
    }

    private val _prayerNotificationSound = MutableStateFlow(loadPrayerNotificationSound())
    val prayerNotificationSound: StateFlow<PrayerNotificationSound> = _prayerNotificationSound.asStateFlow()

    private fun loadPrayerNotificationSound(): PrayerNotificationSound = when (settings.getString(KEY_PRAYER_SOUND, PrayerNotificationSound.DEFAULT.name)) {
        "ADHAN" -> PrayerNotificationSound.ADHAN_1
        else -> runCatching { PrayerNotificationSound.valueOf(settings.getString(KEY_PRAYER_SOUND, PrayerNotificationSound.DEFAULT.name)) }
            .getOrDefault(PrayerNotificationSound.DEFAULT)
    }

    fun setPrayerNotificationSound(value: PrayerNotificationSound) {
        settings.putString(KEY_PRAYER_SOUND, value.name)
        _prayerNotificationSound.value = value
    }

    private val _prayerNotificationVibration = MutableStateFlow(settings.getBoolean(KEY_PRAYER_VIBRATION, true))
    val prayerNotificationVibration: StateFlow<Boolean> = _prayerNotificationVibration.asStateFlow()

    fun setPrayerNotificationVibration(enabled: Boolean) {
        settings.putBoolean(KEY_PRAYER_VIBRATION, enabled)
        _prayerNotificationVibration.value = enabled
    }

    private val _prayerBootReschedule = MutableStateFlow(settings.getBoolean(KEY_PRAYER_BOOT_RESCHEDULE, true))
    val prayerBootReschedule: StateFlow<Boolean> = _prayerBootReschedule.asStateFlow()

    fun setPrayerBootReschedule(enabled: Boolean) {
        settings.putBoolean(KEY_PRAYER_BOOT_RESCHEDULE, enabled)
        _prayerBootReschedule.value = enabled
    }

    fun isPrayerEnabled(key: String): Boolean = settings.getBoolean(KEY_PRAYER_ENABLED_PREFIX + key, true)

    fun setPrayerEnabled(key: String, enabled: Boolean) {
        settings.putBoolean(KEY_PRAYER_ENABLED_PREFIX + key, enabled)
    }

    /** Reminders two days and one day before kandils and other blessed days. */
    private val _occasionRemindersEnabled = MutableStateFlow(settings.getBoolean(KEY_OCCASION_REMINDERS, true))
    val occasionRemindersEnabled: StateFlow<Boolean> = _occasionRemindersEnabled.asStateFlow()

    fun setOccasionRemindersEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_OCCASION_REMINDERS, enabled)
        _occasionRemindersEnabled.value = enabled
    }

    var onboardingCompleted: Boolean
        get() = settings.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = settings.putBoolean(KEY_ONBOARDING_DONE, value)

    /** The one-time "New: Themes" card on Home has been seen (or themes were chosen in onboarding). */
    var themeIntroSeen: Boolean
        get() = settings.getBoolean(KEY_THEME_INTRO_SEEN, false)
        set(value) = settings.putBoolean(KEY_THEME_INTRO_SEEN, value)

    /** Abdullah Basfar became the default in 1.5.1; everyone starts from him once, then may change. */
    var selectedReciter: String
        get() {
            if (!settings.getBoolean(KEY_RECITER_BASFAR_MIGRATED, false)) {
                settings.putBoolean(KEY_RECITER_BASFAR_MIGRATED, true)
                settings.putString(KEY_RECITER, QuranEditions.DEFAULT_RECITER)
            }
            return settings.getString(KEY_RECITER, QuranEditions.DEFAULT_RECITER)
        }
        set(value) = settings.putString(KEY_RECITER, value)

    var selectedTranslation: String
        get() = settings.getString(KEY_TRANSLATION, QuranEditions.DEFAULT_TRANSLATION)
        set(value) = settings.putString(KEY_TRANSLATION, value)

    /** Empty until the reader picks one (or one is auto-selected on first use of the tafsir screen). */
    var selectedTafsir: String
        get() = settings.getString(KEY_TAFSIR, "")
        set(value) = settings.putString(KEY_TAFSIR, value)

    private fun loadThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(settings.getString(KEY_THEME, ThemeMode.SAKURA.name)) }
            .getOrDefault(ThemeMode.SAKURA)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(value: ThemeMode) {
        settings.putString(KEY_THEME, value.name)
        _themeMode.value = value
    }

    private fun loadTextSize(): TextSizeOption =
        runCatching { TextSizeOption.valueOf(settings.getString(KEY_TEXT_SIZE, TextSizeOption.NORMAL.name)) }
            .getOrDefault(TextSizeOption.NORMAL)

    private val _textSize = MutableStateFlow(loadTextSize())
    val textSize: StateFlow<TextSizeOption> = _textSize.asStateFlow()

    fun setTextSize(value: TextSizeOption) {
        settings.putString(KEY_TEXT_SIZE, value.name)
        _textSize.value = value
    }

    /**
     * A single, reversible reading profile for users who benefit from stronger visual and
     * interaction affordances. The existing A-/A+ setting is preserved and restored when the
     * profile is turned off, so enabling this profile never loses a reader's previous choice.
     */
    private val _elderlyProfile = MutableStateFlow(settings.getBoolean(KEY_ELDERLY_PROFILE, false))
    val elderlyProfile: StateFlow<Boolean> = _elderlyProfile.asStateFlow()

    private val _highContrast = MutableStateFlow(settings.getBoolean(KEY_HIGH_CONTRAST, false))
    val highContrast: StateFlow<Boolean> = _highContrast.asStateFlow()

    private val _largeTouchTargets = MutableStateFlow(settings.getBoolean(KEY_LARGE_TOUCH_TARGETS, false))
    val largeTouchTargets: StateFlow<Boolean> = _largeTouchTargets.asStateFlow()

    private val _reducedMotion = MutableStateFlow(settings.getBoolean(KEY_REDUCED_MOTION, false))
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    fun setElderlyProfile(enabled: Boolean) {
        if (enabled == _elderlyProfile.value) return
        if (enabled) {
            settings.putString(KEY_TEXT_SIZE_BEFORE_ELDERLY, _textSize.value.name)
            setTextSize(TextSizeOption.EXTRA_LARGE)
            setHighContrast(true)
            setLargeTouchTargets(true)
            setReducedMotion(true)
        } else {
            val previous = runCatching {
                TextSizeOption.valueOf(settings.getString(KEY_TEXT_SIZE_BEFORE_ELDERLY, TextSizeOption.NORMAL.name))
            }.getOrDefault(TextSizeOption.NORMAL)
            setTextSize(previous)
            setHighContrast(false)
            setLargeTouchTargets(false)
            setReducedMotion(false)
        }
        settings.putBoolean(KEY_ELDERLY_PROFILE, enabled)
        _elderlyProfile.value = enabled
    }

    fun setHighContrast(enabled: Boolean) {
        settings.putBoolean(KEY_HIGH_CONTRAST, enabled)
        _highContrast.value = enabled
    }

    fun setLargeTouchTargets(enabled: Boolean) {
        settings.putBoolean(KEY_LARGE_TOUCH_TARGETS, enabled)
        _largeTouchTargets.value = enabled
    }

    fun setReducedMotion(enabled: Boolean) {
        settings.putBoolean(KEY_REDUCED_MOTION, enabled)
        _reducedMotion.value = enabled
    }

    /** Meal under each Mushaf page; on by default so first-time readers see the meaning. */
    private val _mushafShowTranslation = MutableStateFlow(settings.getBoolean(KEY_MUSHAF_SHOW_TRANSLATION, true))
    val mushafShowTranslation: StateFlow<Boolean> = _mushafShowTranslation.asStateFlow()

    fun setMushafShowTranslation(show: Boolean) {
        settings.putBoolean(KEY_MUSHAF_SHOW_TRANSLATION, show)
        _mushafShowTranslation.value = show
    }

    /** Meal under each ayah in the surah reader; on by default. */
    private val _surahShowTranslation = MutableStateFlow(settings.getBoolean(KEY_SURAH_SHOW_TRANSLATION, true))
    val surahShowTranslation: StateFlow<Boolean> = _surahShowTranslation.asStateFlow()

    fun setSurahShowTranslation(show: Boolean) {
        settings.putBoolean(KEY_SURAH_SHOW_TRANSLATION, show)
        _surahShowTranslation.value = show
    }

    /** The one-time "tap an ayah to listen, hold for actions" hint in Mushaf mode has been shown. */
    var mushafGestureHintSeen: Boolean
        get() = settings.getBoolean(KEY_MUSHAF_GESTURE_HINT_SEEN, false)
        set(value) = settings.putBoolean(KEY_MUSHAF_GESTURE_HINT_SEEN, value)

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    /** Hadis favorites intentionally use their own namespace; they must never mix with surah:ayah keys. */
    private val _hadithFavorites = MutableStateFlow(loadHadithFavorites())
    val hadithFavorites: StateFlow<Set<String>> = _hadithFavorites.asStateFlow()

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

    private fun loadHadithFavorites(): Set<String> =
        settings.getString(KEY_HADITH_FAVORITES, "")
            .split(",")
            .map(String::trim)
            .filter { it.isNotBlank() }
            .toSet()

    fun isHadithFavorite(id: String): Boolean = id in _hadithFavorites.value

    fun toggleHadithFavorite(id: String) {
        val normalized = id.trim()
        if (normalized.isBlank()) return
        val updated = _hadithFavorites.value.toMutableSet()
        if (!updated.add(normalized)) updated.remove(normalized)
        _hadithFavorites.value = updated
        settings.putString(KEY_HADITH_FAVORITES, updated.joinToString(","))
    }

    private fun loadDuaFavorites(): Set<String> = settings.getString(KEY_DUA_FAVORITES, "")
        .split(",").map(String::trim).filter(String::isNotBlank).toSet()

    private val _duaFavorites = MutableStateFlow(loadDuaFavorites())
    val duaFavorites: StateFlow<Set<String>> = _duaFavorites.asStateFlow()

    fun toggleDuaFavorite(id: String) {
        if (id.isBlank()) return
        val updated = _duaFavorites.value.toMutableSet()
        if (!updated.add(id)) updated.remove(id)
        _duaFavorites.value = updated
        settings.putString(KEY_DUA_FAVORITES, updated.joinToString(","))
    }

    private fun loadEsmaFavorites(): Set<Int> = settings.getString(KEY_ESMA_FAVORITES, "")
        .split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()

    private val _esmaFavorites = MutableStateFlow(loadEsmaFavorites())
    val esmaFavorites: StateFlow<Set<Int>> = _esmaFavorites.asStateFlow()

    fun toggleEsmaFavorite(index: Int) {
        val updated = _esmaFavorites.value.toMutableSet()
        if (!updated.add(index)) updated.remove(index)
        _esmaFavorites.value = updated
        settings.putString(KEY_ESMA_FAVORITES, updated.sorted().joinToString(","))
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
        recordReadingActivity()
    }

    private val _lastMushafPage = MutableStateFlow(settings.getIntOrNull(KEY_LAST_MUSHAF_PAGE))
    val lastMushafPage: StateFlow<Int?> = _lastMushafPage.asStateFlow()

    fun saveLastMushafPage(page: Int) {
        settings.putInt(KEY_LAST_MUSHAF_PAGE, page)
        _lastMushafPage.value = page
        recordReadingActivity()
    }

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

    private val _readingStreak = MutableStateFlow(settings.getInt(KEY_STREAK_CURRENT, 0))
    val readingStreak: StateFlow<Int> = _readingStreak.asStateFlow()

    private val _memorizationRepeatCount = MutableStateFlow(settings.getInt(KEY_MEMORIZATION_REPEATS, 0))
    val memorizationRepeatCount: StateFlow<Int> = _memorizationRepeatCount.asStateFlow()

    private val _memorizationCompleted = MutableStateFlow(false)
    val memorizationCompleted: StateFlow<Boolean> = _memorizationCompleted.asStateFlow()

    /** Keeps the small daily repetition flow in the same durable preference store as reading progress. */
    fun refreshMemorizationDay() {
        val today = Clock.System.now().toEpochMilliseconds() / 86_400_000L
        val savedDay = settings.getLong(KEY_MEMORIZATION_DAY, Long.MIN_VALUE)
        if (savedDay != today) {
            settings.putLong(KEY_MEMORIZATION_DAY, today)
            settings.putInt(KEY_MEMORIZATION_REPEATS, 0)
            settings.putLong(KEY_MEMORIZATION_COMPLETED_DAY, Long.MIN_VALUE)
            _memorizationRepeatCount.value = 0
            _memorizationCompleted.value = false
        } else {
            _memorizationRepeatCount.value = settings.getInt(KEY_MEMORIZATION_REPEATS, 0)
            _memorizationCompleted.value = settings.getLong(KEY_MEMORIZATION_COMPLETED_DAY, Long.MIN_VALUE) == today
        }
    }

    fun addMemorizationRepeat() {
        refreshMemorizationDay()
        if (_memorizationCompleted.value) return
        val next = (_memorizationRepeatCount.value + 1).coerceAtMost(5)
        settings.putInt(KEY_MEMORIZATION_REPEATS, next)
        _memorizationRepeatCount.value = next
    }

    fun completeMemorizationForToday() {
        refreshMemorizationDay()
        val today = Clock.System.now().toEpochMilliseconds() / 86_400_000L
        settings.putLong(KEY_MEMORIZATION_COMPLETED_DAY, today)
        _memorizationCompleted.value = true
    }

    /**
     * Called whenever the user genuinely reads something (an ayah or a Mushaf page), not on every
     * screen open. Consecutive calendar days (UTC, coarse on purpose — this is a motivational
     * streak, not a precise attendance log) extend the streak; a gap of more than one day resets
     * it to 1; a second call on the same day is a no-op so re-reading doesn't inflate it.
     */
    private fun recordReadingActivity() {
        val today = Clock.System.now().toEpochMilliseconds() / 86_400_000L
        val lastDay = settings.getLong(KEY_STREAK_LAST_DAY, -1L)
        val newStreak = when (lastDay) {
            today -> return
            today - 1 -> _readingStreak.value + 1
            else -> 1
        }
        settings.putLong(KEY_STREAK_LAST_DAY, today)
        settings.putInt(KEY_STREAK_CURRENT, newStreak)
        _readingStreak.value = newStreak
    }
}
