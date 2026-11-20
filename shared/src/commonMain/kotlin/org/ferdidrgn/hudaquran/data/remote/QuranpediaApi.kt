package org.ferdidrgn.hudaquran.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

/**
 * Public Quranpedia tafsir adapter.
 *
 * The API exposes the actual book catalogue and the language attached to each book;
 * we deliberately do not manufacture edition identifiers for languages it does not serve.
 */
class QuranpediaApi(private val client: io.ktor.client.HttpClient = QuranHttpClient.client) {
    companion object {
        const val BASE_URL = "https://api.quranpedia.net/v1"
        const val EDITION_PREFIX = "qp."
    }

    suspend fun getTafsirEditions(): List<QuranpediaTafsirEdition> = coroutineScope {
        val books: List<QuranpediaBookSummary> = client
            .get("$BASE_URL/surah/tafsirs/1")
            .body()

        books.distinctBy { it.id }.map { book ->
            async {
                runCatching {
                    val detail: QuranpediaBookDetail = client
                        .get("$BASE_URL/book/${book.id}")
                        .body()
                    val language = normalizeQuranpediaLanguage(detail.language.code)
                    language?.let {
                        QuranpediaTafsirEdition(
                            identifier = "$EDITION_PREFIX${detail.id}",
                            language = it,
                            displayName = detail.name.ifBlank { book.name },
                            author = detail.author?.fullName?.ifBlank { null },
                        )
                    }
                }.getOrNull()
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun getTafsirForAyah(surahNumber: Int, ayahNumber: Int, edition: String): String {
        val bookId = edition.removePrefix(EDITION_PREFIX).toIntOrNull()
            ?: return ""
        val response: QuranpediaAyahBookResponse = client
            .get("$BASE_URL/ayah/$surahNumber/$ayahNumber/book/$bookId")
            .body()
        return response.content
            .asSequence()
            .map { it.text.stripMarkup() }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }
}

@Serializable
data class QuranpediaTafsirEdition(
    val identifier: String,
    val language: String,
    val displayName: String,
    val author: String? = null,
)

@Serializable
data class QuranpediaBookSummary(
    val id: Int,
    val name: String = "",
)

@Serializable
data class QuranpediaBookDetail(
    val id: Int,
    val name: String = "",
    val language: QuranpediaLanguage = QuranpediaLanguage(),
    val author: QuranpediaAuthor? = null,
)

@Serializable
data class QuranpediaLanguage(
    /** Missing metadata must remain missing; never silently label an edition as Arabic. */
    val code: String? = null,
)

@Serializable
data class QuranpediaAuthor(
    val full_name: String? = null,
) {
    val fullName: String?
        get() = full_name
}

@Serializable
data class QuranpediaAyahBookResponse(
    val content: List<QuranpediaContent> = emptyList(),
)

@Serializable
data class QuranpediaContent(
    val text: String = "",
)

private fun String.stripMarkup(): String =
    replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace(Regex("[ \\t]+"), " ")
        .trim()

/** Provider metadata is optional; an absent code must never become a made-up language. */
internal fun normalizeQuranpediaLanguage(code: String?): String? =
    normalizeContentLanguageCode(code)
