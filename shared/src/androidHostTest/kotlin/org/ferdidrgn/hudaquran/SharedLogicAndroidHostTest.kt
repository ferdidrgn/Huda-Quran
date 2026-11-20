package org.ferdidrgn.hudaquran

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.ui.localization.stringsFor

class SharedLogicAndroidHostTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun localizationBundlesLoadOnAndroidHost() {
        AppLanguage.entries.forEach { language ->
            val strings = stringsFor(language)
            assertFalse(strings.navHome.isBlank(), "Missing navHome for $language")
            assertFalse(strings.offlineContentTitle.isBlank(), "Missing offline title for $language")
        }
    }
}
