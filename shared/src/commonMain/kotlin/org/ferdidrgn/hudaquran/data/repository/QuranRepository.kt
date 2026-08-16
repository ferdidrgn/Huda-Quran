package org.ferdidrgn.hudaquran.data.repository

import org.ferdidrgn.hudaquran.data.remote.QuranApi
import org.ferdidrgn.hudaquran.data.remote.dto.SurahDto
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranEditions
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
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
        val list = api.getSurahList().map { it.toDomain() }
        cachedSurahList = list
        return list
    }

    suspend fun getSurahDetail(
        surahNumber: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): SurahDetail {
        val editions = api.getSurahWithEditions(
            surahNumber,
            listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, reciterEdition),
        )
        val arabicEdition = editions[0]
        val translationEditionData = editions[1]
        val audioEdition = editions[2]

        val ayahs = arabicEdition.ayahs.mapIndexed { index, ayahDto ->
            Ayah(
                surahNumber = arabicEdition.number,
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
