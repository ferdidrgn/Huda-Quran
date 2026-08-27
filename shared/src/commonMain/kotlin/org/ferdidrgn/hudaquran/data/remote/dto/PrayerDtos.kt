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
