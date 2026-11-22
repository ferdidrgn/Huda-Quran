package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Small, crash-tolerant manifest store built on the same settings backend as QuranCache. */
class OfflineDownloadStore(private val settings: Settings = createSettings()) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val MANIFEST_KEY = "offline_download_manifest_v1"
        private const val LEGACY_SELECTION_KEY = "offline_download_selection_v1"
    }

    fun readManifest(): OfflineDownloadManifest? = runCatching {
        settings.getStringOrNull(MANIFEST_KEY)
    }.getOrNull()?.let { raw ->
        runCatching { json.decodeFromString<OfflineDownloadManifest>(raw) }.getOrNull()
    }

    fun writeManifest(manifest: OfflineDownloadManifest) {
        runCatching { settings.putString(MANIFEST_KEY, json.encodeToString(manifest)) }
    }

    fun clearManifest() {
        // A stale value with the wrong stored type must not crash the offline screen when it
        // tries to clear or reopen its package. Attempt both removals independently so one
        // storage failure does not prevent cleanup of the other key.
        runCatching { settings.remove(MANIFEST_KEY) }
        runCatching { settings.remove(LEGACY_SELECTION_KEY) }
    }
}
