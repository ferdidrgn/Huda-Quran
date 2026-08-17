package org.ferdidrgn.hudaquran.notifications

import org.ferdidrgn.hudaquran.domain.model.PrayerTimes

/** Schedules local notifications for today's remaining prayer times. Re-call once per day. */
expect class PrayerNotificationScheduler() {
    fun scheduleToday(prayerTimes: PrayerTimes)
    fun cancelAll()
}
