package org.ferdidrgn.hudaquran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrayerDataDto(
    val timings: Map<String, String>,
    val meta: PrayerMetaDto? = null,
)

@Serializable
data class PrayerMetaDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class HijriGregorianResponseDto(
    val hijri: HijriDateDto? = null,
    val gregorian: GregorianDateDto? = null,
)

@Serializable
data class HijriDateDto(
    val day: String? = null,
    val month: HijriMonthDto? = null,
    val year: String? = null,
)

@Serializable
data class HijriMonthDto(val number: Int? = null)

/** [date] is "DD-MM-YYYY", AlAdhan's format for this field across every endpoint that returns it. */
@Serializable
data class GregorianDateDto(val date: String? = null)
