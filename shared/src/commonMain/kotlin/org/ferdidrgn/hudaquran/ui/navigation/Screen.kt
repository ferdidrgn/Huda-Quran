package org.ferdidrgn.hudaquran.ui.navigation

import org.ferdidrgn.hudaquran.domain.model.SectionKind

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
    data object LanguagePicker : Screen()
    data object TajwidLessonList : Screen()
    data class TajwidLessonDetail(val lessonId: String) : Screen()
    data object Search : Screen()
    data class SectionList(val kind: SectionKind) : Screen()
    data class SectionDetail(val kind: SectionKind, val number: Int) : Screen()
    data object SajdaAyahs : Screen()
    data object NowPlaying : Screen()
    data class MushafPage(val pageNumber: Int) : Screen()
}
