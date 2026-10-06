package org.ferdidrgn.hudaquran.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

/**
 * Plays the synthesised chime through a static [AudioTrack] on the media stream. AudioTrack takes
 * no audio focus, so it never interrupts (or is paused by) the recitation's ExoPlayer.
 */
actual class PageTurnChime actual constructor() {
    private val pcm: ShortArray by lazy { ChimeSynth.pcm16() }
    private var track: AudioTrack? = null

    actual fun play() {
        try {
            val t = track ?: build().also { track = it }
            if (t.playState != AudioTrack.PLAYSTATE_STOPPED) t.stop()
            t.reloadStaticData()
            t.play()
        } catch (e: Exception) {
            Log.w("HudaQuranChime", "Page-turn chime failed", e)
            release()
        }
    }

    actual fun release() {
        runCatching { track?.release() }
        track = null
    }

    private fun build(): AudioTrack {
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(ChimeSynth.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        t.write(pcm, 0, pcm.size)
        t.setVolume(0.7f)
        return t
    }
}
