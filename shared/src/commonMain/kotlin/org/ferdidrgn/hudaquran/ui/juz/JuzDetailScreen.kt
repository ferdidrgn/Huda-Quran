package org.ferdidrgn.hudaquran.ui.juz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.JuzDetail
import org.ferdidrgn.hudaquran.ui.components.AyahCard

@Composable
fun JuzDetailScreen(juzNumber: Int, modifier: Modifier = Modifier, onBack: () -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager

    var detail by remember(juzNumber) { mutableStateOf<JuzDetail?>(null) }
    var isLoading by remember(juzNumber) { mutableStateOf(true) }
    var loadError by remember(juzNumber) { mutableStateOf(false) }
    var reloadKey by remember(juzNumber) { mutableStateOf(0) }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val favorites by preferences.favorites.collectAsState()

    val currentAyah = if (nowPlaying?.mode == PlaybackMode.AYAH_QUEUE) {
        nowPlaying?.queue?.getOrNull(nowPlaying!!.currentIndex)
    } else null

    LaunchedEffect(juzNumber, reloadKey) {
        isLoading = true
        loadError = false
        runCatching {
            repository.getJuzDetail(juzNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { detail = it }.onFailure { loadError = true }
        isLoading = false
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Text("←", fontSize = 24.sp) }
            Text("Cüz $juzNumber", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError || detail == null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Cüz yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { reloadKey++ }) { Text("Tekrar Dene") }
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(detail!!.ayahs.size) { index ->
                    val ayah = detail!!.ayahs[index]
                    AyahCard(
                        ayah = ayah,
                        isPlaying = currentAyah?.surahNumber == ayah.surahNumber &&
                            currentAyah.numberInSurah == ayah.numberInSurah &&
                            playerState.status == PlaybackStatus.PLAYING,
                        isLoading = currentAyah?.surahNumber == ayah.surahNumber &&
                            currentAyah.numberInSurah == ayah.numberInSurah &&
                            playerState.status == PlaybackStatus.LOADING,
                        isFavorite = "${ayah.surahNumber}:${ayah.numberInSurah}" in favorites,
                        onPlayToggle = {
                            playback.toggleAyahInQueue(detail!!.ayahs, index, ayah.surahNumber, ayah.surahName, preferences.selectedReciter)
                        },
                        onFavoriteToggle = { preferences.toggleFavorite(ayah.surahNumber, ayah.numberInSurah) },
                        showSurahLabel = true,
                    )
                }
            }
        }
    }
}
