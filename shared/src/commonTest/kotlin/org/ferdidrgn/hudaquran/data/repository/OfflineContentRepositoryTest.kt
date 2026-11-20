package org.ferdidrgn.hudaquran.data.repository

import org.ferdidrgn.hudaquran.data.local.OfflineDownloadSelection
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadStatus
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadManifest
import kotlin.test.Test
import kotlin.test.assertEquals

class OfflineContentRepositoryTest {
    private val selection = OfflineDownloadSelection("tr", "", "ar.alafasy")

    @Test
    fun pauseWinsOverCompleteAtFinalization() {
        val manifest = OfflineDownloadManifest(
            selection = selection,
            completedSurahs = (1..114).toSet(),
        )
        assertEquals(OfflineDownloadStatus.PAUSED, offlineFinalStatus(manifest, pauseRequested = true))
    }

    @Test
    fun completeRequiresEverySurahAndNoFailures() {
        val complete = OfflineDownloadManifest(selection, completedSurahs = (1..114).toSet())
        val partial = OfflineDownloadManifest(selection, completedSurahs = setOf(1))

        assertEquals(OfflineDownloadStatus.COMPLETE, offlineFinalStatus(complete, pauseRequested = false))
        assertEquals(OfflineDownloadStatus.PARTIAL, offlineFinalStatus(partial, pauseRequested = false))
    }
}
