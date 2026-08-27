package org.ferdidrgn.hudaquran.ui.navigation

import org.ferdidrgn.hudaquran.domain.model.SectionKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkTest {

    @Test
    fun rootPathIsHome() {
        assertEquals(Screen.Home, DeepLink.parse("/"))
    }

    @Test
    fun emptyStringIsUnparseable() {
        assertNull(DeepLink.parse(""))
    }

    @Test
    fun surahWithoutAyah() {
        assertEquals(Screen.SurahDetail(12, null), DeepLink.parse("/surah/12"))
    }

    @Test
    fun surahWithAyah() {
        assertEquals(Screen.SurahDetail(12, 5), DeepLink.parse("/surah/12/5"))
    }

    @Test
    fun surahWithoutNumberIsUnparseable() {
        assertNull(DeepLink.parse("/surah"))
    }

    @Test
    fun simpleStaticScreens() {
        assertEquals(Screen.Favorites, DeepLink.parse("/favorites"))
        assertEquals(Screen.Search, DeepLink.parse("/search"))
        assertEquals(Screen.Settings, DeepLink.parse("/settings"))
        assertEquals(Screen.ReciterPicker, DeepLink.parse("/reciters"))
        assertEquals(Screen.SajdaAyahs, DeepLink.parse("/sajda"))
        assertEquals(Screen.Qibla, DeepLink.parse("/qibla"))
    }

    @Test
    fun lessonsListVsDetail() {
        assertEquals(Screen.TajwidLessonList, DeepLink.parse("/lessons"))
        assertEquals(Screen.TajwidLessonDetail("tajwid-1"), DeepLink.parse("/lessons/tajwid-1"))
    }

    @Test
    fun mushafPageDefaultsToOneWithoutNumber() {
        assertEquals(Screen.MushafPage(1), DeepLink.parse("/mushaf"))
        assertEquals(Screen.MushafPage(42), DeepLink.parse("/mushaf/42"))
    }

    @Test
    fun sectionKindsListAndDetail() {
        assertEquals(Screen.SectionList(SectionKind.JUZ), DeepLink.parse("/juz"))
        assertEquals(Screen.SectionDetail(SectionKind.JUZ, 5), DeepLink.parse("/juz/5"))
        assertEquals(Screen.SectionDetail(SectionKind.PAGE, 100), DeepLink.parse("/page/100"))
    }

    @Test
    fun unknownPathIsUnparseable() {
        assertNull(DeepLink.parse("/not-a-real-screen"))
    }

    @Test
    fun httpsAppLinkResolvesSameAsBarePath() {
        assertEquals(Screen.SurahDetail(2, 5), DeepLink.parse("https://hudaquran.web.app/surah/2/5"))
    }

    @Test
    fun opaqueCustomSchemeResolvesSameAsBarePath() {
        // The single-slash, no-authority form buildLink() actually produces (see DeepLink's docs).
        assertEquals(Screen.Qibla, DeepLink.parse("hudaquran:/qibla"))
    }

    @Test
    fun buildLinkRoundTripsThroughParse() {
        val screens = listOf(
            Screen.Home,
            Screen.Qibla,
            Screen.Favorites,
            Screen.SurahDetail(2, 5),
            Screen.SurahDetail(2, null),
            Screen.MushafPage(42),
            Screen.SectionDetail(SectionKind.JUZ, 3),
            Screen.TajwidLessonDetail("tajwid-1"),
        )
        for (screen in screens) {
            val link = DeepLink.buildLink(screen)
            assertEquals(screen, DeepLink.parse(link), "round trip failed for $screen via $link")
        }
    }

    @Test
    fun buildLinkUsesSingleSlashOpaqueForm() {
        assertEquals("hudaquran:/surah/2/5", DeepLink.buildLink(Screen.SurahDetail(2, 5)))
    }
}
