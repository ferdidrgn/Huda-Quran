package org.ferdidrgn.hudaquran.data.repository

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.ferdidrgn.hudaquran.data.remote.PrayerApi
import org.ferdidrgn.hudaquran.domain.model.IslamicOccasion
import org.ferdidrgn.hudaquran.domain.model.PrayerTime
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.domain.model.islamicOccasions

private val prayerLabels = listOf(
    "Fajr" to "İmsak",
    "Dhuhr" to "Öğle",
    "Asr" to "İkindi",
    "Maghrib" to "Akşam",
    "Isha" to "Yatsı",
)

class PrayerRepository(private val api: PrayerApi = PrayerApi()) {
    suspend fun getTodayTimings(city: String, country: String): PrayerTimes {
        val dto = api.getTimingsByCity(city, country)
        val prayers = prayerLabels.map { (key, label) ->
            val raw = dto.timings[key].orEmpty().substringBefore(" ")
            PrayerTime(key, label, raw)
        }
        return PrayerTimes(prayers, latitude = dto.meta?.latitude, longitude = dto.meta?.longitude)
    }

    /**
     * For each fixed Hijri-calendar occasion, finds its next real Gregorian occurrence (this
     * Hijri year if it hasn't passed yet, otherwise next year) via AlAdhan's date conversion
     * endpoints, rather than approximating with an average lunar-month length — a real conversion
     * is the only way to get this right for a genuine religious date.
     */
    suspend fun getUpcomingIslamicOccasions(today: LocalDate): List<OccasionCountdown> {
        val todayStr = "${pad(today.dayOfMonth)}-${pad(today.monthNumber)}-${today.year}"
        val hijriToday = api.gregorianToHijri(todayStr).hijri ?: return emptyList()
        val currentYear = hijriToday.year?.toIntOrNull() ?: return emptyList()
        val currentMonth = hijriToday.month?.number ?: return emptyList()
        val currentDay = hijriToday.day?.toIntOrNull() ?: return emptyList()

        return islamicOccasions.mapNotNull { occasion ->
            val alreadyPassed = occasion.hijriMonth < currentMonth ||
                (occasion.hijriMonth == currentMonth && occasion.hijriDay < currentDay)
            val targetYear = if (alreadyPassed) currentYear + 1 else currentYear
            val targetHijri = "${pad(occasion.hijriDay)}-${pad(occasion.hijriMonth)}-$targetYear"
            val gregorianStr = runCatching { api.hijriToGregorian(targetHijri).gregorian?.date }.getOrNull()
                ?: return@mapNotNull null
            val parts = gregorianStr.split("-")
            if (parts.size != 3) return@mapNotNull null
            val day = parts[0].toIntOrNull() ?: return@mapNotNull null
            val month = parts[1].toIntOrNull() ?: return@mapNotNull null
            val year = parts[2].toIntOrNull() ?: return@mapNotNull null
            val date = runCatching { LocalDate(year, month, day) }.getOrNull() ?: return@mapNotNull null
            OccasionCountdown(occasion, date, today.daysUntil(date))
        }.sortedBy { it.daysRemaining }
    }

    private fun pad(value: Int): String = value.toString().padStart(2, '0')
}

data class OccasionCountdown(val occasion: IslamicOccasion, val gregorianDate: LocalDate, val daysRemaining: Int)

data class NextPrayer(val prayer: PrayerTime, val minutesUntil: Int)

fun PrayerTimes.nextPrayer(nowHour: Int, nowMinute: Int): NextPrayer? {
    val nowTotal = nowHour * 60 + nowMinute
    val withMinutes = prayers.mapNotNull { p ->
        val parts = p.time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
        p to (h * 60 + m)
    }
    if (withMinutes.isEmpty()) return null
    val upcoming = withMinutes.filter { it.second > nowTotal }.minByOrNull { it.second }
    val chosen = upcoming ?: withMinutes.minByOrNull { it.second }!!
    val minutesUntil = if (upcoming != null) chosen.second - nowTotal else (24 * 60 - nowTotal) + chosen.second
    return NextPrayer(chosen.first, minutesUntil)
}
