package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.ferdidrgn.hudaquran.data.remote.dto.HadithCategoryDto
import org.ferdidrgn.hudaquran.data.remote.dto.HadithDetailDto
import org.ferdidrgn.hudaquran.data.remote.dto.HadithLanguageDto
import org.ferdidrgn.hudaquran.data.remote.dto.HadithListResponseDto

/** Thin client for HadeethEnc API v1; no hadith data is embedded in the APK. */
class HadithApi(private val client: HttpClient = QuranHttpClient.client) {
    suspend fun getLanguages(): List<HadithLanguageDto> =
        client.get("$BASE_URL/languages").body()

    suspend fun getCategories(languageCode: String): List<HadithCategoryDto> =
        client.get("$BASE_URL/categories/list/") { parameter("language", languageCode) }.body()

    suspend fun getHadiths(
        languageCode: String,
        categoryId: String = "",
        page: Int = 1,
        perPage: Int = DEFAULT_PAGE_SIZE,
    ): HadithListResponseDto = client.get("$BASE_URL/hadeeths/list/") {
        parameter("language", languageCode)
        if (categoryId.isNotBlank()) parameter("category_id", categoryId)
        parameter("page", page)
        parameter("per_page", perPage)
    }.body()

    suspend fun getHadith(id: String, languageCode: String): HadithDetailDto =
        client.get("$BASE_URL/hadeeths/one/") {
            parameter("id", id)
            parameter("language", languageCode)
        }.body()

    companion object {
        const val BASE_URL = "https://hadeethenc.com/api/v1"
        const val DEFAULT_PAGE_SIZE = 20
    }
}
