package org.ferdidrgn.hudaquran.ui.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.ferdidrgn.hudaquran.data.local.AppLanguage

class StringsTest {

    @Test
    fun everySupportedLanguageBuildsACompleteBundle() {
        AppLanguage.entries.forEach { language ->
            val strings = stringsFor(language)

            assertFalse(strings.navHome.isBlank(), "Missing navHome for $language")
            assertFalse(strings.search.isBlank(), "Missing search for $language")
            assertFalse(strings.offlineContentTitle.isBlank(), "Missing offline title for $language")
            assertFalse(strings.shareImageFailed.isBlank(), "Missing share error for $language")
        }
    }

    @Test
    fun publicPropertiesAndDefaultsRemainStable() {
        val turkish = stringsFor(AppLanguage.TURKISH)

        assertEquals("Ana Sayfa", turkish.navHome)
        assertEquals("Bugün", turkish.todayTitle)
        assertEquals("Çevrim dışı içerik", turkish.offlineContentTitle)
        assertEquals("Pusula sensörü bulunamadı", turkish.qiblaSensorNoSensor)
        assertTrue(turkish.offlineProgressTemplate.contains("{done}"))
    }
}
