package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.MapSettings
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppPreferencesTest {

    private fun newPreferences() = AppPreferences(settings = MapSettings())

    @Test
    fun favoritesToggleOnAndOff() {
        val prefs = newPreferences()
        assertFalse(prefs.isFavorite(2, 5))

        prefs.toggleFavorite(2, 5)
        assertTrue(prefs.isFavorite(2, 5))
        assertTrue(prefs.favorites.value.contains("2:5"))

        prefs.toggleFavorite(2, 5)
        assertFalse(prefs.isFavorite(2, 5))
    }

    @Test
    fun favoritesAreIndependentPerAyah() {
        val prefs = newPreferences()
        prefs.toggleFavorite(2, 5)
        assertFalse(prefs.isFavorite(2, 6))
        assertFalse(prefs.isFavorite(3, 5))
    }

    @Test
    fun lastReadStartsNullAndPersistsAfterSave() {
        val prefs = newPreferences()
        assertEquals(null, prefs.lastRead.value)

        prefs.saveLastRead(surahNumber = 18, numberInSurah = 10, surahName = "Al-Kahf")
        val saved = prefs.lastRead.value
        assertEquals(18, saved?.surahNumber)
        assertEquals(10, saved?.numberInSurah)
        assertEquals("Al-Kahf", saved?.surahName)
    }

    @Test
    fun khatmProgressOnlyMovesForward() {
        val prefs = newPreferences()
        assertEquals(0, prefs.khatmFurthestPage.value)

        prefs.advanceKhatmProgress(pageNumber = 50, totalPages = TOTAL_MUSHAF_PAGES)
        assertEquals(50, prefs.khatmFurthestPage.value)

        // Jumping back to re-read an earlier page must not undo recorded progress.
        prefs.advanceKhatmProgress(pageNumber = 10, totalPages = TOTAL_MUSHAF_PAGES)
        assertEquals(50, prefs.khatmFurthestPage.value)

        prefs.advanceKhatmProgress(pageNumber = 51, totalPages = TOTAL_MUSHAF_PAGES)
        assertEquals(51, prefs.khatmFurthestPage.value)
    }

    @Test
    fun completingKhatmWrapsToZeroAndIncrementsCount() {
        val prefs = newPreferences()
        assertEquals(0, prefs.khatmCompletedCount.value)

        prefs.advanceKhatmProgress(pageNumber = TOTAL_MUSHAF_PAGES, totalPages = TOTAL_MUSHAF_PAGES)
        assertEquals(0, prefs.khatmFurthestPage.value)
        assertEquals(1, prefs.khatmCompletedCount.value)

        prefs.advanceKhatmProgress(pageNumber = 5, totalPages = TOTAL_MUSHAF_PAGES)
        prefs.advanceKhatmProgress(pageNumber = TOTAL_MUSHAF_PAGES, totalPages = TOTAL_MUSHAF_PAGES)
        assertEquals(2, prefs.khatmCompletedCount.value)
    }
}
