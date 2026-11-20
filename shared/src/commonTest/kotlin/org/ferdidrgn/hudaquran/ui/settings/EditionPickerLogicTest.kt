package org.ferdidrgn.hudaquran.ui.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class EditionPickerLogicTest {
    private val items = listOf(
        PickerItem("tr-1", "Diyanet", languageCode = " TR "),
        PickerItem("en-1", "English Meaning", languageCode = "en"),
    )

    @Test
    fun languageFilterMatchesNormalizedProviderMetadata() {
        assertEquals(listOf("tr-1"), filterPickerItems(items, "", "tr").map(PickerItem::id))
    }

    @Test
    fun queryAndLanguageFilterCanBeCombined() {
        assertEquals(listOf("en-1"), filterPickerItems(items, "meaning", " EN ").map(PickerItem::id))
    }

    @Test
    fun blankLanguageFilterShowsAllSearchMatches() {
        assertEquals(listOf("tr-1"), filterPickerItems(items, "diyanet", "").map(PickerItem::id))
    }
}
