package org.ferdidrgn.hudaquran.notifications

import kotlinx.serialization.Serializable

/** One local notification to fire at [atEpochMillis]. [id] is stable so re-planning replaces it. */
@Serializable
data class Reminder(
    val id: Int,
    val atEpochMillis: Long,
    val title: String,
    val body: String,
)

/**
 * Arms local notifications with the platform scheduler (AlarmManager on Android, the user
 * notification center on iOS). [schedule] replaces everything armed before.
 */
expect class PrayerNotificationScheduler() {
    fun schedule(reminders: List<Reminder>)
    fun cancelAll()
}
