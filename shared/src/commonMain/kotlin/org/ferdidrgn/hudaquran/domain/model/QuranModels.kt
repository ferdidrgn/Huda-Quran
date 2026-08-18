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

data class QuranSectionDetail(
    val sectionNumber: Int,
    val ayahs: List<Ayah>,
)

/** Every alternate way the Quran is divided for browsing/recitation, beyond Surah. */
enum class SectionKind(val apiPath: String, val titleSingular: String, val titlePlural: String) {
    JUZ("juz", "Cüz", "Cüzler"),
    PAGE("page", "Sayfa", "Sayfalar"),
    MANZIL("manzil", "Menzil", "Menziller"),
    RUKU("ruku", "Rükû", "Rükûlar"),
    HIZB_QUARTER("hizbQuarter", "Hizb Çeyreği", "Hizb Çeyrekleri"),
}

data class QuranMeta(
    val ayahCount: Int,
    val surahCount: Int,
    val sajdaCount: Int,
    val rukuCount: Int,
    val pageCount: Int,
    val manzilCount: Int,
    val hizbQuarterCount: Int,
    val juzCount: Int,
) {
    fun countFor(kind: SectionKind): Int = when (kind) {
        SectionKind.JUZ -> juzCount
        SectionKind.PAGE -> pageCount
        SectionKind.MANZIL -> manzilCount
        SectionKind.RUKU -> rukuCount
        SectionKind.HIZB_QUARTER -> hizbQuarterCount
    }
}

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
