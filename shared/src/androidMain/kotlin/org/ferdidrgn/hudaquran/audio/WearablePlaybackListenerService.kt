package org.ferdidrgn.hudaquran.audio

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.di.AppContainer
import org.json.JSONObject

/**
 * Phone-side bridge for the Wear OS controller.
 *
 * The watch cannot bind directly to the phone's MediaSessionCompat across devices. It sends
 * small control messages through the Wearable Data Layer; this service forwards them to the
 * existing PlaybackManager and returns the current session summary. It does not create a player.
 */
class WearablePlaybackListenerService : WearableListenerService() {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            STATE_REQUEST_PATH -> sendState(event.sourceNodeId)
            CONTROL_PATH -> handleControl(event)
            else -> super.onMessageReceived(event)
        }
    }

    private fun handleControl(event: MessageEvent) {
        val request = runCatching { JSONObject(String(event.data, Charsets.UTF_8)) }.getOrNull()
            ?: return
        val playback = AppContainer.playbackManager
        val action = request.optString("action")

        scope.launch {
            when (action) {
                "play" -> playback.resume()
                "pause" -> playback.pause()
                "previous" -> playback.skipPrevious()
                "next" -> playback.skipNext()
                "seek" -> {
                    val delta = request.optString("value").toLongOrNull() ?: 0L
                    val state = playback.playerState.value
                    val target = (state.positionMs + delta).coerceAtLeast(0L)
                    playback.seekTo(
                        if (state.durationMs > 0L) target.coerceAtMost(state.durationMs) else target,
                    )
                }
            }
            delay(250L)
            sendState(event.sourceNodeId)
        }
    }

    private fun sendState(nodeId: String) {
        val playback = AppContainer.playbackManager
        val nowPlaying = playback.nowPlaying.value
        val playerState = playback.playerState.value
        val currentAyah = nowPlaying?.queue?.getOrNull(nowPlaying.currentIndex)
        val title = if (nowPlaying == null) {
            "Şu an çalan yok"
        } else if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE && currentAyah != null) {
            nowPlaying.surahName + " • Ayet " + currentAyah.numberInSurah
        } else {
            nowPlaying.surahName
        }
        val actions = JSONObject()
            .put("hasSession", nowPlaying != null)
            .put("title", title)
            .put("reciter", nowPlaying?.reciterId.orEmpty())
            .put("isPlaying", playerState.status == PlaybackStatus.PLAYING)
            .put("positionMs", playerState.positionMs)
            .put("durationMs", playerState.durationMs)
            .put("canPrevious", nowPlaying?.currentIndex?.let { it > 0 } == true)
            .put(
                "canNext",
                nowPlaying?.let {
                    it.currentIndex + 1 < it.queue.size || it.pageFlow != null
                } == true,
            )
            .put("canSeek", playerState.durationMs > 0L)
            .toString()
            .toByteArray(Charsets.UTF_8)

        Wearable.getMessageClient(this)
            .sendMessage(nodeId, STATE_PATH, actions)
            .addOnFailureListener { error ->
                Log.w(TAG, "Could not send playback state to Wear OS node.", error)
            }
    }

    override fun onDestroy() {
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "HudaWearBridge"
        const val CONTROL_PATH = "/huda/audio/control"
        const val STATE_PATH = "/huda/audio/state"
        const val STATE_REQUEST_PATH = "/huda/audio/state-request"
    }
}
