package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
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
    }

    fun getSurahList(): List<Surah>? = readOrNull(KEY_SURAH_LIST)

    fun saveSurahList(list: List<Surah>) = write(KEY_SURAH_LIST, list)

    fun getSurahDetail(surahNumber: Int, translationEdition: String, reciterEdition: String): SurahDetail? =
        readOrNull("$KEY_SURAH_DETAIL_PREFIX${surahNumber}_${translationEdition}_$reciterEdition")

    fun saveSurahDetail(surahNumber: Int, translationEdition: String, reciterEdition: String, detail: SurahDetail) =
        write("$KEY_SURAH_DETAIL_PREFIX${surahNumber}_${translationEdition}_$reciterEdition", detail)

    fun getSectionDetail(kindName: String, number: Int, translationEdition: String, reciterEdition: String): QuranSectionDetail? =
        readOrNull("$KEY_SECTION_DETAIL_PREFIX${kindName}_${number}_${translationEdition}_$reciterEdition")

    fun saveSectionDetail(kindName: String, number: Int, translationEdition: String, reciterEdition: String, detail: QuranSectionDetail) =
        write("$KEY_SECTION_DETAIL_PREFIX${kindName}_${number}_${translationEdition}_$reciterEdition", detail)

    private inline fun <reified T> readOrNull(key: String): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        return runCatching { json.decodeFromString<T>(raw) }.getOrNull()
    }

    private inline fun <reified T> write(key: String, value: T) {
        runCatching { settings.putString(key, json.encodeToString(value)) }
    }
}
