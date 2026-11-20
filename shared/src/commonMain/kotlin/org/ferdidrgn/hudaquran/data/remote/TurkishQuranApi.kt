package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

/** Read-only Turkish translation/commentary catalog and verse adapter. */
class TurkishQuranApi(private val client: io.ktor.client.HttpClient = QuranHttpClient.client) {
    companion object {
        const val BASE_URL = "https://www.kuran-meali.com.tr/api/v1"
    }

    suspend fun getEditions(): List<TurkishEditionDto> {
        val response: TurkishApiResponse<List<TurkishEditionDto>> = client.get("$BASE_URL/translations").body()
        return response.data.filter { normalizeContentLanguageCode(it.language) == "tr" }
    }

    suspend fun getVerse(surahNumber: Int, ayahNumber: Int, editionSlug: String): String {
        val response: TurkishApiResponse<TurkishVerseDto> = client.get("$BASE_URL/verses/$surahNumber/$ayahNumber") {
            parameter("translations", editionSlug)
        }.body()
        return response.data.translations.firstOrNull { it.slug == editionSlug }?.text.orEmpty()
    }
}

@Serializable
data class TurkishApiResponse<T>(val status: String = "", val data: T)

@Serializable
data class TurkishEditionDto(
    val slug: String,
    val name: String,
    val author: String? = null,
    val language: String = "",
)

@Serializable
data class TurkishVerseDto(val translations: List<TurkishVerseTranslationDto> = emptyList())

@Serializable
data class TurkishVerseTranslationDto(
    val slug: String,
    val name: String = "",
    val author: String? = null,
    val language: String = "",
    val text: String = "",
)
