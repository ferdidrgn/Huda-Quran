package org.ferdidrgn.hudaquran.domain.model

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
)

data class Ayah(
    val surahNumber: Int,
    val surahName: String,
    val numberInSurah: Int,
    val globalNumber: Int,
    val arabicText: String,
    val translationText: String,
    val audioUrl: String,
    val juz: Int,
    val page: Int,
    val isSajda: Boolean,
)

data class SurahDetail(
    val surah: Surah,
    val ayahs: List<Ayah>,
    val surahAudioUrl: String,
)

data class JuzDetail(
    val juzNumber: Int,
    val ayahs: List<Ayah>,
)

data class SearchMatch(
    val surahNumber: Int,
    val surahName: String,
    val numberInSurah: Int,
    val text: String,
)

data class Reciter(val identifier: String, val displayName: String)

data class Translation(val identifier: String, val language: String, val displayName: String)

object QuranEditions {
    val reciters = listOf(
        Reciter("ar.alafasy", "Mishary Alafasy"),
        Reciter("ar.abdulbasitmurattal", "Abdul Basit (Murattal)"),
        Reciter("ar.abdurrahmaansudais", "Abdurrahmaan As-Sudais"),
        Reciter("ar.husary", "Mahmoud Al-Husary"),
        Reciter("ar.minshawi", "Mohamed Minshawi"),
    )

    val translations = listOf(
        Translation("tr.diyanet", "tr", "Türkçe (Diyanet İşleri)"),
        Translation("en.sahih", "en", "English (Sahih International)"),
        Translation("en.asad", "en", "English (Muhammad Asad)"),
        Translation("ur.jalandhry", "ur", "اردو (Jalandhry)"),
        Translation("fr.hamidullah", "fr", "Français (Hamidullah)"),
    )

    const val ARABIC_TEXT_EDITION = "quran-uthmani"
    const val DEFAULT_RECITER = "ar.alafasy"
    const val DEFAULT_TRANSLATION = "tr.diyanet"

    fun surahAudioUrl(surahNumber: Int, reciter: String = DEFAULT_RECITER): String =
        "https://cdn.islamic.network/quran/audio-surah/128/$reciter/$surahNumber.mp3"
}
