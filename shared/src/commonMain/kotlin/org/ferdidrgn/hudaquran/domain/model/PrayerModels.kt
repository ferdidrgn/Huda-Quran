package org.ferdidrgn.hudaquran.domain.model

data class PrayerTime(val key: String, val label: String, val time: String)

data class PrayerTimes(
    val prayers: List<PrayerTime>,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    fun timeFor(key: String): String = prayers.firstOrNull { it.key == key }?.time.orEmpty()
}
