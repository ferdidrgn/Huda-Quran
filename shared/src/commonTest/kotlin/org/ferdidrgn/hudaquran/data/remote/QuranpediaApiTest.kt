package org.ferdidrgn.hudaquran.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QuranpediaApiTest {
    @Test
    fun missingProviderLanguageIsNotInvented() {
        assertNull(normalizeQuranpediaLanguage(null))
        assertNull(normalizeQuranpediaLanguage("   "))
    }

    @Test
    fun providerLanguageIsTrimmedAndNormalized() {
        assertEquals("tr", normalizeQuranpediaLanguage("  TR "))
    }
}
