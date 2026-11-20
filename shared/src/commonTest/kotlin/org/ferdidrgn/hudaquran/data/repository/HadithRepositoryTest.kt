package org.ferdidrgn.hudaquran.data.repository

import org.ferdidrgn.hudaquran.domain.model.HadithPage
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HadithRepositoryTest {
    @Test
    fun emptyFirstPageIsNotSavedAsOfflineTruth() {
        assertFalse(shouldCacheHadithPage(HadithPage(emptyList(), 1, 1, 0, 20), requestedPage = 1))
    }

    @Test
    fun nonEmptyAndLaterEmptyPagesRemainCacheable() {
        assertTrue(shouldCacheHadithPage(HadithPage(listOf(org.ferdidrgn.hudaquran.domain.model.HadithSummary("1", "Bir")), 1, 2, 1, 20), 1))
        assertTrue(shouldCacheHadithPage(HadithPage(emptyList(), 2, 2, 1, 20), 2))
    }
}
