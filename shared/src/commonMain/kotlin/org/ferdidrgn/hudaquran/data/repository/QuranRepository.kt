package org.ferdidrgn.hudaquran.data.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.ferdidrgn.hudaquran.data.local.QuranCache
import org.ferdidrgn.hudaquran.data.remote.QuranApi
import org.ferdidrgn.hudaquran.data.remote.dto.SurahDto
import org.ferdidrgn.hudaquran.data.remote.retryOnce
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranEditions
import org.ferdidrgn.hudaquran.domain.model.QuranMeta
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.SearchMatch
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.domain.model.Tafsir
import org.ferdidrgn.hudaquran.domain.model.Translation

@Serializable
data class DailyAyah(
    val surahName: String,
    val surahNumber: Int,
    val numberInSurah: Int,
    val arabicText: String,
    val translationText: String,
    /** Local calendar day (yyyy-MM-dd) this ayah belongs to, and the meal it was fetched in. */
    val date: String = "",
    val translationEdition: String = "",
)

class QuranRepository(
    private val api: QuranApi = QuranApi(),
    private val cache: QuranCache = QuranCache(),
) {

    private var cachedSurahList: List<Surah>? = null

    suspend fun getSurahList(): List<Surah> {
        cachedSurahList?.let { return it }
        val fetched = runCatching { retryOnce { api.getSurahList() }.map { it.toDomain() } }
        fetched.getOrNull()?.let { list ->
            cachedSurahList = list
            cache.saveSurahList(list)
            return list
        }
        val offline = cache.getSurahList()
        if (offline != null) {
            cachedSurahList = offline
            return offline
        }
        throw fetched.exceptionOrNull() ?: IllegalStateException("Surah list unavailable")
    }

    suspend fun getSurahDetail(
        surahNumber: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): SurahDetail {
        val fetched = runCatching {
            val editions = retryOnce {
                api.getSurahWithEditions(
                    surahNumber,
                    listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, reciterEdition),
                )
            }
            val arabicEdition = editions[0]
            val translationEditionData = editions[1]
            val audioEdition = editions[2]

            val ayahs = arabicEdition.ayahs.mapIndexed { index, ayahDto ->
                Ayah(
                    surahNumber = arabicEdition.number,
                    surahName = arabicEdition.englishName,
                    numberInSurah = ayahDto.numberInSurah,
                    globalNumber = ayahDto.number,
                    arabicText = ayahDto.text,
                    translationText = translationEditionData.ayahs.getOrNull(index)?.text.orEmpty(),
                    audioUrl = audioEdition.ayahs.getOrNull(index)?.audio.orEmpty(),
                    juz = ayahDto.juz,
                    page = ayahDto.page,
                    isSajda = ayahDto.sajda,
                )
            }

            SurahDetail(
                surah = Surah(
                    number = arabicEdition.number,
                    name = arabicEdition.name,
                    englishName = arabicEdition.englishName,
                    englishNameTranslation = arabicEdition.englishNameTranslation,
                    numberOfAyahs = arabicEdition.numberOfAyahs,
                    revelationType = arabicEdition.revelationType,
                ),
                ayahs = ayahs,
                surahAudioUrl = QuranEditions.surahAudioUrl(surahNumber, reciterEdition),
            )
        }
        fetched.getOrNull()?.let { detail ->
            cache.saveSurahDetail(surahNumber, translationEdition, reciterEdition, detail)
            return detail
        }
        val offline = cache.getSurahDetail(surahNumber, translationEdition, reciterEdition)
        if (offline != null) return offline
        throw fetched.exceptionOrNull() ?: IllegalStateException("Surah detail unavailable")
    }

    suspend fun getSectionDetail(
        kind: SectionKind,
        number: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): QuranSectionDetail {
        val fetched = runCatching {
            val editions = retryOnce {
                api.getSectionWithEditions(
                    kind.apiPath,
                    number,
                    listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, reciterEdition),
                )
            }
            val arabicEdition = editions[0]
            val translationEditionData = editions[1]
            val audioEdition = editions[2]

            val ayahs = arabicEdition.ayahs.mapIndexed { index, ayahDto ->
                Ayah(
                    surahNumber = ayahDto.surah.number,
                    surahName = ayahDto.surah.englishName,
                    numberInSurah = ayahDto.numberInSurah,
                    globalNumber = ayahDto.number,
                    arabicText = ayahDto.text,
                    translationText = translationEditionData.ayahs.getOrNull(index)?.text.orEmpty(),
                    audioUrl = audioEdition.ayahs.getOrNull(index)?.audio.orEmpty(),
                    juz = ayahDto.juz,
                    page = ayahDto.page,
                    isSajda = ayahDto.sajda,
                )
            }

            QuranSectionDetail(sectionNumber = number, ayahs = ayahs)
        }
        fetched.getOrNull()?.let { detail ->
            cache.saveSectionDetail(kind.name, number, translationEdition, reciterEdition, detail)
            return detail
        }
        val offline = cache.getSectionDetail(kind.name, number, translationEdition, reciterEdition)
        if (offline != null) return offline
        throw fetched.exceptionOrNull() ?: IllegalStateException("Section detail unavailable")
    }

    suspend fun getSajdaAyahs(
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): List<Ayah> {
        val arabic = retryOnce { api.getSajdaAyahs(QuranEditions.ARABIC_TEXT_EDITION) }
        val translation = retryOnce { api.getSajdaAyahs(translationEdition) }
        val audio = retryOnce { api.getSajdaAyahs(reciterEdition) }

        return arabic.ayahs.mapIndexed { index, ayahDto ->
            Ayah(
                surahNumber = ayahDto.surah.number,
                surahName = ayahDto.surah.englishName,
                numberInSurah = ayahDto.numberInSurah,
                globalNumber = ayahDto.number,
                arabicText = ayahDto.text,
                translationText = translation.ayahs.getOrNull(index)?.text.orEmpty(),
                audioUrl = audio.ayahs.getOrNull(index)?.audio.orEmpty(),
                juz = ayahDto.juz,
                page = ayahDto.page,
                isSajda = true,
            )
        }
    }

    suspend fun getMeta(): QuranMeta {
        val meta = retryOnce { api.getMeta() }
        return QuranMeta(
            ayahCount = meta.ayahs.count,
            surahCount = meta.surahs.count,
            sajdaCount = meta.sajdas.count,
            rukuCount = meta.rukus.count,
            pageCount = meta.pages.count,
            manzilCount = meta.manzils.count,
            hizbQuarterCount = meta.hizbQuarters.count,
            juzCount = meta.juzs.count,
        )
    }

    private var cachedReciters: List<Reciter>? = null
    private var cachedTranslations: List<Translation>? = null

    suspend fun getReciters(): List<Reciter> {
        cachedReciters?.let { return it }
        val loaded = runCatching {
            api.getEditions(format = "audio", type = "versebyverse")
                .map { Reciter(it.identifier, it.englishName.ifBlank { it.name }) }
                .distinctBy { it.identifier }
                .sortedBy { it.displayName }
        }.getOrElse { QuranEditions.reciters }
        cachedReciters = loaded
        return loaded
    }

    suspend fun getTranslations(): List<Translation> {
        cachedTranslations?.let { return it }
        val loaded = runCatching {
            api.getEditions(format = "text", type = "translation")
                .map { Translation(it.identifier, it.language, it.englishName.ifBlank { it.name }) }
                .distinctBy { it.identifier }
                .sortedBy { it.language }
        }.getOrElse { QuranEditions.translations }
        cachedTranslations = loaded
        return loaded
    }

    private var cachedTafsirs: List<Tafsir>? = null

    suspend fun getTafsirs(): List<Tafsir> {
        cachedTafsirs?.let { return it }
        val loaded = runCatching {
            api.getEditions(format = "text", type = "tafsir")
                .map { Tafsir(it.identifier, it.language, it.englishName.ifBlank { it.name }) }
                .distinctBy { it.identifier }
                .sortedBy { it.language }
        }.getOrElse { emptyList() }
        cachedTafsirs = loaded
        return loaded
    }

    suspend fun getTafsirForAyah(globalAyahNumber: Int, tafsirEdition: String): String {
        val result = retryOnce { api.getAyahWithEditions(globalAyahNumber, listOf(tafsirEdition)) }
        return result.firstOrNull()?.text.orEmpty()
    }

    suspend fun searchQuran(keyword: String, edition: String = QuranEditions.DEFAULT_TRANSLATION): List<SearchMatch> {
        val result = retryOnce { api.search(keyword, edition) }
        return result.matches.map {
            SearchMatch(
                surahNumber = it.surah.number,
                surahName = it.surah.englishName,
                numberInSurah = it.numberInSurah,
                text = it.text,
            )
        }
    }

    /**
     * The "ayah of the day": one ayah per local calendar day, the same on web and mobile for
     * everyone that day, fetched once and then served from cache until midnight — opening Home
     * again no longer re-rolls a random ayah (and hits the API) every time.
     */
    suspend fun getDailyAyah(translationEdition: String = QuranEditions.DEFAULT_TRANSLATION): DailyAyah {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dateKey = today.toString()
        cache.getDailyAyah()
            ?.takeIf { it.date == dateKey && it.translationEdition == translationEdition }
            ?.let { return it }

        val globalAyahNumber = dailyAyahNumberFor(today.toEpochDays().toLong())
        val results = retryOnce {
            api.getAyahWithEditions(globalAyahNumber, listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition))
        }
        val arabic = results[0]
        val translation = results.getOrNull(1)
        val daily = DailyAyah(
            surahName = arabic.surah.englishName,
            surahNumber = arabic.surah.number,
            numberInSurah = arabic.numberInSurah,
            arabicText = arabic.text,
            translationText = translation?.text.orEmpty(),
            date = dateKey,
            translationEdition = translationEdition,
        )
        cache.saveDailyAyah(daily)
        return daily
    }

    private fun SurahDto.toDomain() = Surah(
        number = number,
        name = name,
        englishName = englishName,
        englishNameTranslation = englishNameTranslation,
        numberOfAyahs = numberOfAyahs,
        revelationType = revelationType,
    )
}

private const val TOTAL_AYAHS = 6236

/**
 * Day → global ayah number (1..6236). Stepping by 1683 — coprime with 6236 — walks every ayah once
 * before repeating, and consecutive days land far apart in the mushaf rather than on neighbours.
 */
internal fun dailyAyahNumberFor(epochDay: Long): Int {
    val index = ((epochDay * 1683L) % TOTAL_AYAHS + TOTAL_AYAHS) % TOTAL_AYAHS
    return index.toInt() + 1
}
