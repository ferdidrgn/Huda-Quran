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
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.ui.components.AyahCard

@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    scrollToAyah: Int?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var detail by remember(surahNumber) { mutableStateOf<SurahDetail?>(null) }
    var isLoading by remember(surahNumber) { mutableStateOf(true) }
    var loadError by remember(surahNumber) { mutableStateOf(false) }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val favorites by preferences.favorites.collectAsState()

    val currentAyah = if (nowPlaying?.mode == PlaybackMode.AYAH_QUEUE) {
        nowPlaying?.queue?.getOrNull(nowPlaying!!.currentIndex)
    } else null
    val isWholeSurahPlaying = nowPlaying?.mode == PlaybackMode.WHOLE_SURAH && nowPlaying?.surahNumber == surahNumber

    LaunchedEffect(surahNumber) {
        isLoading = true
        loadError = false
        runCatching {
            repository.getSurahDetail(surahNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { loaded ->
            detail = loaded
            val resumeAyah = scrollToAyah
                ?: currentAyah?.takeIf { it.surahNumber == surahNumber }?.numberInSurah
            if (resumeAyah != null) {
                val index = loaded.ayahs.indexOfFirst { it.numberInSurah == resumeAyah }
                if (index >= 0) scope.launch { listState.scrollToItem(index) }
            }
            if (scrollToAyah != null) {
                preferences.saveLastRead(loaded.surah.number, scrollToAyah, loaded.surah.englishName)
            }
        }.onFailure { loadError = true }
        isLoading = false
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
                val isThisPlaying = isWholeSurahPlaying && playerState.status == PlaybackStatus.PLAYING
                IconButton(onClick = {
                    val current = detail ?: return@IconButton
                    playback.toggleWholeSurah(surahNumber, current.surah.englishName, current.surahAudioUrl, preferences.selectedReciter)
                }) {
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
            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(detail!!.ayahs.size) { index ->
                    val ayah = detail!!.ayahs[index]
                    AyahCard(
                        ayah = ayah,
                        isPlaying = currentAyah?.surahNumber == surahNumber &&
                            currentAyah.numberInSurah == ayah.numberInSurah &&
                            playerState.status == PlaybackStatus.PLAYING,
                        isLoading = currentAyah?.surahNumber == surahNumber &&
                            currentAyah.numberInSurah == ayah.numberInSurah &&
                            playerState.status == PlaybackStatus.LOADING,
                        isFavorite = "$surahNumber:${ayah.numberInSurah}" in favorites,
                        onPlayToggle = {
                            playback.toggleAyahInQueue(detail!!.ayahs, index, surahNumber, detail!!.surah.englishName, preferences.selectedReciter)
                        },
                        onFavoriteToggle = { preferences.toggleFavorite(surahNumber, ayah.numberInSurah) },
                    )
                }
            }
        }
    }
}
