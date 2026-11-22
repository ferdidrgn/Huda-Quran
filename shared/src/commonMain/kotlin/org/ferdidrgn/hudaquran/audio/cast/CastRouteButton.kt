package org.ferdidrgn.hudaquran.audio.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform Cast route selector. The Android implementation hands the current remote-playable
 * media item to the Cast session; non-Android targets do not expose a Cast route button.
 */
@Composable
expect fun CastRouteButton(
    streamUrl: String?,
    title: String,
    artist: String,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
)
