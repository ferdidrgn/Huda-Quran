package org.ferdidrgn.hudaquran.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanguageMetadataTest {
    @Test
    fun providerRegionAndUnderscoreCodesShareThePickerCode() {
        assertEquals("fr", normalizeContentLanguageCode(" FR-fr "))
        assertEquals("tr", normalizeContentLanguageCode("TR_tr"))
    }

    @Test
    fun missingLanguageDoesNotBecomeArabicOrTurkish() {
        assertNull(normalizeContentLanguageCode(null))
        assertNull(normalizeContentLanguageCode("   "))
    }
}
