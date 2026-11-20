package org.ferdidrgn.hudaquran.ui.tracker

import kotlin.test.Test
import kotlin.test.assertEquals

class WorshipTrackerLogicTest {
    @Test
    fun choosingSameMarkClearsIt() {
        assertEquals(PrayerMark.PENDING, togglePrayerMark(PrayerMark.COMPLETED, PrayerMark.COMPLETED))
        assertEquals(PrayerMark.PENDING, togglePrayerMark(PrayerMark.QADA, PrayerMark.QADA))
    }

    @Test
    fun choosingAnotherMarkReplacesPreviousMark() {
        assertEquals(PrayerMark.QADA, togglePrayerMark(PrayerMark.COMPLETED, PrayerMark.QADA))
        assertEquals(PrayerMark.COMPLETED, togglePrayerMark(PrayerMark.QADA, PrayerMark.COMPLETED))
    }

    @Test
    fun weeklyProgressCountsOnlyCompletedPrayers() {
        assertEquals(15 to 25, weeklyPrayerProgress(listOf(PrayerDaySummary(completed = 10, qada = 2), PrayerDaySummary(completed = 5, qada = 4))))
    }

    @Test
    fun weeklyTargetCannotBeZero() {
        assertEquals(0 to 1, weeklyPrayerProgress(emptyList(), target = 0))
    }
}
