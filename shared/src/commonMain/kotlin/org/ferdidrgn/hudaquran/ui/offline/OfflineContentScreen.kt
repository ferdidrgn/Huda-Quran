package org.ferdidrgn.hudaquran.ui.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.data.local.OfflineDownloadStatus
import org.ferdidrgn.hudaquran.data.local.VaktelyaCatalog
import org.ferdidrgn.hudaquran.data.local.VaktelyaRecording
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

/**
 * Text-first offline package screen shared by Android and web.
 *
 * Quran audio downloads remain provider-backed, while verified Vaktelya assets are shipped local
 * recordings and can be previewed without network access.
 */
@Composable
fun OfflineContentScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val repository = AppContainer.offlineContent
    val scope = rememberCoroutineScope()
    var manifest by remember { mutableStateOf(repository.currentManifest()) }
    var currentItem by remember { mutableStateOf<String?>(null) }
    var running by remember { mutableStateOf(false) }
    var unexpectedError by remember { mutableStateOf<String?>(null) }
    var verifiedRecordings by remember { mutableStateOf<List<VaktelyaRecording>>(emptyList()) }
    val playerState by AppContainer.audioPlayer.state.collectAsState()
    val selection = remember { repository.selectedResources() }

    LaunchedEffect(Unit) {
        verifiedRecordings = VaktelyaCatalog.verifiedRecordings()
    }

    fun startDownload() {
        if (running) return
        running = true
        unexpectedError = null
        scope.launch {
            try {
                manifest = repository.download(selection) { progress ->
                    currentItem = progress.currentLabel
                    manifest = repository.currentManifest()
                }
                currentItem = null
            } catch (error: Throwable) {
                unexpectedError = error.message ?: strings.offlineErrorHint
                manifest = repository.currentManifest()
            } finally {
                running = false
            }
        }
    }

    fun deleteDownload() {
        if (running) return
        repository.delete(selection)
        manifest = null
        currentItem = null
        unexpectedError = null
    }

    val status = manifest?.status ?: OfflineDownloadStatus.IDLE
    val fraction = manifest?.progress ?: 0f
    val statusLabel = when (status) {
        OfflineDownloadStatus.DOWNLOADING -> strings.offlineDownloadingLabel
        OfflineDownloadStatus.PAUSED -> strings.offlinePausedLabel
        OfflineDownloadStatus.COMPLETE -> strings.offlineCompleteLabel
        OfflineDownloadStatus.PARTIAL -> strings.offlinePartialLabel
        OfflineDownloadStatus.FAILED -> strings.offlinePartialLabel
        OfflineDownloadStatus.DELETED, OfflineDownloadStatus.IDLE -> strings.offlineIdleLabel
    }

    Column(
        modifier = modifier.fillMaxSize().screenBackground().verticalScroll(rememberScrollState()),
    ) {
        PageHeader(title = strings.offlineContentTitle, onBack = onBack)
        Column(
            modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = true) {
                Text(strings.offlineContentSubtitle, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))
                Text(
                    strings.offlineSelectionTemplate
                        .replace("{translation}", selection.translationEdition.ifBlank { "Seçilmedi" })
                        .replace("{tafsir}", selection.tafsirEdition.ifBlank { "Seçilmedi" }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
                Spacer(Modifier.height(12.dp))
                Text(statusLabel, style = MaterialTheme.typography.titleMedium)
                Text(
                    strings.offlineProgressTemplate
                        .replace("{done}", (manifest?.completedItems ?: 0).toString())
                        .replace("{total}", (manifest?.totalItems ?: 0).toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { fraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = fraction.coerceIn(0f, 1f),
                            range = 0f..1f,
                            steps = 0,
                        )
                        contentDescription = statusLabel
                    },
                )
                currentItem?.let { item ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        strings.offlineCurrentItemTemplate.replace("{item}", item),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when {
                        running -> OutlinedButton(
                            onClick = { repository.pause() },
                            modifier = Modifier.weight(1f),
                        ) { Text(strings.offlinePauseAction) }
                        status == OfflineDownloadStatus.PAUSED -> Button(
                            onClick = ::startDownload,
                            modifier = Modifier.weight(1f),
                        ) { Text(strings.offlineResumeAction) }
                        status == OfflineDownloadStatus.PARTIAL || status == OfflineDownloadStatus.FAILED -> Button(
                            onClick = ::startDownload,
                            modifier = Modifier.weight(1f),
                        ) { Text(strings.offlineRetryAction) }
                        status == OfflineDownloadStatus.COMPLETE -> OutlinedButton(
                            onClick = ::startDownload,
                            modifier = Modifier.weight(1f),
                        ) { Text(strings.offlineRetryAction) }
                        else -> Button(
                            onClick = ::startDownload,
                            modifier = Modifier.weight(1f),
                        ) { Text(strings.offlineDownloadAction) }
                    }
                    if (manifest != null && !running) {
                        OutlinedButton(onClick = ::deleteDownload) { Text(strings.offlineDeleteAction) }
                    }
                }
            }

            if ((manifest?.failedItems?.isNotEmpty() == true) || unexpectedError != null) {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        manifest?.failedItems?.size?.let {
                            strings.offlineFailedItemsTemplate.replace("{n}", it.toString())
                        } ?: strings.offlineErrorHint,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        unexpectedError ?: manifest?.lastError ?: strings.offlineErrorHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Text(strings.offlineDownloadedLabel, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${manifest?.completedSurahs?.size ?: 0} sure • ${manifest?.completedTafsirAyahs?.size ?: 0} tefsir ayeti",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    strings.offlineAudioCatalogued,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }

            if (verifiedRecordings.isNotEmpty()) {
                GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = true) {
                    Text("Vaktelya doğrulanmış yerel kayıtları", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Bu kayıtlar APK içinde bulunduğu için ağ olmadan önizlenebilir. Bu liste tam kıraatçı kataloğu değildir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    )
                    verifiedRecordings.take(8).forEach { recording ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recording.title.ifBlank { "Ayet ${recording.ayah}" }, style = MaterialTheme.typography.bodyMedium)
                                Text(recording.reader, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(onClick = {
                                val uri = VaktelyaCatalog.localUri(recording)
                                if (playerState.currentUrl == uri && playerState.status == org.ferdidrgn.hudaquran.audio.PlaybackStatus.PLAYING) AppContainer.audioPlayer.pause()
                                else AppContainer.audioPlayer.play(uri)
                            }) {
                                Text(if (playerState.currentUrl == VaktelyaCatalog.localUri(recording) && playerState.status == org.ferdidrgn.hudaquran.audio.PlaybackStatus.PLAYING) "Duraklat" else "Dinle")
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
