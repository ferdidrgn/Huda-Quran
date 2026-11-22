package org.ferdidrgn.hudaquran.wear

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
private const val STATE_REFRESH_MS = 2_000L

private data class WatchPlaybackState(
    val connected: Boolean = false,
    val hasSession: Boolean = false,
    val message: String = "Telefondaki Kur'an oynatıcısına bağlanıyor.",
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
                message = if (hasSession) "" else "Telefonda bir Kur'an yayını başlat.",
                title = json.optString("title", "Kur'an dinleme"),
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
                        message = "Huda Quran kurulu bir Android telefon bağlanmalı.",
                    )
                } else {
                    uiState = uiState.copy(connected = true, message = "Oynatıcı durumu alınıyor.")
                    nodes.forEach { node ->
                        messageClient.sendMessage(node.id, STATE_REQUEST_PATH, byteArrayOf())
                    }
                }
            }
            .addOnFailureListener {
                uiState = uiState.copy(
                    connected = false,
                    hasSession = false,
                    message = "Telefon bağlantısı kurulamadı.",
                )
            }
    }

    private fun sendControl(action: String, value: String = "") {
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                uiState = uiState.copy(
                    connected = false,
                    message = "Huda Quran kurulu bir Android telefon bağlanmalı.",
                )
                return@addOnSuccessListener
            }
            val payload = JSONObject()
                .put("action", action)
                .put("value", value)
                .toString()
                .toByteArray(Charsets.UTF_8)
            nodes.forEach { node -> messageClient.sendMessage(node.id, CONTROL_PATH, payload) }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("HUDA KUR'AN", style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (state.reciter.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = state.reciter,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (state.hasSession) {
            Text(
                text = formatTime(state.positionMs) + " / " + formatTime(state.durationMs),
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onPrevious, enabled = state.canPrevious) { Text("Önceki") }
                Button(onClick = { onSeek(-SEEK_STEP_MS) }, enabled = state.canSeek) { Text("−15") }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onPlayPause) {
                    Text(if (state.isPlaying) "Duraklat" else "Oynat")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { onSeek(SEEK_STEP_MS) }, enabled = state.canSeek) { Text("+15") }
                Button(onClick = onNext, enabled = state.canNext) { Text("Sonraki") }
            }
        } else {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    if (milliseconds <= 0L) return "0:00"
    val totalSeconds = milliseconds / 1_000L
    return (totalSeconds / 60L).toString() + ":" +
        (totalSeconds % 60L).toString().padStart(2, '0')
}
