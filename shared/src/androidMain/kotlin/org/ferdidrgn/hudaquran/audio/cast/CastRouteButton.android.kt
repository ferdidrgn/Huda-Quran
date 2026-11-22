package org.ferdidrgn.hudaquran.audio.cast

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

@Composable
actual fun CastRouteButton(
    streamUrl: String?,
    title: String,
    artist: String,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            CastPlaybackController.initialize(viewContext)
            MediaRouteButton(viewContext).also { button ->
                CastButtonFactory.setUpMediaRouteButton(viewContext, button)
            }
        },
        update = { button ->
            button.visibility = if (CastPlaybackController.canCastUrl(streamUrl)) View.VISIBLE else View.GONE
            CastPlaybackController.updatePendingMedia(
                streamUrl = streamUrl,
                title = title,
                artist = artist,
                positionMs = positionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
            )
        },
    )
}
