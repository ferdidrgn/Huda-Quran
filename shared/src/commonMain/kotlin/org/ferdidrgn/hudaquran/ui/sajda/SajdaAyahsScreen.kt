package org.ferdidrgn.hudaquran.ui.sajda

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.AyahCard
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

@Composable
fun SajdaAyahsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager

    var ayahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val favorites by preferences.favorites.collectAsState()
    val strings = LocalStrings.current

    val currentAyah = if (nowPlaying?.mode == PlaybackMode.AYAH_QUEUE) {
        nowPlaying?.queue?.getOrNull(nowPlaying!!.currentIndex)
    } else null

    LaunchedEffect(reloadKey) {
        isLoading = true
        loadError = false
        runCatching {
            repository.getSajdaAyahs(preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { ayahs = it }.onFailure { loadError = true }
        isLoading = false
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Column {
                Text(strings.sajdaTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    strings.sajdaSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        strings.sajdaLoadErrorFull,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { reloadKey++ }) { Text(strings.retry) }
                }
            }
            else -> {
                val showAds = !preferences.isAdFree()
                val midIndex = ayahs.size / 2
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    itemsIndexed(ayahs, key = { _, ayah -> "${ayah.surahNumber}:${ayah.numberInSurah}" }) { index, ayah ->
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
                                playback.toggleAyahInQueue(ayahs, index, ayah.surahNumber, ayah.surahName, preferences.selectedReciter)
                            },
                            onFavoriteToggle = { preferences.toggleFavorite(ayah.surahNumber, ayah.numberInSurah) },
                            showSurahLabel = true,
                        )
                        if (showAds && index == midIndex) AdBannerCard()
                    }
                    if (showAds) item { AdBannerCard() }
                }
            }
        }
    }
}
