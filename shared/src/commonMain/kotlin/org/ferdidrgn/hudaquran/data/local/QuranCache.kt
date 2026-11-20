package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ferdidrgn.hudaquran.data.repository.DailyAyah
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.SurahDetail

/**
 * Persists already-fetched Quran content as JSON blobs in [Settings], so the reader can keep
 * reading a surah/page they already opened once even with no network on a reinstall-free device.
 * Deliberately reuses the same storage the app already relies on (multiplatform-settings) instead
 * of introducing a new database dependency/driver per platform.
 */
class QuranCache(private val settings: Settings = createSettings()) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEY_SURAH_LIST = "cache_surah_list"
        private const val KEY_SURAH_DETAIL_PREFIX = "cache_surah_detail_"
        private const val KEY_SECTION_DETAIL_PREFIX = "cache_section_detail_"
        private const val KEY_TAFSIR_PREFIX = "cache_tafsir_"
        private const val KEY_DAILY_AYAH = "cache_daily_ayah"
        // v2 deliberately invalidates the old static-fallback catalogue. A cached catalogue is
        // safe to show offline only when it was observed from a real provider response.
        private const val KEY_TRANSLATIONS = "cache_translation_catalog_v2"
        private const val KEY_TAFSIRS = "cache_tafsir_catalog_v2"
    }

    fun getDailyAyah(): DailyAyah? = readOrNull(KEY_DAILY_AYAH)

    fun saveDailyAyah(daily: DailyAyah) = write(KEY_DAILY_AYAH, daily)

    fun getSurahList(): List<Surah>? = readOrNull(KEY_SURAH_LIST)

    fun saveSurahList(list: List<Surah>) = write(KEY_SURAH_LIST, list)

    fun getTranslations(): List<org.ferdidrgn.hudaquran.domain.model.Translation>? = readOrNull(KEY_TRANSLATIONS)

    fun saveTranslations(list: List<org.ferdidrgn.hudaquran.domain.model.Translation>) = write(KEY_TRANSLATIONS, list)

    fun getTafsirs(): List<org.ferdidrgn.hudaquran.domain.model.Tafsir>? = readOrNull(KEY_TAFSIRS)

    fun saveTafsirs(list: List<org.ferdidrgn.hudaquran.domain.model.Tafsir>) = write(KEY_TAFSIRS, list)

    fun getSurahDetail(surahNumber: Int, translationEdition: String, reciterEdition: String): SurahDetail? =
        readOrNull("$KEY_SURAH_DETAIL_PREFIX${surahNumber}_${translationEdition}_$reciterEdition")

    fun saveSurahDetail(surahNumber: Int, translationEdition: String, reciterEdition: String, detail: SurahDetail) =
        write("$KEY_SURAH_DETAIL_PREFIX${surahNumber}_${translationEdition}_$reciterEdition", detail)

    fun getSectionDetail(kindName: String, number: Int, translationEdition: String, reciterEdition: String): QuranSectionDetail? =
        readOrNull("$KEY_SECTION_DETAIL_PREFIX${kindName}_${number}_${translationEdition}_$reciterEdition")

    fun saveSectionDetail(kindName: String, number: Int, translationEdition: String, reciterEdition: String, detail: QuranSectionDetail) =
        write("$KEY_SECTION_DETAIL_PREFIX${kindName}_${number}_${translationEdition}_$reciterEdition", detail)

    /** Keeps the last-read explanation available when the user opens an ayah without internet. */
    fun getTafsir(globalAyahNumber: Int, edition: String): String? =
        settings.getStringOrNull("$KEY_TAFSIR_PREFIX${globalAyahNumber}_$edition")

    fun saveTafsir(globalAyahNumber: Int, edition: String, text: String) {
        if (text.isNotBlank()) settings.putString("$KEY_TAFSIR_PREFIX${globalAyahNumber}_$edition", text)
    }

    /** Removes the cached Quran payload for one exact offline selection. */
    fun clearOfflineSelection(selection: OfflineDownloadSelection) {
        // The cache intentionally owns only known keys. There is no broad clear operation here,
        // so deleting a package never touches favorites, settings, or another translation.
        for (surah in 1..114) {
            settings.remove("$KEY_SURAH_DETAIL_PREFIX${surah}_${selection.translationEdition}_${selection.reciterEdition}")
        }
        for (page in 1..604) {
            settings.remove("$KEY_SECTION_DETAIL_PREFIX${SectionKind.PAGE.name}_${page}_${selection.translationEdition}_${selection.reciterEdition}")
        }
        // Tafsir keys are global-ayah based; only the selected edition is removed.
        for (ayah in 1..6236) {
            settings.remove("$KEY_TAFSIR_PREFIX${ayah}_${selection.tafsirEdition}")
        }
    }

    private inline fun <reified T> readOrNull(key: String): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        return runCatching { json.decodeFromString<T>(raw) }.getOrNull()
    }

    private inline fun <reified T> write(key: String, value: T) {
        runCatching { settings.putString(key, json.encodeToString(value)) }
    }
}
