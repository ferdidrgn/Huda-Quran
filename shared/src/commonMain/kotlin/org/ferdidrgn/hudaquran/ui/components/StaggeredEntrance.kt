package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/** flutter_animate-style staggered fade + slide-up entrance for grid/list items. */
@Composable
fun StaggeredEntrance(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var started by remember(index) { mutableStateOf(false) }
    LaunchedEffect(index) {
        delay(70L * index.coerceAtMost(10))
        started = true
    }
    val alpha by animateFloatAsState(if (started) 1f else 0f, animationSpec = tween(380))
    val translation by animateFloatAsState(if (started) 0f else 28f, animationSpec = tween(380))

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationY = translation
        },
    ) {
        content()
    }
}
