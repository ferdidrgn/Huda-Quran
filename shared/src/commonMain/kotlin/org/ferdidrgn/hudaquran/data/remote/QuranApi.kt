package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.ferdidrgn.hudaquran.data.remote.dto.ApiResponseDto
import org.ferdidrgn.hudaquran.data.remote.dto.AyahDto
import org.ferdidrgn.hudaquran.data.remote.dto.EditionDto
import org.ferdidrgn.hudaquran.data.remote.dto.QuranMetaDto
import org.ferdidrgn.hudaquran.data.remote.dto.SajdaResponseDto
import org.ferdidrgn.hudaquran.data.remote.dto.SearchResultDto
import org.ferdidrgn.hudaquran.data.remote.dto.SectionEditionDto
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

    // Juz/Page/Manzil/Ruku/HizbQuarter have no multi-edition endpoint (unlike Surah/Ayah) — only
    // /{kind}/{number}/{edition} for a single edition, so fetch each edition separately in parallel.
    suspend fun getSectionWithEditions(apiPath: String, number: Int, editions: List<String>): List<SectionEditionDto> = coroutineScope {
        editions.map { edition ->
            async {
                val response: ApiResponseDto<SectionEditionDto> =
                    client.get("${QuranHttpClient.BASE_URL}/$apiPath/$number/$edition").body()
                response.data
            }
        }.awaitAll()
    }

    suspend fun getAyahWithEditions(globalAyahNumber: Int, editions: List<String>): List<AyahEditionResult> {
        val editionsPath = editions.joinToString(",")
        val response: ApiResponseDto<List<AyahEditionDto>> =
            client.get("${QuranHttpClient.BASE_URL}/ayah/$globalAyahNumber/editions/$editionsPath").body()
        return response.data.map { AyahEditionResult(it.surah, it.text, it.numberInSurah, it.audio) }
    }

    suspend fun getRandomAyahWithEditions(editions: List<String>): List<AyahEditionResult> {
        val editionsPath = editions.joinToString(",")
        val response: ApiResponseDto<List<AyahEditionDto>> =
            client.get("${QuranHttpClient.BASE_URL}/ayah/random/editions/$editionsPath").body()
        return response.data.map { AyahEditionResult(it.surah, it.text, it.numberInSurah, it.audio) }
    }

    // /sajda has no multi-edition endpoint either, so callers fetch one edition at a time.
    suspend fun getSajdaAyahs(edition: String): SajdaResponseDto {
        val response: ApiResponseDto<SajdaResponseDto> =
            client.get("${QuranHttpClient.BASE_URL}/sajda/$edition").body()
        return response.data
    }

    suspend fun getMeta(): QuranMetaDto {
        val response: ApiResponseDto<QuranMetaDto> = client.get("${QuranHttpClient.BASE_URL}/meta").body()
        return response.data
    }

    suspend fun getEditions(format: String? = null, language: String? = null, type: String? = null): List<EditionDto> {
        val response: ApiResponseDto<List<EditionDto>> = client.get("${QuranHttpClient.BASE_URL}/edition") {
            format?.let { parameter("format", it) }
            language?.let { parameter("language", it) }
            type?.let { parameter("type", it) }
        }.body()
        return response.data
    }

    suspend fun search(keyword: String, edition: String): SearchResultDto {
        val encoded = keyword.encodeURLParameter(spaceToPlus = false)
        val response: ApiResponseDto<SearchResultDto> =
            client.get("${QuranHttpClient.BASE_URL}/search/$encoded/all/$edition").body()
        return response.data
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
