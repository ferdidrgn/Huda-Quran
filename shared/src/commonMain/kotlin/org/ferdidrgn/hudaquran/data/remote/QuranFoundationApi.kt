package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonObject

/**
 * Quran Foundation read-only API through Huda Quran's Firebase Hosting proxy.
 * OAuth credentials and tokens belong to the proxy and are never sent by this client.
 */
class QuranFoundationApi(
    private val config: QuranFoundationProxyConfig,
    private val client: HttpClient = QuranHttpClient.client,
) {
    suspend fun chapters(language: String = "tr"): JsonObject = content("chapters", "language" to language)

    suspend fun verses(
        chapter: Int,
        language: String? = null,
        page: Int = 1,
        perPage: Int = 50,
        audioRecitationId: Int? = null,
        translationIds: String? = null,
        tafsirIds: String? = null,
    ): JsonObject {
        require(chapter in 1..114) { "Sure numarası 1 ile 114 arasında olmalı" }
        require(page > 0) { "Sayfa numarası pozitif olmalı" }
        require(perPage in 1..50) { "Sayfa boyutu 1 ile 50 arasında olmalı" }
        return content(
            "verses/by_chapter/$chapter",
            "language" to language,
            "page" to page,
            "per_page" to perPage,
            "audio" to audioRecitationId,
            "translations" to translationIds,
            "tafsirs" to tafsirIds,
        )
    }

    /** Ayet ayet okuma kimlikleri; bunlar sure bazlı kıraatçı kimliklerinden farklıdır. */
    suspend fun recitations(language: String? = null): JsonObject =
        content("resources/recitations", "language" to language)

    /** Sure bazlı kıraatçı katalogu; ses dosyaları için chapterAudio kullanılır. */
    suspend fun chapterReciters(language: String = "tr"): JsonObject =
        content("resources/chapter_reciters", "language" to language)

    suspend fun translations(language: String? = null): JsonObject =
        content("resources/translations", "language" to language)

    suspend fun tafsirs(language: String? = null): JsonObject =
        content("resources/tafsirs", "language" to language)

    suspend fun chapterAudio(reciterId: Int, chapter: Int? = null, segments: Boolean = false): JsonObject {
        require(reciterId > 0) { "Kıraatçı numarası pozitif olmalı" }
        chapter?.let { require(it in 1..114) { "Sure numarası 1 ile 114 arasında olmalı" } }
        val route = if (chapter == null) "chapter_recitations/$reciterId" else "chapter_recitations/$reciterId/$chapter"
        return content(route, "segments" to segments)
    }

    suspend fun search(
        query: String,
        mode: SearchMode = SearchMode.QUICK,
        page: Int? = null,
        size: Int? = null,
        translationIds: String? = null,
    ): JsonObject {
        require(query.isNotBlank() && query.length <= 200) { "Arama metni boş olamaz ve 200 karakteri aşamaz" }
        return get(
            "search/api/v1/search",
            "mode" to mode.value,
            "query" to query,
            "page" to page,
            "size" to size,
            "translation_ids" to translationIds,
        )
    }

    private suspend fun content(path: String, vararg parameters: Pair<String, Any?>): JsonObject =
        get("content/api/v4/$path", *parameters)

    private suspend fun get(path: String, vararg parameters: Pair<String, Any?>): JsonObject {
        check(config.baseUrl.isNotBlank()) { "Quran Foundation proxy URL yapılandırılmamış" }
        return client.get(config.baseUrl.trimEnd('/') + "/" + path.trimStart('/')) {
            header(HttpHeaders.Accept, "application/json")
            parameters.forEach { (key, value) -> value?.let { parameter(key, it) } }
        }.body()
    }
}

enum class SearchMode(val value: String) {
    QUICK("quick"),
    ADVANCED("advanced"),
}

data class QuranFoundationProxyConfig(val baseUrl: String)
