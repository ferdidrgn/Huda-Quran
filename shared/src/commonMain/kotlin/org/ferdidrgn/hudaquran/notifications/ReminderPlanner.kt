package org.ferdidrgn.hudaquran.notifications

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.ferdidrgn.hudaquran.data.repository.OccasionCountdown
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.stringsFor

/** Nights observed on the evening before their Hijri date (the Gregorian date the API returns). */
private val kandilIds = setOf("mevlid", "mirac", "berat", "kadir")

private const val DAYS_OF_PRAYERS = 2
private const val MAX_REMINDERS = 60 // iOS keeps at most 64 pending requests per app
private const val OCCASION_REMINDER_TIME = "10:00"

/**
 * Builds the whole reminder plan from the user's settings and hands it to the platform scheduler:
 * - a gentle pre-reminder [lead] minutes before each prayer (default 10), and optionally one when
 *   the time begins, for today and tomorrow — so reminders keep coming even if the app isn't
 *   opened for a day;
 * - two days and one day before each upcoming kandil / blessed day, at 10:00.
 *
 * Called whenever something relevant changes (app open, settings, location).
 */
object ReminderPlanner {
    suspend fun reschedule() {
        val preferences = AppContainer.preferences
        val scheduler = PrayerNotificationScheduler()
        if (!preferences.prayerNotificationsEnabled.value) {
            scheduler.cancelAll()
            return
        }
        val strings = stringsFor(preferences.appLanguage.value)
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date
        val repository = AppContainer.prayerRepository
        val city = preferences.prayerCity
        val country = preferences.prayerCountry

        val reminders = mutableListOf<Reminder>()
        for (dayIndex in 0 until DAYS_OF_PRAYERS) {
            val date = today.plus(dayIndex, DateTimeUnit.DAY)
            val timings = runCatching { repository.getTimings(city, country, date) }.getOrNull() ?: continue
            reminders += prayerReminders(
                timings = timings,
                date = date,
                dayIndex = dayIndex,
                leadMinutes = preferences.prayerReminderLeadMinutes.value,
                atTime = preferences.prayerAtTimeEnabled.value,
                strings = strings,
                zone = zone,
            )
        }
        if (preferences.occasionRemindersEnabled.value) {
            val occasions = runCatching { repository.getUpcomingIslamicOccasions(today) }.getOrNull().orEmpty()
            reminders += occasionReminders(occasions, strings, zone)
        }

        val now = Clock.System.now().toEpochMilliseconds()
        scheduler.schedule(
            reminders.filter { it.atEpochMillis > now }.sortedBy { it.atEpochMillis }.take(MAX_REMINDERS),
        )
    }
}

internal fun prayerReminders(
    timings: PrayerTimes,
    date: LocalDate,
    dayIndex: Int,
    leadMinutes: Int,
    atTime: Boolean,
    strings: Strings,
    zone: TimeZone,
): List<Reminder> = buildList {
    timings.prayers.forEachIndexed { prayerIndex, prayer ->
        val at = epochMillisOf(date, prayer.time, zone) ?: return@forEachIndexed
        val baseId = 7_000 + dayIndex * 20 + prayerIndex * 2
        if (leadMinutes > 0) {
            add(
                Reminder(
                    id = baseId,
                    atEpochMillis = at - leadMinutes * 60_000L,
                    title = strings.prayerSoonTitleTemplate.replace("{prayer}", prayer.label).replace("{n}", leadMinutes.toString()),
                    body = strings.prayerSoonBody,
                ),
            )
        }
        if (atTime) {
            add(
                Reminder(
                    id = baseId + 1,
                    atEpochMillis = at,
                    title = strings.prayerNowTitleTemplate.replace("{prayer}", prayer.label),
                    body = strings.prayerNowBody,
                ),
            )
        }
    }
}

internal fun occasionReminders(occasions: List<OccasionCountdown>, strings: Strings, zone: TimeZone): List<Reminder> =
    buildList {
        occasions.take(4).forEachIndexed { index, countdown ->
            val isKandil = countdown.occasion.id in kandilIds
            // A kandil is the night before its date; aim the countdown at that evening.
            val anchor = if (isKandil) countdown.gregorianDate.minus(1, DateTimeUnit.DAY) else countdown.gregorianDate
            val name = countdown.occasion.name
            listOf(2, 1).forEach { daysBefore ->
                val day = anchor.minus(daysBefore, DateTimeUnit.DAY)
                val at = epochMillisOf(day, OCCASION_REMINDER_TIME, zone) ?: return@forEach
                val title = when {
                    daysBefore == 2 -> strings.occasionTwoDaysTemplate
                    isKandil -> strings.occasionTomorrowNightTemplate
                    else -> strings.occasionTomorrowTemplate
                }.replace("{occasion}", name)
                add(Reminder(id = 9_000 + index * 4 + daysBefore, atEpochMillis = at, title = title, body = strings.occasionBody))
            }
        }
    }

/** "HH:mm" on [date] in [zone] → epoch millis, or null if the time can't be parsed. */
internal fun epochMillisOf(date: LocalDate, time: String, zone: TimeZone): Long? {
    val parts = time.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.take(2)?.toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return LocalDateTime(date, LocalTime(hour, minute)).toInstant(zone).toEpochMilliseconds()
}
