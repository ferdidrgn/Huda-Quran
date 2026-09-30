package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.TimeMark

/** When the current screen was first shown; set per destination in App. Null disables entrances. */
val LocalScreenEntranceStart = staticCompositionLocalOf<TimeMark?> { null }

/** Items composed later than this after the screen opened (i.e. scrolled into view) appear instantly. */
private const val ENTRANCE_WINDOW_MS = 700L
private const val STAGGER_STEP_MS = 45L
private const val MAX_STAGGER_STEPS = 8

/**
 * One orchestrated reveal per screen: the first items fade and rise into place in sequence when a
 * screen opens. It deliberately never replays — an item that a lazy list disposes and recomposes
 * while scrolling is outside the window and renders immediately, which is what made the old
 * per-item animation read as the UI loading late.
 */
@Composable
fun StaggeredEntrance(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val start = LocalScreenEntranceStart.current
    val shouldAnimate = remember { start != null && start.elapsedNow().inWholeMilliseconds < ENTRANCE_WINDOW_MS }
    if (!shouldAnimate) {
        Box(modifier) { content() }
        return
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index.coerceIn(0, MAX_STAGGER_STEPS) * STAGGER_STEP_MS)
        progress.animateTo(1f, tween(durationMillis = 420, easing = FastOutSlowInEasing))
    }
    Box(
        modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 18.dp.toPx()
        },
    ) { content() }
}
