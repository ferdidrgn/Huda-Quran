package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

@Composable
fun GlobalMiniPlayer(modifier: Modifier = Modifier, onOpenNowPlaying: () -> Unit) {
    val playback = AppContainer.playbackManager
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current

    val current = nowPlaying ?: return

    var reciterName by remember(current.reciterId) { mutableStateOf(current.reciterId) }
    LaunchedEffect(current.reciterId) {
        val reciter = runCatching { repository.getReciters() }.getOrNull()
            ?.firstOrNull { it.identifier == current.reciterId }
        if (reciter != null) reciterName = reciter.displayName
    }

    val title = if (current.mode == PlaybackMode.AYAH_QUEUE) {
        val ayah = current.queue.getOrNull(current.currentIndex)
        if (ayah != null) {
            "${localizedSurahName(ayah.surahNumber, ayah.surahName, appLanguage)} • ${strings.ayahWord} ${ayah.numberInSurah}"
        } else {
            localizedSurahName(current.surahNumber, current.surahName, appLanguage)
        }
    } else {
        "${localizedSurahName(current.surahNumber, current.surahName, appLanguage)} • ${strings.wholeSurahSuffix}"
    }

    MiniPlayerBar(
        title = title,
        subtitle = reciterName,
        isPlaying = playerState.status == PlaybackStatus.PLAYING,
        isBuffering = playerState.status == PlaybackStatus.LOADING,
        progress = if (playerState.durationMs > 0) {
            (playerState.positionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f,
        onOpen = onOpenNowPlaying,
        onToggle = { playback.togglePlayPause() },
        onStop = { playback.stop() },
        modifier = modifier,
    )
}
