package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonObject

/**
 * Quran Foundation Content API adapter.
 *
 * The application must point this at a Huda Quran controlled backend/proxy. OAuth client
 * secrets are intentionally not accepted here: putting a secret in an Android or browser
 * binary would expose it to every user. The proxy owns OAuth and terms/licence enforcement.
 */
class QuranFoundationApi(
    private val config: QuranFoundationProxyConfig,
    private val client: HttpClient = QuranHttpClient.client,
) {
    suspend fun chapters(language: String = "tr"): JsonObject = get("chapters", "language" to language)
    suspend fun verses(chapter: Int, language: String? = null, page: Int? = null): JsonObject = get("verses/by_chapter/$chapter", "language" to language, "page" to page)
    suspend fun recitations(): JsonObject = get("resources/recitations")
    suspend fun translations(language: String? = null): JsonObject = get("resources/translations", "language" to language)
    suspend fun tafsirs(language: String? = null): JsonObject = get("resources/tafsirs", "language" to language)
    suspend fun audio(recitationId: Int, chapter: Int): JsonObject = get("chapter_recitations/$recitationId/$chapter")
    suspend fun search(query: String, language: String = "tr"): JsonObject = get("search", "q" to query, "language" to language)

    private suspend fun get(path: String, vararg parameters: Pair<String, Any?>): JsonObject {
        check(config.baseUrl.isNotBlank()) { "Quran Foundation proxy URL yapılandırılmamış" }
        return client.get(config.baseUrl.trimEnd('/') + "/" + path.trimStart('/')) {
            config.clientId?.takeIf { it.isNotBlank() }?.let { header("x-client-id", it) }
            config.accessToken?.takeIf { it.isNotBlank() }?.let { header("x-auth-token", it) }
            header(HttpHeaders.Accept, "application/json")
            parameters.forEach { (key, value) -> value?.let { parameter(key, it) } }
        }.body()
    }
}

data class QuranFoundationProxyConfig(
    val baseUrl: String,
    /** Optional short-lived token for controlled development environments; never a client secret. */
    val accessToken: String? = null,
    val clientId: String? = null,
)
