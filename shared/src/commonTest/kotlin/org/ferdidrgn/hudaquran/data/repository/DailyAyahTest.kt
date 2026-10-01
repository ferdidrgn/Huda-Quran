package org.ferdidrgn.hudaquran.data.repository

import com.russhwolf.settings.MapSettings
import org.ferdidrgn.hudaquran.data.local.QuranCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DailyAyahTest {

    @Test
    fun sameDayGivesSameAyah() {
        assertEquals(dailyAyahNumberFor(20_000L), dailyAyahNumberFor(20_000L))
    }

    @Test
    fun everyAyahIsVisitedOnceBeforeRepeating() {
        val seen = (0L until 6236L).map { dailyAyahNumberFor(it) }.toSet()
        assertEquals(6236, seen.size)
        assertTrue(seen.all { it in 1..6236 })
    }

    @Test
    fun consecutiveDaysAreNotNeighbouringAyahs() {
        val a = dailyAyahNumberFor(20_000L)
        val b = dailyAyahNumberFor(20_001L)
        assertTrue(kotlin.math.abs(a - b) > 1)
    }

    @Test
    fun cacheRoundTripsDailyAyah() {
        val cache = QuranCache(settings = MapSettings())
        assertNull(cache.getDailyAyah())
        val daily = DailyAyah("Al-Faatiha", 1, 2, "arabic", "meal", date = "2026-09-30", translationEdition = "tr.diyanet")
        cache.saveDailyAyah(daily)
        assertEquals(daily, cache.getDailyAyah())
    }
}
