package org.ferdidrgn.hudaquran.data.repository

import org.ferdidrgn.hudaquran.data.remote.QuranApi
import org.ferdidrgn.hudaquran.data.remote.retryOnce
import org.ferdidrgn.hudaquran.data.remote.dto.SurahDto
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.JuzDetail
import org.ferdidrgn.hudaquran.domain.model.QuranEditions
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.SearchMatch
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.domain.model.Translation
import kotlin.random.Random

data class DailyAyah(
    val surahName: String,
    val surahNumber: Int,
    val numberInSurah: Int,
    val arabicText: String,
    val translationText: String,
)

class QuranRepository(private val api: QuranApi = QuranApi()) {

    private var cachedSurahList: List<Surah>? = null

    suspend fun getSurahList(): List<Surah> {
        cachedSurahList?.let { return it }
        val list = retryOnce { api.getSurahList() }.map { it.toDomain() }
        cachedSurahList = list
        return list
    }

    suspend fun getSurahDetail(
        surahNumber: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): SurahDetail {
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

        return SurahDetail(
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

    suspend fun getJuzDetail(
        juzNumber: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): JuzDetail {
        val editions = retryOnce {
            api.getJuzWithEditions(
                juzNumber,
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
                juz = juzNumber,
                page = ayahDto.page,
                isSajda = ayahDto.sajda,
            )
        }

        return JuzDetail(juzNumber = juzNumber, ayahs = ayahs)
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

    suspend fun getDailyAyah(translationEdition: String = QuranEditions.DEFAULT_TRANSLATION): DailyAyah {
        val globalNumber = Random.nextInt(1, 6237)
        val results = api.getAyahWithEditions(globalNumber, listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition))
        val arabic = results[0]
        val translation = results.getOrNull(1)
        return DailyAyah(
            surahName = arabic.surah.englishName,
            surahNumber = arabic.surah.number,
            numberInSurah = arabic.numberInSurah,
            arabicText = arabic.text,
            translationText = translation?.text.orEmpty(),
        )
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
