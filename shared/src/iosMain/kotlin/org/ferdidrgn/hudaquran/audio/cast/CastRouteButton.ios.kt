package org.ferdidrgn.hudaquran.audio.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CastRouteButton(
    streamUrl: String?,
    title: String,
    artist: String,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    modifier: Modifier,
) = Unit
