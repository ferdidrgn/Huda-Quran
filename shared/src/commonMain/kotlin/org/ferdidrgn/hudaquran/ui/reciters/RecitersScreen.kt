package org.ferdidrgn.hudaquran.ui.reciters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.QuranEditions
import org.ferdidrgn.hudaquran.domain.model.Reciter

private const val PREVIEW_SURAH_NUMBER = 1
private const val PREVIEW_SURAH_NAME = "Al-Faatiha"

@Composable
fun RecitersScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager

    var reciters by remember { mutableStateOf<List<Reciter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedReciter by remember { mutableStateOf(preferences.selectedReciter) }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()

    LaunchedEffect(Unit) {
        runCatching { repository.getReciters() }
            .onSuccess { reciters = it }
            .onFailure { loadError = true }
        isLoading = false
    }

    val filtered = remember(reciters, query) {
        if (query.isBlank()) reciters else reciters.filter { it.displayName.contains(query, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Text("←", fontSize = 24.sp) }
            Column {
                Text("Hafızlar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (!isLoading) {
                    Text(
                        "${reciters.size} hafız • dinleyip seçebilirsiniz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Hafız ara...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
        )

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError && reciters.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Hafız listesi yüklenemedi. İnternet bağlantınızı kontrol edin.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.identifier }) { reciter ->
                    val isThisPreviewing = nowPlaying?.mode == PlaybackMode.WHOLE_SURAH &&
                        nowPlaying?.reciterId == reciter.identifier &&
                        nowPlaying?.surahNumber == PREVIEW_SURAH_NUMBER
                    ReciterRow(
                        reciter = reciter,
                        isSelected = selectedReciter == reciter.identifier,
                        isPreviewPlaying = isThisPreviewing && playerState.status == PlaybackStatus.PLAYING,
                        isPreviewLoading = isThisPreviewing && playerState.status == PlaybackStatus.LOADING,
                        onSelect = {
                            selectedReciter = reciter.identifier
                            preferences.selectedReciter = reciter.identifier
                        },
                        onPreviewToggle = {
                            if (isThisPreviewing) {
                                playback.togglePlayPause()
                            } else {
                                playback.playWholeSurah(
                                    PREVIEW_SURAH_NUMBER,
                                    PREVIEW_SURAH_NAME,
                                    QuranEditions.surahAudioUrl(PREVIEW_SURAH_NUMBER, reciter.identifier),
                                    reciter.identifier,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReciterRow(
    reciter: Reciter,
    isSelected: Boolean,
    isPreviewPlaying: Boolean,
    isPreviewLoading: Boolean,
    onSelect: () -> Unit,
    onPreviewToggle: () -> Unit,
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    CircleShape,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    reciter.displayName.take(1).uppercase(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            ) {
                Text(
                    reciter.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (isSelected) "Seçili hafız" else "Seçmek için dokunun",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            if (isSelected) {
                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Box(modifier = Modifier.size(8.dp))
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable(onClick = onPreviewToggle),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isPreviewLoading -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    isPreviewPlaying -> Text("⏸️", fontSize = 14.sp)
                    else -> Text("▶️", fontSize = 14.sp)
                }
            }
        }
    }
}
