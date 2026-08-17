package org.ferdidrgn.hudaquran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrayerDataDto(
    val timings: Map<String, String>,
)
