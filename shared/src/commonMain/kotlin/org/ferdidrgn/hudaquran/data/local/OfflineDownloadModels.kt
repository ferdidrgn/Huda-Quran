package org.ferdidrgn.hudaquran.data.local

import kotlinx.serialization.Serializable

/** The three user-selected resources that define an offline Quran package. */
@Serializable
data class OfflineDownloadSelection(
    val translationEdition: String,
    val tafsirEdition: String,
    val reciterEdition: String,
)

@Serializable
enum class OfflineDownloadStatus {
    IDLE,
    DOWNLOADING,
    PAUSED,
    COMPLETE,
    PARTIAL,
    FAILED,
    DELETED,
}

@Serializable
enum class OfflineDownloadItemKind {
    SURAH_TEXT,
    TAFSIR,
    AUDIO_CATALOG,
}

@Serializable
data class OfflineDownloadManifest(
    val selection: OfflineDownloadSelection,
    val status: OfflineDownloadStatus = OfflineDownloadStatus.IDLE,
    val completedSurahs: Set<Int> = emptySet(),
    val completedTafsirAyahs: Set<Int> = emptySet(),
    /** Audio URLs are catalogued by the Quran API, but are not claimed as downloaded bytes. */
    val audioCataloguedSurahs: Set<Int> = emptySet(),
    val failedItems: Map<String, String> = emptyMap(),
    val lastError: String? = null,
    val updatedAtEpochMillis: Long = 0L,
) {
    val completedItems: Int get() = completedSurahs.size + completedTafsirAyahs.size
    val totalItems: Int get() = TOTAL_SURAHS + if (selection.tafsirEdition.isBlank()) 0 else TOTAL_AYAHS
    val progress: Float get() = (completedItems.toFloat() / totalItems).coerceIn(0f, 1f)
    val hasTextContent: Boolean get() = completedSurahs.isNotEmpty()
    val hasTafsirContent: Boolean get() = completedTafsirAyahs.isNotEmpty()
}

data class OfflineDownloadProgress(
    val status: OfflineDownloadStatus,
    val completedItems: Int,
    val totalItems: Int,
    val currentLabel: String,
    val failedItems: Int,
    val audioBytesDownloaded: Long = 0L,
    val audioDownloadSupported: Boolean = false,
) {
    val fraction: Float get() = (completedItems.toFloat() / totalItems.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class OfflineStorageSummary(
    val cachedSurahs: Int,
    val cachedTafsirAyahs: Int,
    val selectedAudioSurahs: Int,
    val audioBytesDownloaded: Long,
    val audioDownloadSupported: Boolean,
)

internal const val TOTAL_SURAHS = 114
internal const val TOTAL_AYAHS = 6236
