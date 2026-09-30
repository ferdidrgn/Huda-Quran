package org.ferdidrgn.hudaquran.notifications

// The website doesn't schedule notifications.
actual class PrayerNotificationScheduler actual constructor() {
    actual fun schedule(reminders: List<Reminder>) = Unit
    actual fun cancelAll() = Unit
}
