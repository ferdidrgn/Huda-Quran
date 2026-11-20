package org.ferdidrgn.hudaquran.ui.hadith

import org.ferdidrgn.hudaquran.domain.model.HadithPage
import org.ferdidrgn.hudaquran.domain.model.HadithSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HadithListStateTest {
    @Test
    fun firstPageReplacesOldCategoryAndRemovesDuplicateIds() {
        val old = listOf(HadithSummary("old", "Eski"))
        val incoming = listOf(HadithSummary("1", "Bir"), HadithSummary("1", "Bir"))

        assertEquals(listOf(HadithSummary("1", "Bir")), mergeHadithSummaries(old, incoming, 1))
    }

    @Test
    fun laterPagesAppendWithoutDuplicatingItems() {
        val existing = listOf(HadithSummary("1", "Bir"))
        val incoming = listOf(HadithSummary("1", "Bir"), HadithSummary("2", "İki"))

        assertEquals(
            listOf(HadithSummary("1", "Bir"), HadithSummary("2", "İki")),
            mergeHadithSummaries(existing, incoming, 2),
        )
    }

    @Test
    fun searchTrimsAndMatchesTitleOrIdCaseInsensitively() {
        val items = listOf(
            HadithSummary("42", "Merhamet"),
            HadithSummary("7", "Sabır"),
        )

        assertEquals(listOf(items[0]), filterHadithSummaries(items, "  MERHAMET "))
        assertEquals(listOf(items[0]), filterHadithSummaries(items, "42"))
        assertEquals(items, filterHadithSummaries(items, "   "))
    }

    @Test
    fun loadingMoreRemainsAvailableAfterAnEmptySearchPage() {
        val state = HadithListState(
            items = listOf(HadithSummary("1", "Bir")),
            isLoading = false,
            loadedPage = 1,
            lastPage = 2,
        )

        assertTrue(state.canLoadMore)
        assertTrue(filterHadithSummaries(state.items, "bulunamayan").isEmpty())
        assertFalse(state.beginLoad(2).canLoadMore)
    }

    @Test
    fun failedPageCanBeRetriedWithoutDroppingExistingItems() {
        val state = HadithListState(
            items = listOf(HadithSummary("1", "Bir")),
            isLoading = false,
            loadedPage = 1,
            lastPage = 2,
        )
            .beginLoad(2)
            .failLoad("Bağlantı yok", 2)

        assertEquals(2, state.failedPage)
        assertEquals(listOf(HadithSummary("1", "Bir")), state.items)
        assertFalse(state.isLoadingMore)
        assertEquals(2, state.lastPage)
    }

    @Test
    fun completionNeverMovesLastPageBehindRequestedPage() {
        val state = HadithListState().beginLoad(2)
        val page = HadithPage(emptyList(), currentPage = 2, lastPage = 5, totalItems = 0, perPage = 20)

        assertEquals(2, state.completeLoad(page, 2).lastPage)
    }

    @Test
    fun successfulEmptyAllPageOffersRefreshButSearchMissDoesNot() {
        val state = HadithListState()
            .beginLoad(1)
            .completeLoad(HadithPage(emptyList(), 1, 1, 0, 20), 1)

        assertTrue(state.shouldOfferEmptyRetry("   "))
        assertFalse(state.shouldOfferEmptyRetry("sabır"))
    }

    @Test
    fun firstPageRetryClearsStaleItemsAndLoadingIsObservable() {
        val state = HadithListState(
            items = listOf(HadithSummary("1", "Eski")),
            isLoading = false,
            loadedPage = 2,
            lastPage = 2,
        ).beginLoad(1)

        assertTrue(state.isLoading)
        assertFalse(state.isLoadingMore)
        assertTrue(state.items.isEmpty())
        assertEquals(1, state.failedPage)
    }
}
