package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.navigation.AppBackHandler
import org.ferdidrgn.hudaquran.ui.nowplaying.NowPlayingScreen

private val CollapsedHeight = 68.dp
private val CollapsedSideMargin = 14.dp
private val SheetMaxWidth = 640.dp

/** A lively bounce on open/close and on drag release, rather than a flat, mechanical glide. */
private val PlayerSheetMotion = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

private enum class PlayerSheetAnchor { Collapsed, Expanded }

/**
 * Mini player as an [AnchoredDraggable] bottom sheet (Compose Foundation's sheet/drawer pattern):
 * a floating pill that the finger can drag between collapsed and expanded anchors. Motion uses
 * [graphicsLayer] translation (GPU) instead of animating layout height every frame, and the heavy
 * [NowPlayingScreen] tree is only composed once the sheet has left the collapsed rest state.
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

    val sheetState = remember { AnchoredDraggableState(initialValue = PlayerSheetAnchor.Collapsed) }
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = sheetState,
        positionalThreshold = { distance -> distance * 0.28f },
        animationSpec = PlayerSheetMotion,
    )

    LaunchedEffect(nowPlaying == null) {
        if (nowPlaying == null) sheetState.snapTo(PlayerSheetAnchor.Collapsed)
    }

    // The caller mounts this inside an AnimatedVisibility that keeps it composed for the whole
    // exit transition; without this, nowPlaying going null would blank the sheet's content a
    // frame before the fade/slide-out even starts playing.
    var lastTrack by remember { mutableStateOf(nowPlaying) }
    LaunchedEffect(nowPlaying) {
        if (nowPlaying != null) lastTrack = nowPlaying
    }
    val current = lastTrack ?: return

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

    val pillInteractionSource = remember { MutableInteractionSource() }
    val pillPressed by pillInteractionSource.collectIsPressedAsState()
    val pillPressScale by animateFloatAsState(if (pillPressed) 0.97f else 1f, label = "playerPillPress")

    // A slow breathing pulse on the headphone icon while audio is actually running, so the
    // collapsed pill itself tells you something is alive instead of sitting static.
    val breathTransition = rememberInfiniteTransition(label = "playerIconBreath")
    val breath by breathTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "playerIconBreath",
    )

    fun expand() {
        scope.launch { sheetState.animateTo(PlayerSheetAnchor.Expanded, PlayerSheetMotion) }
    }
    fun collapse() {
        scope.launch { sheetState.animateTo(PlayerSheetAnchor.Collapsed, PlayerSheetMotion) }
    }

    val isExpanded by remember {
        derivedStateOf { sheetState.targetValue == PlayerSheetAnchor.Expanded || sheetState.currentValue == PlayerSheetAnchor.Expanded }
    }
    AppBackHandler(enabled = isExpanded) { collapse() }

    BoxWithConstraints(modifier = modifier) {
        val fullHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val collapsedHeightPx = with(density) { CollapsedHeight.toPx() }
        val collapsedOffsetPx = (fullHeightPx - collapsedHeightPx).coerceAtLeast(1f)

        LaunchedEffect(collapsedOffsetPx) {
            sheetState.updateAnchors(
                DraggableAnchors {
                    PlayerSheetAnchor.Expanded at 0f
                    PlayerSheetAnchor.Collapsed at collapsedOffsetPx
                },
            )
        }

        val offsetY = sheetState.offset.let { if (it.isNaN()) collapsedOffsetPx else it }
        val t = (1f - (offsetY / collapsedOffsetPx)).coerceIn(0f, 1f)
        val collapseFactor = (t * 2.2f).coerceAtMost(1f)
        val miniAlpha = (1f - t * 1.85f).coerceIn(0f, 1f)
        val fullAlpha = ((t - 0.12f) / 0.55f).coerceIn(0f, 1f)

        if (t > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.52f * t }
                    .background(Color.Black)
                    .pointerInput(Unit) { detectTapGestures { collapse() } },
            )
        }

        val sideMargin = lerpDp(CollapsedSideMargin, 0.dp, collapseFactor)
        val bottomMargin = lerpDp(collapsedBottomInset, 0.dp, collapseFactor)
        val topRadius = lerpDp(26.dp, 28.dp, t)
        val bottomRadius = lerpDp(26.dp, 0.dp, collapseFactor)
        val elevation = lerpDp(10.dp, 0.dp, collapseFactor)
        val surfaceColor = lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background, t)
        val shape = RoundedCornerShape(
            topStart = topRadius,
            topEnd = topRadius,
            bottomStart = bottomRadius,
            bottomEnd = bottomRadius,
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = SheetMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = sideMargin, end = sideMargin, bottom = bottomMargin)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .shadow(elevation, shape, clip = false)
                .clip(shape)
                .background(surfaceColor)
                .anchoredDraggable(
                    state = sheetState,
                    orientation = Orientation.Vertical,
                    flingBehavior = flingBehavior,
                ),
        ) {
            // Mini pill lives at the top of the full-height sheet so translationY reveals exactly
            // CollapsedHeight when parked on the Collapsed anchor.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CollapsedHeight)
                    .graphicsLayer {
                        alpha = miniAlpha
                        // Subtle scale so the pill feels like it dissolves into the full surface,
                        // with an extra squeeze while the finger is actually holding it down.
                        val s = (0.96f + 0.04f * miniAlpha) * pillPressScale
                        scaleX = s
                        scaleY = s
                    }
                    .then(
                        if (miniAlpha > 0.05f) {
                            Modifier.clickable(interactionSource = pillInteractionSource, indication = null, onClick = ::expand)
                        } else {
                            Modifier
                        },
                    ),
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
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            // A soft halo that breathes outward only while audio is actually
                            // playing — alpha/scale live entirely inside graphicsLayer, so the
                            // pulse never forces a recomposition, just a cheap re-draw.
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha = if (isPlaying) 0.35f * breath else 0f
                                        val haloScale = 1f + 0.5f * breath
                                        scaleX = haloScale
                                        scaleY = haloScale
                                    }
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val scale = 1f + (if (isPlaying) 0.12f * breath else 0f)
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Headphones,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
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

            // Heavy full player only after the sheet has left rest — avoids jank while collapsed.
            if (t > 0.04f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = fullAlpha }
                        .windowInsetsPadding(WindowInsets.systemBars),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clickable(onClick = ::collapse),
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "roundButtonPress")
    Box(
        modifier = Modifier
            .size(38.dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .clip(CircleShape)
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}
