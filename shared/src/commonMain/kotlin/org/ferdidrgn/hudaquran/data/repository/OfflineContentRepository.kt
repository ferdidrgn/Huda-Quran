package org.ferdidrgn.hudaquran.data.repository

import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.data.local.AppPreferences
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadItemKind
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadManifest
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadProgress
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadSelection
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadStatus
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadStore
import org.ferdidrgn.hudaquran.data.local.OfflineStorageSummary
import org.ferdidrgn.hudaquran.data.local.QuranCache

/**
 * Resumable, text-first offline package coordinator.
 *
 * It deliberately downloads through QuranRepository, which means every successful item is
 * written through the existing QuranCache and can be served when the next request is offline.
 * Audio is only catalogued here: binary audio storage is platform-specific and is not falsely
 * marked complete until a platform storage implementation is supplied.
 */
class OfflineContentRepository(
    private val quranRepository: QuranRepository = QuranRepository(),
    private val cache: QuranCache = QuranCache(),
    private val preferences: AppPreferences = AppPreferences(),
    private val store: OfflineDownloadStore = OfflineDownloadStore(),
) {
    // The coordinator is intentionally process-local: the persisted manifest is the source of
    // truth after a process death, while this flag lets the shared UI stop between network items.
    private var pauseRequested: Boolean = false

    fun currentManifest(): OfflineDownloadManifest? = store.readManifest()

    /** Requests a safe pause. The current network request is allowed to finish first. */
    fun pause() {
        pauseRequested = true
        store.readManifest()?.let { manifest ->
            if (manifest.status == OfflineDownloadStatus.DOWNLOADING) {
                store.writeManifest(
                    manifest.copy(
                        status = OfflineDownloadStatus.PAUSED,
                        updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
        }
    }

    fun isPaused(): Boolean = pauseRequested

    fun selectedResources(): OfflineDownloadSelection = OfflineDownloadSelection(
        // The offline screen is often the first place these persisted choices are read after an
        // upgrade. Treat stale or malformed preference values as unset instead of failing during
        // composition before the user can reach the screen.
        translationEdition = runCatching { preferences.selectedTranslation }
            .getOrDefault(org.ferdidrgn.hudaquran.domain.model.QuranEditions.DEFAULT_TRANSLATION),
        tafsirEdition = runCatching { preferences.selectedTafsir }.getOrDefault(""),
        reciterEdition = runCatching { preferences.selectedReciter }
            .getOrDefault(org.ferdidrgn.hudaquran.domain.model.QuranEditions.DEFAULT_RECITER),
    )

    fun storageSummary(): OfflineStorageSummary {
        val manifest = store.readManifest()
        return OfflineStorageSummary(
            cachedSurahs = manifest?.completedSurahs?.size ?: 0,
            cachedTafsirAyahs = manifest?.completedTafsirAyahs?.size ?: 0,
            selectedAudioSurahs = manifest?.audioCataloguedSurahs?.size ?: 0,
            audioBytesDownloaded = 0L,
            audioDownloadSupported = false,
        )
    }

    /**
     * Downloads missing surahs and tafsir ayahs one item at a time.
     * A thrown network error is recorded per item, then the process continues; calling this again
     * resumes only incomplete work. This keeps a transient outage from invalidating good cache.
     */
    suspend fun download(
        selection: OfflineDownloadSelection = selectedResources(),
        onProgress: (OfflineDownloadProgress) -> Unit = {},
    ): OfflineDownloadManifest {
        pauseRequested = false
        var manifest = store.readManifest()
            ?.takeIf { it.selection == selection }
            ?: OfflineDownloadManifest(selection = selection)
        manifest = manifest.copy(status = OfflineDownloadStatus.DOWNLOADING, lastError = null)
        store.writeManifest(manifest)

        val total = manifest.totalItems
        fun report(label: String) {
            onProgress(
                OfflineDownloadProgress(
                    status = manifest.status,
                    completedItems = manifest.completedItems,
                    totalItems = total,
                    currentLabel = label,
                    failedItems = manifest.failedItems.size,
                ),
            )
        }

        for (surah in 1..114) {
            if (pauseRequested) {
                manifest = manifest.copy(
                    status = OfflineDownloadStatus.PAUSED,
                    updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                )
                store.writeManifest(manifest)
                reportPaused(onProgress, manifest, total)
                return manifest
            }
            if (surah in manifest.completedSurahs) continue
            report("Sure $surah")
            runCatching {
                quranRepository.getSurahDetail(
                    surahNumber = surah,
                    translationEdition = selection.translationEdition,
                    reciterEdition = selection.reciterEdition,
                )
            }.onSuccess { detail ->
                manifest = manifest.copy(
                    completedSurahs = manifest.completedSurahs + surah,
                    audioCataloguedSurahs = if (detail.ayahs.any { it.audioUrl.isNotBlank() }) {
                        manifest.audioCataloguedSurahs + surah
                    } else {
                        manifest.audioCataloguedSurahs
                    },
                    failedItems = manifest.failedItems - itemKey(OfflineDownloadItemKind.SURAH_TEXT, surah),
                    updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                )
                store.writeManifest(manifest)
            }.onFailure { error ->
                manifest = recordFailure(manifest, OfflineDownloadItemKind.SURAH_TEXT, surah, error)
                store.writeManifest(manifest)
            }
            report("Sure $surah")
        }

        if (selection.tafsirEdition.isNotBlank()) {
            for (surah in 1..114) {
                if (pauseRequested) {
                    manifest = manifest.copy(
                        status = OfflineDownloadStatus.PAUSED,
                        updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                    )
                    store.writeManifest(manifest)
                    reportPaused(onProgress, manifest, total)
                    return manifest
                }
                val detail = runCatching {
                    quranRepository.getSurahDetail(surah, selection.translationEdition, selection.reciterEdition)
                }.getOrNull() ?: continue
                for (ayah in detail.ayahs) {
                    if (pauseRequested) {
                        manifest = manifest.copy(
                            status = OfflineDownloadStatus.PAUSED,
                            updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                        )
                        store.writeManifest(manifest)
                        reportPaused(onProgress, manifest, total)
                        return manifest
                    }
                    if (ayah.globalNumber in manifest.completedTafsirAyahs) continue
                    report("Tefsir ${ayah.globalNumber}")
                    runCatching {
                        quranRepository.getTafsirForAyah(
                            globalAyahNumber = ayah.globalNumber,
                            tafsirEdition = selection.tafsirEdition,
                            surahNumber = ayah.surahNumber,
                            numberInSurah = ayah.numberInSurah,
                        )
                    }.onSuccess { text ->
                        if (text.isNotBlank()) {
                            manifest = manifest.copy(
                                completedTafsirAyahs = manifest.completedTafsirAyahs + ayah.globalNumber,
                                failedItems = manifest.failedItems - itemKey(OfflineDownloadItemKind.TAFSIR, ayah.globalNumber),
                                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                            )
                            store.writeManifest(manifest)
                        }
                    }.onFailure { error ->
                        manifest = recordFailure(manifest, OfflineDownloadItemKind.TAFSIR, ayah.globalNumber, error)
                        store.writeManifest(manifest)
                    }
                }
            }
        }

        if (pauseRequested) {
            manifest = manifest.copy(
                status = OfflineDownloadStatus.PAUSED,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            )
            store.writeManifest(manifest)
            reportPaused(onProgress, manifest, total)
            return manifest
        }
        manifest = manifest.copy(
            status = offlineFinalStatus(manifest, pauseRequested = false),
            updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
        )
        store.writeManifest(manifest)
        report("Tamamlandı")
        return manifest
    }

    fun delete(selection: OfflineDownloadSelection = selectedResources()) {
        cache.clearOfflineSelection(selection)
        if (store.readManifest()?.selection == selection) store.clearManifest()
    }

    private fun recordFailure(
        manifest: OfflineDownloadManifest,
        kind: OfflineDownloadItemKind,
        number: Int,
        error: Throwable,
    ): OfflineDownloadManifest = manifest.copy(
        failedItems = manifest.failedItems + (itemKey(kind, number) to (error.message ?: "İndirme başarısız")),
        lastError = error.message,
        updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
    )

    private fun itemKey(kind: OfflineDownloadItemKind, number: Int) = "${kind.name}:$number"

    private fun reportPaused(
        onProgress: (OfflineDownloadProgress) -> Unit,
        manifest: OfflineDownloadManifest,
        total: Int,
    ) {
        onProgress(
            OfflineDownloadProgress(
                status = OfflineDownloadStatus.PAUSED,
                completedItems = manifest.completedItems,
                totalItems = total,
                currentLabel = "Duraklatıldı",
                failedItems = manifest.failedItems.size,
            ),
        )
    }
}

internal fun offlineFinalStatus(
    manifest: OfflineDownloadManifest,
    pauseRequested: Boolean,
): OfflineDownloadStatus = when {
    pauseRequested -> OfflineDownloadStatus.PAUSED
    manifest.failedItems.isEmpty() && manifest.completedSurahs.size == 114 -> OfflineDownloadStatus.COMPLETE
    else -> OfflineDownloadStatus.PARTIAL
}
