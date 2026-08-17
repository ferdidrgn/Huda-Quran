package org.ferdidrgn.hudaquran.notifications

import org.ferdidrgn.hudaquran.domain.model.PrayerTimes

// iOS local-notification scheduling (UNUserNotificationCenter) is not implemented yet.
actual class PrayerNotificationScheduler actual constructor() {
    actual fun scheduleToday(prayerTimes: PrayerTimes) {}
    actual fun cancelAll() {}
}
