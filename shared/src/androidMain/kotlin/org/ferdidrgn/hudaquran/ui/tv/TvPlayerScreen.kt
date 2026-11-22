package org.ferdidrgn.hudaquran.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName

/** TV player surface; all actions use the app's existing common PlaybackManager session. */
@Composable
fun TvPlayerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playback = AppContainer.playbackManager
    val preferences = AppContainer.preferences
    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val repeatOne by playback.repeatOne.collectAsState()
    val speed by playback.speed.collectAsState()
    val language by preferences.appLanguage.collectAsState()
    val playPauseFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { playPauseFocus.requestFocus() }
    }

    val current = nowPlaying
    if (current == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("\u015Eu anda \u00E7alan bir sure yok.", style = MaterialTheme.typography.headlineMedium)
                TvFocusButton(label = "Geri d\u00F6n", onClick = onBack)
            }
        }
        return
    }

    val ayah = if (current.mode == PlaybackMode.AYAH_QUEUE) current.queue.getOrNull(current.currentIndex) else null
    val title = ayah?.let {
        "${localizedSurahName(it.surahNumber, it.surahName, language)} \u00B7 Ayet ${it.numberInSurah}"
    } ?: localizedSurahName(current.surahNumber, current.surahName, language)
    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val duration = playerState.durationMs.coerceAtLeast(0L)
    val position = playerState.positionMs.coerceIn(0L, duration.takeIf { it > 0 } ?: Long.MAX_VALUE)
    val progress = if (duration > 0L) position.toFloat() / duration.toFloat() else 0f

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 64.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TvFocusButton(label = "\u2190  Geri", onClick = onBack)
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.82f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = current.surahName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = when (playerState.status) {
                    PlaybackStatus.LOADING -> "Ses y\u00FCkleniyor\u2026"
                    PlaybackStatus.ERROR -> "Ses a\u00E7\u0131lamad\u0131. Sureyi yeniden se\u00E7in."
                    PlaybackStatus.PAUSED -> "Duraklat\u0131ld\u0131"
                    PlaybackStatus.PLAYING -> "Dinleniyor"
                    PlaybackStatus.COMPLETED -> "Tamamland\u0131"
                    PlaybackStatus.IDLE -> "Haz\u0131r"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (playerState.status == PlaybackStatus.ERROR) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatTime(position), style = MaterialTheme.typography.titleMedium)
                Text(formatTime(duration), style = MaterialTheme.typography.titleMedium)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TvFocusButton(
                    label = "\u221215 sn",
                    onClick = { playback.seekTo((position - 15_000L).coerceAtLeast(0L)) },
                    modifier = Modifier.height(72.dp),
                )
                TvFocusButton(
                    label = if (isPlaying) "Duraklat" else "Oynat",
                    onClick = { playback.togglePlayPause() },
                    modifier = Modifier.width(220.dp).height(84.dp).focusRequester(playPauseFocus),
                    prominent = true,
                    enabled = playerState.status != PlaybackStatus.LOADING && playerState.status != PlaybackStatus.ERROR,
                )
                TvFocusButton(
                    label = "+15 sn",
                    onClick = { playback.seekTo((position + 15_000L).coerceAtMost(duration.takeIf { it > 0 } ?: Long.MAX_VALUE)) },
                    modifier = Modifier.height(72.dp),
                )
                Spacer(Modifier.width(8.dp))
                TvFocusButton(
                    label = if (repeatOne) "Tekrar a\u00E7\u0131k" else "Tekrar kapal\u0131",
                    onClick = { playback.toggleRepeatOne() },
                    selected = repeatOne,
                    modifier = Modifier.height(72.dp),
                )
                TvFocusButton(
                    label = "H\u0131z ${speed}\u00D7",
                    onClick = {
                        val options = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
                        val next = options[(options.indexOf(speed).coerceAtLeast(0) + 1) % options.size]
                        playback.setSpeed(next)
                    },
                    modifier = Modifier.height(72.dp),
                )
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1_000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}
