package org.ferdidrgn.hudaquran.notifications

import org.ferdidrgn.hudaquran.domain.model.PrayerTimes

actual class PrayerNotificationScheduler actual constructor() {
    actual fun scheduleToday(prayerTimes: PrayerTimes) {}
    actual fun cancelAll() {}
}
