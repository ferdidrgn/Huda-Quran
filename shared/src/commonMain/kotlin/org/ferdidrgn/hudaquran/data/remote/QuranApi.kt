package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import org.ferdidrgn.hudaquran.data.remote.dto.ApiResponseDto
import org.ferdidrgn.hudaquran.data.remote.dto.AyahDto
import org.ferdidrgn.hudaquran.data.remote.dto.SurahDto
import org.ferdidrgn.hudaquran.data.remote.dto.SurahEditionDto

class QuranApi(private val client: io.ktor.client.HttpClient = QuranHttpClient.client) {

    suspend fun getSurahList(): List<SurahDto> {
        val response: ApiResponseDto<List<SurahDto>> =
            client.get("${QuranHttpClient.BASE_URL}/surah").body()
        return response.data
    }

    suspend fun getSurahWithEditions(surahNumber: Int, editions: List<String>): List<SurahEditionDto> {
        val editionsPath = editions.joinToString(",")
        val response: ApiResponseDto<List<SurahEditionDto>> =
            client.get("${QuranHttpClient.BASE_URL}/surah/$surahNumber/editions/$editionsPath").body()
        return response.data
    }

    suspend fun getAyahWithEditions(globalAyahNumber: Int, editions: List<String>): List<AyahEditionResult> {
        val editionsPath = editions.joinToString(",")
        val response: ApiResponseDto<List<AyahEditionDto>> =
            client.get("${QuranHttpClient.BASE_URL}/ayah/$globalAyahNumber/editions/$editionsPath").body()
        return response.data.map { AyahEditionResult(it.surah, it.text, it.numberInSurah, it.audio) }
    }
}

@kotlinx.serialization.Serializable
data class AyahEditionDto(
    val number: Int,
    val text: String,
    val surah: SurahDto,
    val numberInSurah: Int,
    val audio: String? = null,
)

data class AyahEditionResult(
    val surah: SurahDto,
    val text: String,
    val numberInSurah: Int,
    val audio: String?,
)
