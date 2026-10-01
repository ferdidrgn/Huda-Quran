package org.ferdidrgn.hudaquran.notifications

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.repository.OccasionCountdown
import org.ferdidrgn.hudaquran.domain.model.IslamicOccasion
import org.ferdidrgn.hudaquran.domain.model.PrayerTime
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReminderPlannerTest {
    private val utc = TimeZone.UTC
    private val strings = stringsFor(AppLanguage.TURKISH)
    private val day = LocalDate(2026, 10, 1)

    @Test
    fun parsesPrayerTimes() {
        val noon = epochMillisOf(day, "12:30", utc)!!
        assertEquals(epochMillisOf(day, "12:00", utc)!! + 30 * 60_000L, noon)
        assertNull(epochMillisOf(day, "25:00", utc))
        assertNull(epochMillisOf(day, "--:--", utc))
    }

    @Test
    fun preReminderIsLeadMinutesBeforeAndAtTimeIsOptional() {
        val timings = PrayerTimes(listOf(PrayerTime("Dhuhr", "Öğle", "13:05")))
        val both = prayerReminders(timings, day, dayIndex = 0, leadMinutes = 10, atTime = true, strings = strings, zone = utc)
        assertEquals(2, both.size)
        assertEquals(epochMillisOf(day, "12:55", utc), both[0].atEpochMillis)
        assertTrue(both[0].title.contains("Öğle") && both[0].title.contains("10"))
        assertEquals(epochMillisOf(day, "13:05", utc), both[1].atEpochMillis)

        val onlyAtTime = prayerReminders(timings, day, dayIndex = 0, leadMinutes = 0, atTime = true, strings = strings, zone = utc)
        assertEquals(1, onlyAtTime.size)
        val none = prayerReminders(timings, day, dayIndex = 0, leadMinutes = 0, atTime = false, strings = strings, zone = utc)
        assertTrue(none.isEmpty())
    }

    @Test
    fun kandilRemindersCountDownToTheEve() {
        val mevlid = OccasionCountdown(IslamicOccasion("mevlid", "Mevlid Kandili", 3, 12), LocalDate(2026, 8, 25), 10)
        val reminders = occasionReminders(listOf(mevlid), strings, utc)
        assertEquals(2, reminders.size)
        // Eve is 24 Aug; reminders on 22 Aug and 23 Aug at 10:00.
        assertEquals(epochMillisOf(LocalDate(2026, 8, 22), "10:00", utc), reminders[0].atEpochMillis)
        assertEquals(epochMillisOf(LocalDate(2026, 8, 23), "10:00", utc), reminders[1].atEpochMillis)
        assertTrue(reminders[1].title.contains("Yarın akşam"))
    }

    @Test
    fun idsAreStableAndDistinct() {
        val timings = PrayerTimes(
            listOf("Fajr" to "05:00", "Dhuhr" to "12:00", "Asr" to "15:00", "Maghrib" to "18:00", "Isha" to "19:30")
                .map { (k, t) -> PrayerTime(k, k, t) },
        )
        val ids = (0..1).flatMap { d ->
            prayerReminders(timings, day, dayIndex = d, leadMinutes = 10, atTime = true, strings = strings, zone = utc).map { it.id }
        }
        assertEquals(ids.size, ids.toSet().size)
    }
}
