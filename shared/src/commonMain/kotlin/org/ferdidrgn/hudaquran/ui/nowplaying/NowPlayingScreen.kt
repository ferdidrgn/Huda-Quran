package org.ferdidrgn.hudaquran.ui.nowplaying

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

private val speedOptions = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)

@Composable
fun NowPlayingScreen(modifier: Modifier = Modifier, onClose: () -> Unit) {
    val playback = AppContainer.playbackManager
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val repeatOne by playback.repeatOne.collectAsState()
    val speed by playback.speed.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current

    val current = nowPlaying
    LaunchedEffect(current) {
        if (current == null) onClose()
    }
    if (current == null) return

    var reciterName by remember(current.reciterId) { mutableStateOf(current.reciterId) }
    LaunchedEffect(current.reciterId) {
        val reciter = runCatching { repository.getReciters() }.getOrNull()
            ?.firstOrNull { it.identifier == current.reciterId }
        if (reciter != null) reciterName = reciter.displayName
    }

    val ayah = if (current.mode == PlaybackMode.AYAH_QUEUE) current.queue.getOrNull(current.currentIndex) else null
    val title = ayah?.let { "${localizedSurahName(it.surahNumber, it.surahName, appLanguage)} • ${strings.ayahWord} ${it.numberInSurah}" }
        ?: "${localizedSurahName(current.surahNumber, current.surahName, appLanguage)} • ${strings.wholeSurahSuffix}"
    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val hasNext = current.mode == PlaybackMode.AYAH_QUEUE && current.currentIndex + 1 < current.queue.size
    val hasPrevious = current.mode == PlaybackMode.AYAH_QUEUE && current.currentIndex > 0

    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(26.dp))
            }
            Text(strings.nowPlayingTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (repeatOne) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { playback.toggleRepeatOne() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Repeat,
                    contentDescription = null,
                    tint = if (repeatOne) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        val transition = rememberInfiniteTransition()
        val pulse by transition.animateFloat(
            initialValue = 1f,
            targetValue = if (isPlaying) 1.04f else 1f,
            animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(28.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text("🎧", fontSize = 64.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            reciterName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        val durationMs = playerState.durationMs.coerceAtLeast(1L)
        val positionFraction = if (isDragging) dragPosition else (playerState.positionMs.toFloat() / durationMs).coerceIn(0f, 1f)

        Slider(
            value = positionFraction,
            onValueChange = {
                isDragging = true
                dragPosition = it
            },
            onValueChangeFinished = {
                playback.seekTo((dragPosition * durationMs).toLong())
                isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMillis(if (isDragging) (dragPosition * durationMs).toLong() else playerState.positionMs), style = MaterialTheme.typography.bodyMedium)
            Text(formatMillis(playerState.durationMs), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransportButton(size = 48.dp, enabled = hasPrevious, onClick = { playback.skipPrevious() }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = null, modifier = Modifier.size(24.dp))
            }
            TransportButton(
                size = 72.dp,
                filled = true,
                isLoading = playerState.status == PlaybackStatus.LOADING,
                onClick = { playback.togglePlayPause() },
            ) {
                if (isPlaying) {
                    Icon(Icons.Filled.Pause, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(34.dp))
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(34.dp))
                }
            }
            TransportButton(size = 48.dp, enabled = hasNext, onClick = { playback.skipNext() }) {
                Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        GlassSurface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Text(strings.playbackSpeedTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                speedOptions.forEach { option ->
                    val selected = speed == option
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { playback.setSpeed(option) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "${option}x".replace(".0x", "x"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransportButton(
    size: Dp,
    enabled: Boolean = true,
    filled: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                when {
                    filled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }.let { if (!enabled) it.copy(alpha = 0.35f) else it },
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size * 0.4f),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            icon()
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
