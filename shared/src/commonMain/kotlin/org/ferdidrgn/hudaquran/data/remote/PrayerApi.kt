package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.ferdidrgn.hudaquran.data.remote.dto.ApiResponseDto
import org.ferdidrgn.hudaquran.data.remote.dto.PrayerDataDto

class PrayerApi(private val client: io.ktor.client.HttpClient = QuranHttpClient.client) {
    companion object {
        const val BASE_URL = "https://api.aladhan.com/v1"
    }

    suspend fun getTimingsByCity(city: String, country: String, method: Int = 13): PrayerDataDto {
        val response: ApiResponseDto<PrayerDataDto> = client.get("$BASE_URL/timingsByCity") {
            parameter("city", city)
            parameter("country", country)
            parameter("method", method)
        }.body()
        return response.data
    }
}
