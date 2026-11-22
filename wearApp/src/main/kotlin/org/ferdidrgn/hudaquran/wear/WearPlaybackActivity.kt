package org.ferdidrgn.hudaquran.wear

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.foundation.requestFocusOnHierarchyActive
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

private const val CONTROL_PATH = "/huda/audio/control"
private const val STATE_PATH = "/huda/audio/state"
private const val STATE_REQUEST_PATH = "/huda/audio/state-request"
private const val SEEK_STEP_MS = 15_000L
private const val STATE_REFRESH_MS = 5_000L

private data class WatchPlaybackState(
    val connected: Boolean = false,
    val hasSession: Boolean = false,
    val hasResponse: Boolean = false,
    val message: String = "",
    val title: String = "Kur'an dinleme",
    val reciter: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val canPrevious: Boolean = false,
    val canNext: Boolean = false,
    val canSeek: Boolean = false,
)

class WearPlaybackActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {
    private val handler = Handler(Looper.getMainLooper())
    private var uiState by mutableStateOf(WatchPlaybackState())
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    private val refreshState = object : Runnable {
        override fun run() {
            requestPhoneState()
            handler.postDelayed(this, STATE_REFRESH_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WearPlayerScreen(
                    state = uiState,
                    onPlayPause = { sendControl(if (uiState.isPlaying) "pause" else "play") },
                    onPrevious = { sendControl("previous") },
                    onNext = { sendControl("next") },
                    onSeek = { delta -> sendControl("seek", delta.toString()) },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        messageClient.addListener(this)
        requestPhoneState()
        handler.postDelayed(refreshState, STATE_REFRESH_MS)
    }

    override fun onStop() {
        handler.removeCallbacks(refreshState)
        messageClient.removeListener(this)
        super.onStop()
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != STATE_PATH) return
        val json = runCatching { JSONObject(String(event.data, Charsets.UTF_8)) }.getOrNull() ?: return
        handler.post {
            val hasSession = json.optBoolean("hasSession")
            uiState = WatchPlaybackState(
                connected = true,
                hasSession = hasSession,
                hasResponse = true,
                message = if (hasSession) "" else getString(R.string.wear_no_active_playback),
                title = if (hasSession) {
                    json.optString("title", getString(R.string.wear_player_title))
                } else {
                    getString(R.string.wear_player_title)
                },
                reciter = json.optString("reciter", ""),
                isPlaying = json.optBoolean("isPlaying"),
                positionMs = json.optLong("positionMs").coerceAtLeast(0L),
                durationMs = json.optLong("durationMs").coerceAtLeast(0L),
                canPrevious = json.optBoolean("canPrevious"),
                canNext = json.optBoolean("canNext"),
                canSeek = json.optBoolean("canSeek"),
            )
        }
    }

    private fun requestPhoneState() {
        nodeClient.connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isEmpty()) {
                    uiState = uiState.copy(
                        connected = false,
                        hasSession = false,
                        hasResponse = false,
                        message = getString(R.string.wear_phone_required),
                    )
                } else {
                    uiState = uiState.copy(
                        connected = true,
                        message = if (uiState.hasResponse) uiState.message
                        else getString(R.string.wear_loading_state),
                    )
                    val phone = nodes.firstOrNull { it.isNearby } ?: nodes.first()
                    messageClient.sendMessage(phone.id, STATE_REQUEST_PATH, byteArrayOf())
                        .addOnFailureListener {
                            uiState = uiState.copy(
                                connected = false,
                                hasSession = false,
                                hasResponse = false,
                                message = getString(R.string.wear_phone_unavailable),
                            )
                        }
                }
            }
            .addOnFailureListener {
                uiState = uiState.copy(
                    connected = false,
                    hasSession = false,
                    hasResponse = false,
                    message = getString(R.string.wear_phone_unavailable),
                )
            }
    }

    private fun sendControl(action: String, value: String = "") {
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                uiState = uiState.copy(
                    connected = false,
                    hasSession = false,
                    hasResponse = false,
                    message = getString(R.string.wear_phone_required),
                )
                return@addOnSuccessListener
            }
            val payload = JSONObject()
                .put("action", action)
                .put("value", value)
                .toString()
                .toByteArray(Charsets.UTF_8)
            val phone = nodes.firstOrNull { it.isNearby } ?: nodes.first()
            messageClient.sendMessage(phone.id, CONTROL_PATH, payload)
                .addOnFailureListener {
                    uiState = uiState.copy(
                        connected = false,
                        hasSession = false,
                        hasResponse = false,
                        message = getString(R.string.wear_control_failed),
                    )
                }
        }.addOnFailureListener {
            uiState = uiState.copy(
                connected = false,
                hasSession = false,
                hasResponse = false,
                message = getString(R.string.wear_phone_unavailable),
            )
        }
    }
}

@Composable
private fun WearPlayerScreen(
    state: WatchPlaybackState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    val scrollState = rememberScrollState()
    val focusRequester = androidx.compose.runtime.remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .requestFocusOnHierarchyActive()
            .focusRequester(focusRequester)
            .focusable()
            .verticalScroll(scrollState)
            .rotaryScrollable(
                behavior = RotaryScrollableDefaults.behavior(scrollableState = scrollState),
                focusRequester = focusRequester,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.wear_player_brand), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.title.ifBlank { stringResource(R.string.wear_player_title) },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (state.reciter.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.wear_reciter, state.reciter),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (state.hasSession) {
            Text(
                text = stringResource(
                    R.string.wear_playback_position,
                    formatTime(state.positionMs),
                    formatTime(state.durationMs),
                ),
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onPrevious,
                    enabled = state.connected && state.hasSession && state.canPrevious,
                ) {
                    Text(stringResource(R.string.wear_previous))
                }
                Button(
                    onClick = { onSeek(-SEEK_STEP_MS) },
                    enabled = state.connected && state.hasSession && state.canSeek,
                ) {
                    Text(stringResource(R.string.wear_back_15))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onPlayPause, enabled = state.connected && state.hasSession) {
                    Text(
                        stringResource(
                            if (state.isPlaying) R.string.wear_pause else R.string.wear_play,
                        ),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onSeek(SEEK_STEP_MS) },
                    enabled = state.connected && state.hasSession && state.canSeek,
                ) {
                    Text(stringResource(R.string.wear_forward_15))
                }
                Button(
                    onClick = onNext,
                    enabled = state.connected && state.hasSession && state.canNext,
                ) {
                    Text(stringResource(R.string.wear_next))
                }
            }
        } else {
            Text(
                text = state.message.ifBlank {
                    stringResource(
                        if (state.connected) R.string.wear_no_active_playback
                        else R.string.wear_connecting,
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    if (milliseconds <= 0L) return "0:00"
    val totalSeconds = milliseconds / 1_000L
    val seconds = (totalSeconds % 60L).toString().padStart(2, '0')
    val minutes = (totalSeconds / 60L) % 60L
    val hours = totalSeconds / 3_600L
    return if (hours > 0L) {
        hours.toString() + ":" + minutes.toString().padStart(2, '0') + ":" + seconds
    } else {
        minutes.toString() + ":" + seconds
    }
}
