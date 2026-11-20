package org.ferdidrgn.hudaquran.domain.model

import kotlinx.serialization.Serializable

// The standard 15-line Uthmani mushaf layout — the same one AlQuran Cloud's /page endpoints
// paginate against — always has exactly 604 pages. Shared so khatm-progress tracking (data layer)
// and its UI (Home, Mushaf mode) agree on the same total without duplicating the literal.
const val TOTAL_MUSHAF_PAGES = 604

@Serializable
data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
)

@Serializable
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

@Serializable
data class SurahDetail(
    val surah: Surah,
    val ayahs: List<Ayah>,
    val surahAudioUrl: String,
)

@Serializable
data class QuranSectionDetail(
    val sectionNumber: Int,
    val ayahs: List<Ayah>,
)

/** Every alternate way the Quran is divided for browsing/recitation, beyond Surah. */
enum class SectionKind(val apiPath: String) {
    JUZ("juz"),
    PAGE("page"),
    MANZIL("manzil"),
    RUKU("ruku"),
    HIZB_QUARTER("hizbQuarter"),
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

@Serializable
data class Translation(
    val identifier: String,
    val language: String,
    val displayName: String,
    val source: String = "AlQuran Cloud",
    val sourceUrl: String? = "https://alquran.cloud/api",
    val author: String? = null,
    /** Only populated when the catalogue explicitly supplies a licence/rights notice. */
    val licenseNotice: String? = null,
)

@Serializable
data class Tafsir(
    val identifier: String,
    val language: String,
    val displayName: String,
    val author: String? = null,
    val source: String? = null,
    val sourceUrl: String? = null,
    /** Null means the provider did not publish licence metadata in the catalogue response. */
    val licenseNotice: String? = null,
)

object QuranEditions {
    val reciters = listOf(
        Reciter("ar.abdullahbasfar", "Abdullah Basfar"),
        Reciter("ar.alafasy", "Mishary Alafasy"),
        Reciter("ar.abdulsamad", "Abdul Samad"),
        Reciter("ar.abdurrahmaansudais", "Abdurrahmaan As-Sudais"),
        Reciter("ar.shaatree", "Abu Bakr Ash-Shaatree"),
        Reciter("ar.ahmedajamy", "Ahmed ibn Ali al-Ajamy"),
        Reciter("ar.hanirifai", "Hani Rifai"),
        Reciter("ar.husary", "Mahmoud Al-Husary"),
        Reciter("ar.husarymujawwad", "Mahmoud Al-Husary (Mujawwad)"),
        Reciter("ar.mahermuaiqly", "Maher Al Muaiqly"),
        Reciter("ar.muhammadayyoub", "Muhammad Ayyoub"),
        Reciter("ar.hudhaify", "Ali Al-Hudhaify"),
        Reciter("ar.muhammadjibreel", "Muhammad Jibreel"),
        Reciter("ar.saoodshuraym", "Saood bin Ibraaheem Ash-Shuraym"),
        Reciter("ar.ibrahimakhbar", "Ibrahim Akhdar"),
        Reciter("ar.aymanswoaid", "Ayman Sowaid"),
        Reciter("ar.parhizgar", "Parhizgar"),
    )

    const val ARABIC_TEXT_EDITION = "quran-uthmani"
    const val DEFAULT_RECITER = "ar.abdullahbasfar"
    const val DEFAULT_TRANSLATION = "tr.diyanet"

    fun surahAudioUrl(surahNumber: Int, reciter: String = DEFAULT_RECITER): String =
        "https://cdn.islamic.network/quran/audio-surah/128/$reciter/$surahNumber.mp3"
}
