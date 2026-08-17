package org.ferdidrgn.hudaquran.domain.model

import org.ferdidrgn.hudaquran.data.local.AppLanguage

/** Standard Diyanet-style Turkish surah names, index 0 = Surah 1 (Fatiha). */
private val turkishSurahNames = listOf(
    "Fâtiha", "Bakara", "Âl-i İmrân", "Nisâ", "Mâide", "En'âm", "A'râf", "Enfâl",
    "Tevbe", "Yûnus", "Hûd", "Yûsuf", "Ra'd", "İbrâhim", "Hicr", "Nahl",
    "İsrâ", "Kehf", "Meryem", "Tâhâ", "Enbiyâ", "Hac", "Mü'minûn", "Nûr",
    "Furkân", "Şuarâ", "Neml", "Kasas", "Ankebût", "Rûm", "Lokman", "Secde",
    "Ahzâb", "Sebe'", "Fâtır", "Yâsîn", "Sâffât", "Sâd", "Zümer", "Mü'min",
    "Fussilet", "Şûrâ", "Zuhruf", "Duhân", "Câsiye", "Ahkâf", "Muhammed", "Fetih",
    "Hucurât", "Kâf", "Zâriyât", "Tûr", "Necm", "Kamer", "Rahmân", "Vâkıa",
    "Hadîd", "Mücâdele", "Haşr", "Mümtehine", "Saf", "Cum'a", "Münâfikûn", "Tegâbün",
    "Talâk", "Tahrîm", "Mülk", "Kalem", "Hâkka", "Meâric", "Nûh", "Cin",
    "Müzzemmil", "Müddessir", "Kıyâme", "İnsân", "Mürselât", "Nebe'", "Nâziât", "Abese",
    "Tekvîr", "İnfitâr", "Mutaffifîn", "İnşikâk", "Bürûc", "Târık", "A'lâ", "Gâşiye",
    "Fecr", "Beled", "Şems", "Leyl", "Duhâ", "İnşirâh", "Tîn", "Alak",
    "Kadir", "Beyyine", "Zilzâl", "Âdiyât", "Kâria", "Tekâsür", "Asr", "Hümeze",
    "Fil", "Kureyş", "Mâûn", "Kevser", "Kâfirûn", "Nasr", "Tebbet", "İhlâs",
    "Felak", "Nâs",
)

/**
 * Turkish gets the standard Diyanet spelling instead of the API's raw English transliteration.
 * Other languages fall back to the API-provided name to avoid guessing at unverified spellings.
 */
fun localizedSurahName(surahNumber: Int, fallbackName: String, language: AppLanguage): String =
    when (language) {
        AppLanguage.TURKISH -> turkishSurahNames.getOrNull(surahNumber - 1) ?: fallbackName
        else -> fallbackName
    }
