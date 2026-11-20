package org.ferdidrgn.hudaquran.widget

import org.ferdidrgn.hudaquran.notifications.Reminder
import org.ferdidrgn.hudaquran.notifications.ReminderKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerWidgetLogicTest {
    @Test
    fun selectsOnlyTheNextRealPrayer() {
        val now = 1_000_000L
        val reminders = listOf(
            Reminder(1, now + 30_000, "10 dakika önce", "", ReminderKind.PRAYER_PRE),
            Reminder(2, now + 120_000, "Öğle", "", ReminderKind.PRAYER_WIDGET, false),
            Reminder(3, now + 60_000, "Vakit bildirimi", "", ReminderKind.PRAYER_AT_TIME),
        )

        assertEquals("Öğle", PrayerWidgetLogic.next(reminders, now)?.title)
        assertEquals(now + 60_000, PrayerWidgetLogic.nextRefreshAt(reminders, now, 60_000, 1_800_000))
    }

    @Test
    fun doesNotShowStaleOrNotificationOnlyRecords() {
        val now = 1_000_000L
        val reminders = listOf(
            Reminder(1, now - 1, "Geçmiş", "", ReminderKind.PRAYER_WIDGET, false),
            Reminder(2, now + 10_000, "Ön bildirim", "", ReminderKind.PRAYER_PRE),
        )

        assertNull(PrayerWidgetLogic.next(reminders, now))
        assertTrue(PrayerWidgetLogic.display(reminders, now) == null)
    }

    @Test
    fun countdownRoundsUpAndRefreshesEveryMinute() {
        val now = 2_000_000L
        val item = Reminder(1, now + 59_000, "İkindi", "", ReminderKind.PRAYER_WIDGET, false)
        assertEquals("1 dk kaldı", PrayerWidgetLogic.display(listOf(item), now)?.second?.substringAfter("· "))
        assertEquals(now + 59_000, PrayerWidgetLogic.nextRefreshAt(listOf(item), now, 60_000, 1_800_000))
    }
}
