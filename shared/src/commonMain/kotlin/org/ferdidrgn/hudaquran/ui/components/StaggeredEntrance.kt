package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders [content] immediately. Used to be a staggered fade/slide-in per grid item, but that
 * replayed every time a Lazy grid disposed and recomposed an off-screen item, reading as the UI
 * loading late while scrolling — removed rather than tuned, per explicit request to drop
 * animations app-wide. Kept as a passthrough so none of its call sites need to change.
 */
@Composable
fun StaggeredEntrance(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    content()
}
