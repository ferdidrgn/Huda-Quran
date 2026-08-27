package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.MapSettings
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QuranCacheTest {

    private fun newCache() = QuranCache(settings = MapSettings())

    private val sampleAyah = Ayah(
        surahNumber = 1,
        surahName = "Al-Fatiha",
        numberInSurah = 1,
        globalNumber = 1,
        arabicText = "بِسْمِ اللَّهِ",
        translationText = "In the name of Allah",
        audioUrl = "https://example.com/1.mp3",
        juz = 1,
        page = 1,
        isSajda = false,
    )

    private val sampleSurah = Surah(
        number = 1,
        name = "الفاتحة",
        englishName = "Al-Fatiha",
        englishNameTranslation = "The Opening",
        numberOfAyahs = 7,
        revelationType = "Meccan",
    )

    @Test
    fun surahListMissThenHitAfterSave() {
        val cache = newCache()
        assertNull(cache.getSurahList())

        cache.saveSurahList(listOf(sampleSurah))
        assertEquals(listOf(sampleSurah), cache.getSurahList())
    }

    @Test
    fun surahDetailIsKeyedByNumberAndEditions() {
        val cache = newCache()
        val detail = SurahDetail(surah = sampleSurah, ayahs = listOf(sampleAyah), surahAudioUrl = "https://example.com/full.mp3")

        cache.saveSurahDetail(1, "tr.diyanet", "ar.alafasy", detail)

        assertEquals(detail, cache.getSurahDetail(1, "tr.diyanet", "ar.alafasy"))
        // A different translation/reciter combination must not collide with the cached entry.
        assertNull(cache.getSurahDetail(1, "en.sahih", "ar.alafasy"))
        assertNull(cache.getSurahDetail(2, "tr.diyanet", "ar.alafasy"))
    }

    @Test
    fun sectionDetailIsKeyedByKindNumberAndEditions() {
        val cache = newCache()
        val detail = QuranSectionDetail(sectionNumber = 3, ayahs = listOf(sampleAyah))

        cache.saveSectionDetail("JUZ", 3, "tr.diyanet", "ar.alafasy", detail)

        assertEquals(detail, cache.getSectionDetail("JUZ", 3, "tr.diyanet", "ar.alafasy"))
        assertNull(cache.getSectionDetail("PAGE", 3, "tr.diyanet", "ar.alafasy"))
    }
}
