package org.ferdidrgn.hudaquran.notifications

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Local notifications through the system notification center. The planner already caps the plan
 * at 60 requests (iOS keeps at most 64 pending per app); each re-plan replaces the previous set.
 */
actual class PrayerNotificationScheduler actual constructor() {
    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    actual fun schedule(reminders: List<Reminder>) {
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { granted, _ ->
            if (!granted) return@requestAuthorizationWithOptions
            center.removeAllPendingNotificationRequests()
            val nowMillis = NSDate().timeIntervalSince1970 * 1000.0
            reminders.forEach { reminder ->
                val seconds = (reminder.atEpochMillis - nowMillis) / 1000.0
                if (seconds < 1.0) return@forEach
                val content = UNMutableNotificationContent().apply {
                    setTitle(reminder.title)
                    setBody(reminder.body)
                    setSound(UNNotificationSound.defaultSound)
                }
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, repeats = false)
                val request = UNNotificationRequest.requestWithIdentifier("huda_${reminder.id}", content, trigger)
                center.addNotificationRequest(request, withCompletionHandler = null)
            }
        }
    }

    actual fun cancelAll() {
        center.removeAllPendingNotificationRequests()
    }
}
