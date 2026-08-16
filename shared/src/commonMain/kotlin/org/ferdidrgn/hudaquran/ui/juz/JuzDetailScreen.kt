package org.ferdidrgn.hudaquran.ui.juz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.JuzDetail
import org.ferdidrgn.hudaquran.ui.components.AyahCard
import org.ferdidrgn.hudaquran.ui.components.MiniPlayerBar

@Composable
fun JuzDetailScreen(juzNumber: Int, modifier: Modifier = Modifier, onBack: () -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val audioPlayer = AppContainer.audioPlayer

    var detail by remember(juzNumber) { mutableStateOf<JuzDetail?>(null) }
    var isLoading by remember(juzNumber) { mutableStateOf(true) }
    var loadError by remember(juzNumber) { mutableStateOf(false) }
    var playingAyahIndex by remember(juzNumber) { mutableStateOf<Int?>(null) }
    val playerState by audioPlayer.state.collectAsState()
    val favorites by preferences.favorites.collectAsState()

    LaunchedEffect(juzNumber) {
        isLoading = true
        loadError = false
        runCatching {
            repository.getJuzDetail(juzNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { detail = it }.onFailure { loadError = true }
        isLoading = false
    }

    LaunchedEffect(playerState.status) {
        if (playerState.status == PlaybackStatus.COMPLETED) {
            val ayahs = detail?.ayahs.orEmpty()
            val currentIndex = playingAyahIndex
            if (currentIndex != null) {
                val next = currentIndex + 1
                if (next < ayahs.size) {
                    playingAyahIndex = next
                    audioPlayer.play(ayahs[next].audioUrl)
                    preferences.saveLastRead(ayahs[next].surahNumber, ayahs[next].numberInSurah, ayahs[next].surahName)
                } else {
                    playingAyahIndex = null
                }
            }
        }
    }

    fun playAyah(index: Int) {
        val ayahs = detail?.ayahs ?: return
        playingAyahIndex = index
        audioPlayer.play(ayahs[index].audioUrl)
        preferences.saveLastRead(ayahs[index].surahNumber, ayahs[index].numberInSurah, ayahs[index].surahName)
    }

    fun toggleAyah(index: Int) {
        if (playingAyahIndex == index) {
            when (playerState.status) {
                PlaybackStatus.PLAYING -> audioPlayer.pause()
                PlaybackStatus.PAUSED -> audioPlayer.resume()
                else -> playAyah(index)
            }
        } else {
            playAyah(index)
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
        playingAyahIndex = null
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
            Text("Cüz $juzNumber", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError || detail == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cüz yüklenemedi. İnternet bağlantınızı kontrol edin.", color = MaterialTheme.colorScheme.error)
            }
            else -> Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(detail!!.ayahs.size) { index ->
                        val ayah = detail!!.ayahs[index]
                        AyahCard(
                            ayah = ayah,
                            isPlaying = playingAyahIndex == index && playerState.status == PlaybackStatus.PLAYING,
                            isLoading = playingAyahIndex == index && playerState.status == PlaybackStatus.LOADING,
                            isFavorite = "${ayah.surahNumber}:${ayah.numberInSurah}" in favorites,
                            onPlayToggle = { toggleAyah(index) },
                            onFavoriteToggle = { preferences.toggleFavorite(ayah.surahNumber, ayah.numberInSurah) },
                            showSurahLabel = true,
                        )
                    }
                }

                if (playerState.status != PlaybackStatus.IDLE && playingAyahIndex != null) {
                    MiniPlayerBar(
                        label = "${detail!!.ayahs[playingAyahIndex!!].surahName} • Ayet ${detail!!.ayahs[playingAyahIndex!!].numberInSurah}",
                        isPlaying = playerState.status == PlaybackStatus.PLAYING,
                        progress = if (playerState.durationMs > 0) {
                            (playerState.positionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f,
                        onToggle = {
                            when (playerState.status) {
                                PlaybackStatus.PLAYING -> audioPlayer.pause()
                                PlaybackStatus.PAUSED -> audioPlayer.resume()
                                else -> {}
                            }
                        },
                        onStop = { stopPlayback() },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}
