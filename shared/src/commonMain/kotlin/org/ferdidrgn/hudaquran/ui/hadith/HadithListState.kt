package org.ferdidrgn.hudaquran.ui.hadith

import org.ferdidrgn.hudaquran.domain.model.HadithPage
import org.ferdidrgn.hudaquran.domain.model.HadithSummary

internal const val HADITH_PAGE_SIZE = 20
internal const val HADITH_LOAD_TIMEOUT_MS = 9_000L

/** UI state for one category/query. Keeping page transitions together avoids stale combinations. */
internal data class HadithListState(
    val items: List<HadithSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val pageToLoad: Int = 1,
    val loadedPage: Int = 0,
    val lastPage: Int = 1,
    val failedPage: Int = 1,
) {
    val canLoadMore: Boolean get() = !isLoading && !isLoadingMore && loadedPage < lastPage

    /**
     * A successful empty first page is still recoverable: the provider may have returned a
     * transient empty response, especially for the unfiltered "all" view. The screen uses this
     * predicate to keep a retry action visible without turning a real search miss into a network
     * request.
     */
    fun shouldOfferEmptyRetry(query: String): Boolean =
        items.isEmpty() &&
            !isLoading &&
            !isLoadingMore &&
            errorMessage == null &&
            query.trim().isEmpty()
}

internal fun HadithListState.beginLoad(page: Int): HadithListState {
    val requestedPage = page.coerceAtLeast(1)
    return copy(
        isLoading = requestedPage == 1,
        isLoadingMore = requestedPage > 1,
        errorMessage = null,
        pageToLoad = requestedPage,
        loadedPage = if (requestedPage == 1) 0 else loadedPage,
        lastPage = if (requestedPage == 1) 1 else lastPage,
        items = if (requestedPage == 1) emptyList() else items,
        failedPage = requestedPage,
    )
}

internal fun HadithListState.completeLoad(page: HadithPage, requestedPage: Int): HadithListState = copy(
    items = mergeHadithSummaries(items, page.items, requestedPage),
    isLoading = false,
    isLoadingMore = false,
    errorMessage = null,
    pageToLoad = requestedPage,
    loadedPage = requestedPage,
    // An empty successful page is an authoritative end marker. This prevents a stale provider
    // metadata value from keeping the "load more" action alive forever.
    lastPage = if (page.items.isEmpty()) requestedPage else maxOf(requestedPage, page.lastPage),
)

internal fun HadithListState.failLoad(message: String, requestedPage: Int): HadithListState = copy(
    isLoading = false,
    isLoadingMore = false,
    errorMessage = message,
    pageToLoad = requestedPage,
    failedPage = requestedPage,
)

internal fun mergeHadithSummaries(
    existing: List<HadithSummary>,
    incoming: List<HadithSummary>,
    requestedPage: Int,
): List<HadithSummary> = if (requestedPage <= 1) {
    incoming.distinctBy(HadithSummary::id)
} else {
    (existing + incoming).distinctBy(HadithSummary::id)
}

internal fun filterHadithSummaries(
    items: List<HadithSummary>,
    query: String,
): List<HadithSummary> {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isBlank()) return items
    return items.filter {
        it.title.lowercase().contains(normalizedQuery) || it.id.lowercase().contains(normalizedQuery)
    }
}
