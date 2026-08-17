package org.ferdidrgn.hudaquran.ui.favorites

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.components.PlayToggleButton

private data class FavoriteEntry(val surahNumber: Int, val numberInSurah: Int)

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier, onOpenSurah: (Int, Int) -> Unit) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val playback = AppContainer.playbackManager

    val favorites by preferences.favorites.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val currentPlayingAyah = nowPlaying?.queue?.getOrNull(nowPlaying?.currentIndex ?: -1)
    val entries = remember(favorites) {
        favorites.mapNotNull { key ->
            val parts = key.split(":")
            val surah = parts.getOrNull(0)?.toIntOrNull()
            val ayah = parts.getOrNull(1)?.toIntOrNull()
            if (surah != null && ayah != null) FavoriteEntry(surah, ayah) else null
        }.sortedWith(compareBy({ it.surahNumber }, { it.numberInSurah }))
    }
    val distinctSurahs = remember(entries) { entries.map { it.surahNumber }.distinct() }

    var detailsCache by remember { mutableStateOf<Map<Int, SurahDetail>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(distinctSurahs) {
        val missing = distinctSurahs.filter { it !in detailsCache }
        if (missing.isNotEmpty()) {
            isLoading = true
            val loaded = missing.mapNotNull { number ->
                runCatching {
                    repository.getSurahDetail(number, preferences.selectedTranslation, preferences.selectedReciter)
                }.getOrNull()
            }
            detailsCache = detailsCache + loaded.associateBy { it.surah.number }
            isLoading = false
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("Favorilerim", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        when {
            entries.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Henüz favori ayetiniz yok. Sureleri okurken ⭐ ikonuna dokunarak buraya ekleyebilirsiniz.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            isLoading && detailsCache.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(entries, key = { "${it.surahNumber}:${it.numberInSurah}" }) { entry ->
                    val surahDetail = detailsCache[entry.surahNumber]
                    val ayah = surahDetail?.ayahs?.firstOrNull { it.numberInSurah == entry.numberInSurah }
                    Card(
                        onClick = { onOpenSurah(entry.surahNumber, entry.numberInSurah) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                val displayName = surahDetail?.surah?.let { localizedSurahName(it.number, it.englishName, appLanguage) }
                                    ?: "Sure ${entry.surahNumber}"
                                Text(
                                    "$displayName • Ayet ${entry.numberInSurah}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                if (ayah != null && surahDetail != null) {
                                    val isThisPlaying = currentPlayingAyah?.surahNumber == ayah.surahNumber &&
                                        currentPlayingAyah.numberInSurah == ayah.numberInSurah
                                    PlayToggleButton(
                                        isPlaying = isThisPlaying && playerState.status == PlaybackStatus.PLAYING,
                                        isLoading = isThisPlaying && playerState.status == PlaybackStatus.LOADING,
                                        onClick = {
                                            val index = surahDetail.ayahs.indexOfFirst { it.numberInSurah == ayah.numberInSurah }
                                            if (index >= 0) {
                                                playback.toggleAyahInQueue(
                                                    surahDetail.ayahs,
                                                    index,
                                                    entry.surahNumber,
                                                    surahDetail.surah.englishName,
                                                    preferences.selectedReciter,
                                                )
                                            }
                                        },
                                    )
                                }
                                IconButton(onClick = { preferences.toggleFavorite(entry.surahNumber, entry.numberInSurah) }) {
                                    Text("⭐", fontSize = 16.sp)
                                }
                            }
                            if (ayah != null) {
                                Text(
                                    ayah.arabicText,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.End,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                )
                                if (ayah.translationText.isNotBlank()) {
                                    Text(
                                        ayah.translationText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    )
                                }
                            } else {
                                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
