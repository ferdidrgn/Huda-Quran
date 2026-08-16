package org.ferdidrgn.hudaquran.ui.surahdetail

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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.ui.components.AyahCard
import org.ferdidrgn.hudaquran.ui.components.MiniPlayerBar

@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    scrollToAyah: Int?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val audioPlayer = AppContainer.audioPlayer
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var detail by remember(surahNumber) { mutableStateOf<SurahDetail?>(null) }
    var isLoading by remember(surahNumber) { mutableStateOf(true) }
    var loadError by remember(surahNumber) { mutableStateOf(false) }

    var playingAyahIndex by remember(surahNumber) { mutableStateOf<Int?>(null) }
    var playingWholeSurah by remember(surahNumber) { mutableStateOf(false) }
    val playerState by audioPlayer.state.collectAsState()
    val favorites by preferences.favorites.collectAsState()

    LaunchedEffect(surahNumber) {
        isLoading = true
        loadError = false
        runCatching {
            repository.getSurahDetail(surahNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { loaded ->
            detail = loaded
            preferences.saveLastRead(loaded.surah.number, scrollToAyah ?: 1, loaded.surah.englishName)
            if (scrollToAyah != null) {
                val index = loaded.ayahs.indexOfFirst { it.numberInSurah == scrollToAyah }
                if (index >= 0) scope.launch { listState.scrollToItem(index) }
            }
        }.onFailure { loadError = true }
        isLoading = false
    }

    LaunchedEffect(playerState.status) {
        if (playerState.status == PlaybackStatus.COMPLETED) {
            val ayahs = detail?.ayahs.orEmpty()
            val currentIndex = playingAyahIndex
            when {
                currentIndex != null -> {
                    val next = currentIndex + 1
                    if (next < ayahs.size) {
                        playingAyahIndex = next
                        audioPlayer.play(ayahs[next].audioUrl)
                        preferences.saveLastRead(surahNumber, ayahs[next].numberInSurah, detail?.surah?.englishName.orEmpty())
                    } else {
                        playingAyahIndex = null
                    }
                }
                playingWholeSurah -> playingWholeSurah = false
            }
        }
    }

    fun playAyah(index: Int) {
        val ayahs = detail?.ayahs ?: return
        playingWholeSurah = false
        playingAyahIndex = index
        audioPlayer.play(ayahs[index].audioUrl)
        preferences.saveLastRead(surahNumber, ayahs[index].numberInSurah, detail?.surah?.englishName.orEmpty())
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

    fun toggleWholeSurah() {
        val current = detail ?: return
        if (playingWholeSurah) {
            when (playerState.status) {
                PlaybackStatus.PLAYING -> audioPlayer.pause()
                PlaybackStatus.PAUSED -> audioPlayer.resume()
                else -> {
                    playingAyahIndex = null
                    audioPlayer.play(current.surahAudioUrl)
                }
            }
        } else {
            playingAyahIndex = null
            playingWholeSurah = true
            audioPlayer.play(current.surahAudioUrl)
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
        playingAyahIndex = null
        playingWholeSurah = false
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    detail?.surah?.englishName ?: "Sure",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                detail?.surah?.let {
                    Text(
                        "${it.numberOfAyahs} ayet • ${if (it.revelationType == "Meccan") "Mekki" else "Medeni"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
            if (detail != null) {
                val isThisPlaying = playingWholeSurah && playerState.status == PlaybackStatus.PLAYING
                IconButton(onClick = { toggleWholeSurah() }) {
                    Text(if (isThisPlaying) "⏸️" else "▶️", fontSize = 20.sp)
                }
            }
        }
        HorizontalDivider()

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError || detail == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sure yüklenemedi. İnternet bağlantınızı kontrol edin.", color = MaterialTheme.colorScheme.error)
            }
            else -> {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(detail!!.ayahs.size) { index ->
                            AyahCard(
                                ayah = detail!!.ayahs[index],
                                isPlaying = playingAyahIndex == index && playerState.status == PlaybackStatus.PLAYING,
                                isLoading = playingAyahIndex == index && playerState.status == PlaybackStatus.LOADING,
                                isFavorite = "$surahNumber:${detail!!.ayahs[index].numberInSurah}" in favorites,
                                onPlayToggle = { toggleAyah(index) },
                                onFavoriteToggle = {
                                    preferences.toggleFavorite(surahNumber, detail!!.ayahs[index].numberInSurah)
                                },
                            )
                        }
                    }

                    if (playerState.status != PlaybackStatus.IDLE && (playingAyahIndex != null || playingWholeSurah)) {
                        MiniPlayerBar(
                            label = when {
                                playingWholeSurah -> "${detail!!.surah.englishName} • Tüm sure"
                                playingAyahIndex != null -> "Ayet ${detail!!.ayahs[playingAyahIndex!!].numberInSurah}"
                                else -> ""
                            },
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
}

