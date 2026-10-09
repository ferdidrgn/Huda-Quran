package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.navigation.AppBackHandler
import org.ferdidrgn.hudaquran.ui.nowplaying.NowPlayingScreen

private val PlayerSheetSpring = spring<Float>(dampingRatio = 0.86f, stiffness = 380f)
private val CollapsedHeight = 68.dp
private val CollapsedSideMargin = 14.dp
private val SheetMaxWidth = 640.dp

/**
 * The always-on mini player, reimagined as a drag-and-tap bottom sheet: a floating pill that
 * hovers above the page (transparent margins around it, like it's resting on top of the content)
 * when collapsed, and morphs in place — no navigation, no screen swap — into the full Now Playing
 * surface when opened. Tap, swipe up, or fling it open; swipe down, tap the handle's chevron, or
 * press back to send it home again.
 */
@Composable
fun ExpandablePlayerSheet(modifier: Modifier = Modifier, collapsedBottomInset: Dp = 16.dp) {
    val playback = AppContainer.playbackManager
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val expansion = remember { Animatable(0f) }

    LaunchedEffect(nowPlaying == null) {
        if (nowPlaying == null) expansion.snapTo(0f)
    }

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
    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val isBuffering = playerState.status == PlaybackStatus.LOADING
    val progress = if (playerState.durationMs > 0) {
        (playerState.positionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    fun expand() {
        scope.launch { expansion.animateTo(1f, PlayerSheetSpring) }
    }
    fun collapse() {
        scope.launch { expansion.animateTo(0f, PlayerSheetSpring) }
    }
    fun settle(velocity: Float) {
        val target = when {
            velocity < -900f -> 1f
            velocity > 900f -> 0f
            else -> if (expansion.value > 0.5f) 1f else 0f
        }
        scope.launch { expansion.animateTo(target, PlayerSheetSpring) }
    }

    val isExpanded by remember { derivedStateOf { expansion.value > 0.5f } }
    AppBackHandler(enabled = isExpanded) { collapse() }

    BoxWithConstraints(modifier = modifier) {
        val fullHeight = maxHeight
        val dragRangePx = with(density) { (fullHeight - CollapsedHeight).toPx() }.coerceAtLeast(1f)
        val draggableState = rememberDraggableState { delta ->
            scope.launch {
                val next = (expansion.value - delta / dragRangePx).coerceIn(0f, 1f)
                expansion.snapTo(next)
            }
        }

        val t = expansion.value
        val collapseFactor = (t * 2.4f).coerceAtMost(1f)

        // A scrim behind the sheet only exists while it's partway (or fully) open, and taps it
        // closed — it costs nothing when collapsed since it's not even composed then.
        if (t > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f * t))
                    .pointerInput(Unit) { detectTapGestures { collapse() } },
            )
        }

        val sheetHeight = lerpDp(CollapsedHeight, fullHeight, t)
        val sideMargin = lerpDp(CollapsedSideMargin, 0.dp, collapseFactor)
        val bottomMargin = lerpDp(collapsedBottomInset, 0.dp, collapseFactor)
        val topRadius = lerpDp(26.dp, 30.dp, t)
        val bottomRadius = lerpDp(26.dp, 0.dp, collapseFactor)
        val elevation = lerpDp(10.dp, 0.dp, collapseFactor)
        val surfaceColor = lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background, t)
        val shape = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = SheetMaxWidth)
                .fillMaxWidth()
                .padding(start = sideMargin, end = sideMargin, bottom = bottomMargin)
                .height(sheetHeight)
                .shadow(elevation, shape, clip = false)
                .clip(shape)
                .background(surfaceColor),
        ) {
            // Collapsed pill: present while mostly closed, drag or tap to open.
            if (t < 0.999f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha((1f - t * 2.2f).coerceIn(0f, 1f))
                        .draggable(orientation = Orientation.Vertical, state = draggableState, onDragStopped = { settle(it) })
                        .clickable(onClick = ::expand),
                ) {
                    Column {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(2.5.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.Headphones, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    reciterName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            PlayerSheetRoundButton(background = MaterialTheme.colorScheme.primary, onClick = { playback.togglePlayPause() }) {
                                when {
                                    isBuffering -> CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                    isPlaying -> Icon(Icons.Filled.Pause, contentDescription = strings.cdPause, tint = MaterialTheme.colorScheme.onPrimary)
                                    else -> Icon(Icons.Filled.PlayArrow, contentDescription = strings.cdPlay, tint = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Box(modifier = Modifier.size(8.dp))
                            PlayerSheetRoundButton(background = MaterialTheme.colorScheme.surfaceVariant, onClick = { playback.stop() }) {
                                Icon(Icons.Filled.Close, contentDescription = strings.cdClose, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Full player: present once mostly open; a drag handle collapses it alongside the
            // chevron already in NowPlayingScreen's own header and the system back gesture.
            if (t > 0.001f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(((t - 0.35f) * 1.7f).coerceIn(0f, 1f))
                        .windowInsetsPadding(WindowInsets.systemBars),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .draggable(orientation = Orientation.Vertical, state = draggableState, onDragStopped = { settle(it) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
                        )
                    }
                    NowPlayingScreen(modifier = Modifier.weight(1f).fillMaxSize(), onClose = ::collapse)
                }
            }
        }
    }
}

@Composable
private fun PlayerSheetRoundButton(background: Color, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}
