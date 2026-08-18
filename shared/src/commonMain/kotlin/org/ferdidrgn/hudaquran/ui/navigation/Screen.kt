package org.ferdidrgn.hudaquran.ui.navigation

sealed class Screen {
    data object Splash : Screen()
    data object Onboarding : Screen()
    data object Home : Screen()
    data object SurahList : Screen()
    data class SurahDetail(val surahNumber: Int, val scrollToAyah: Int? = null) : Screen()
    data object Favorites : Screen()
    data object Settings : Screen()
    data object ReciterPicker : Screen()
    data object TranslationPicker : Screen()
    data object PrayerLocationPicker : Screen()
    data object ArabicAlphabet : Screen()
    data object Search : Screen()
    data object JuzList : Screen()
    data class JuzDetail(val juzNumber: Int) : Screen()
    data object NowPlaying : Screen()
}
