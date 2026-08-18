package org.ferdidrgn.hudaquran.data.repository

import org.ferdidrgn.hudaquran.data.remote.PrayerApi
import org.ferdidrgn.hudaquran.domain.model.PrayerTime
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes

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
        return PrayerTimes(prayers)
    }
}

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
