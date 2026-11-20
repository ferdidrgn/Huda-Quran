package org.ferdidrgn.hudaquran.data.repository

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ferdidrgn.hudaquran.data.local.HadithLocalDataSource
import org.ferdidrgn.hudaquran.data.local.VaktelyaHadithLocalDataSource
import org.ferdidrgn.hudaquran.data.remote.HadithApi
import org.ferdidrgn.hudaquran.data.remote.dto.toDomain
import org.ferdidrgn.hudaquran.data.local.createSettings
import org.ferdidrgn.hudaquran.domain.model.Hadith
import org.ferdidrgn.hudaquran.domain.model.HadithAttribution
import org.ferdidrgn.hudaquran.domain.model.HadithCategory
import org.ferdidrgn.hudaquran.domain.model.HadithLanguage
import org.ferdidrgn.hudaquran.domain.model.HadithPage
import org.ferdidrgn.hudaquran.domain.model.HadithSummary
import org.ferdidrgn.hudaquran.domain.model.HadeethEncAttribution
import org.ferdidrgn.hudaquran.domain.model.ContentSourceBadge

/**
 * Repository seam for future Hadith screens. Remote data is preferred, while a supplied local
 * source or a previously opened detail can provide an offline fallback.
 */
class HadithRepository(
    private val api: HadithApi = HadithApi(),
    private val local: HadithLocalDataSource = VaktelyaHadithLocalDataSource,
    private val settings: Settings = createSettings(),
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val categoriesCache = mutableMapOf<String, List<HadithCategory>>()
    private val categoriesMutex = Mutex()

    /** Raised only when no remote, cached, or approved local content can answer the request. */
    class LoadException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

    suspend fun languages(): List<HadithLanguage> = runCatching {
        withTimeoutOrNull(REMOTE_TIMEOUT_MS) { api.getLanguages().map { it.toDomain() } }
            ?: throw LoadException("Hadis dilleri zamanında alınamadı.")
    }.getOrElse { DEFAULT_LANGUAGES }

    suspend fun categories(languageCode: String): List<HadithCategory> =
        loadCategories(languageCode).getOrElse { emptyList() }

    private suspend fun loadCategories(languageCode: String): Result<List<HadithCategory>> {
        return categoriesMutex.withLock {
            categoriesCache[languageCode]?.let { return@withLock Result.success(it) }
            runCatching {
                withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                    api.getCategories(languageCode).map { it.toDomain() }
                } ?: throw LoadException("Hadis kategorileri zamanında alınamadı.")
            }.onSuccess { categoriesCache[languageCode] = it }
        }
    }

    suspend fun list(
        languageCode: String = "tr",
        categoryId: String = "",
        page: Int = 1,
        perPage: Int = HadithApi.DEFAULT_PAGE_SIZE,
    ): HadithPage {
        val cacheKey = pageCacheKey(languageCode, categoryId, page, perPage)
        var remoteFailed = false
        val remote = runCatching {
            withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                api.getHadiths(languageCode, categoryId, page, perPage).toDomain()
            }
        }.onFailure { remoteFailed = true }.getOrNull()
        if (remote == null) remoteFailed = true
        // An empty first page is not useful offline evidence: it can be a transient provider
        // response and would otherwise turn the next network failure into a misleading empty
        // screen. Later empty pages remain valid end markers for an already populated cache.
        remote?.takeIf { shouldCacheHadithPage(it, page) }?.let { savePage(cacheKey, it) }
        if (remote != null && remote.items.isNotEmpty()) return remote

        // Some provider deployments return an empty page for an omitted category. In that case,
        // use every category as a bounded, parallel fallback so "Tümü" does not silently show a
        // misleading partial sample.
        if (categoryId.isBlank()) {
            val allCategoryResult = boundedAllCategoryItems(languageCode, page, perPage)
            val allCategoryItems = allCategoryResult.items

            if (allCategoryItems.isNotEmpty()) {
                val pageItems = allCategoryItems
                return HadithPage(
                    items = pageItems,
                    currentPage = page,
                    lastPage = if (page < MAX_ALL_PAGES && allCategoryItems.size >= perPage) page + 1 else page,
                    totalItems = 0,
                    perPage = perPage,
                )
            }

            if (allCategoryResult.failed) {
                val cached = readPage(cacheKey)
                if (cached != null) return cached.copy(currentPage = page, perPage = perPage)
                val localItems = local.list(languageCode)
                    .filter { categoryId.isBlank() || categoryId in it.categories }
                if (localItems.isEmpty()) {
                    throw LoadException("Hadisler alınamadı; ağ bağlantısını kontrol edin.")
                }
            }
        }

        val localItems = local.list(languageCode)
            .filter { categoryId.isBlank() || categoryId in it.categories }
            .map { HadithSummary(it.id, it.title) }
        return localItems.chunked(perPage).getOrNull(page - 1)?.let {
            HadithPage(it, page, (localItems.size + perPage - 1) / perPage, localItems.size, perPage)
        } ?: readPage(cacheKey)?.let { it.copy(currentPage = page, perPage = perPage) }
            ?: if (remoteFailed && localItems.isEmpty()) {
                throw LoadException("Hadisler alınamadı; ağ bağlantısını kontrol edin.")
            } else {
                HadithPage(emptyList(), page, 1, 0, perPage)
            }
    }

    private data class AllCategoryResult(val items: List<HadithSummary>, val failed: Boolean)

    private suspend fun boundedAllCategoryItems(
        languageCode: String,
        page: Int,
        perPage: Int,
    ): AllCategoryResult {
        val requestedPage = page.coerceIn(1, MAX_ALL_PAGES)
        val outcome = withTimeoutOrNull(ALL_VIEW_TIMEOUT_MS) {
            val categoryIds = loadCategories(languageCode)
                .getOrElse { return@withTimeoutOrNull AllCategoryResult(emptyList(), failed = true) }
                .map { it.id }
                .filter(String::isNotBlank)
                .distinct()
            if (categoryIds.isEmpty()) return@withTimeoutOrNull AllCategoryResult(emptyList(), failed = false)
            coroutineScope {
                val responses = categoryIds.map { categoryId ->
                    async {
                        runCatching {
                            api.getHadiths(languageCode, categoryId, requestedPage, perPage)
                                .toDomain().items
                        }
                    }
                }.awaitAll()
                val items = responses.filter { it.isSuccess }
                    .flatMap { it.getOrDefault(emptyList()) }
                    .distinctBy(HadithSummary::id)
                    .take(perPage)
                AllCategoryResult(items, failed = responses.any { it.isFailure })
            }
        }
        return outcome ?: AllCategoryResult(emptyList(), failed = true)
    }

    suspend fun get(id: String, languageCode: String = "tr"): Hadith? {
        val remote = runCatching {
            withTimeoutOrNull(REMOTE_TIMEOUT_MS) { api.getHadith(id, languageCode).toDomain(languageCode) }
        }.getOrNull()
        if (remote != null && remote.text.isNotBlank()) {
            saveCached(remote)
            return remote
        }
        return local.get(languageCode, id) ?: readCached(id, languageCode)
    }

    val attribution: HadithAttribution get() = local.attribution

    fun sourceBadge(languageCode: String = "tr"): ContentSourceBadge = ContentSourceBadge(
        providerName = attribution.providerName,
        sourceUrl = attribution.providerUrl,
        licenseNotice = attribution.notice,
        languageCode = languageCode,
    )

    private fun saveCached(hadith: Hadith) {
        runCatching { settings.putString(cacheKey(hadith.id, hadith.languageCode), json.encodeToString(hadith)) }
    }

    private fun readCached(id: String, languageCode: String): Hadith? =
        settings.getStringOrNull(cacheKey(id, languageCode))
            ?.let { runCatching { json.decodeFromString<Hadith>(it) }.getOrNull() }

    private fun cacheKey(id: String, languageCode: String) = "cache_hadith_${languageCode}_$id"

    private fun pageCacheKey(languageCode: String, categoryId: String, page: Int, perPage: Int) =
        "cache_hadith_page_${languageCode}_${categoryId.ifBlank { "all" }}_${page}_$perPage"

    private fun savePage(key: String, page: HadithPage) {
        runCatching { settings.putString(key, json.encodeToString(page)) }
    }

    private fun readPage(key: String): HadithPage? = settings.getStringOrNull(key)
        ?.let { runCatching { json.decodeFromString<HadithPage>(it) }.getOrNull() }

    companion object {
        val DEFAULT_LANGUAGES = listOf(
            HadithLanguage("tr", "Türkçe"),
            HadithLanguage("ar", "العربية"),
            HadithLanguage("en", "English"),
        )

        private const val REMOTE_TIMEOUT_MS = 8_000L
        private const val ALL_VIEW_TIMEOUT_MS = 2_000L
        private const val MAX_ALL_PAGES = 2
    }
}

/** Do not persist an empty first response as if it were a reliable offline catalogue. */
internal fun shouldCacheHadithPage(page: HadithPage, requestedPage: Int): Boolean =
    page.items.isNotEmpty() || requestedPage > 1
