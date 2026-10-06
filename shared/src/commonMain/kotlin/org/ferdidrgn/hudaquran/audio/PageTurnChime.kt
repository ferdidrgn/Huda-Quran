package org.ferdidrgn.hudaquran.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * A short, soft "page turned" cue played when a page-synced recitation turns the page by itself:
 * a brief paper swish followed by a quiet two-note bell. Synthesised in code (no asset files),
 * so every platform plays the identical sound.
 */
expect class PageTurnChime() {
    fun play()
    fun release()
}

/** Generates the chime as 16-bit mono PCM (and as a WAV file / data URI for players that need one). */
internal object ChimeSynth {
    const val SAMPLE_RATE = 22_050
    private const val DURATION_S = 0.62

    fun pcm16(): ShortArray {
        val total = (SAMPLE_RATE * DURATION_S).toInt()
        val out = ShortArray(total)
        val random = Random(7)
        var lowPassed = 0.0
        // (start s, frequency Hz, amplitude): E6 then B5 — a gentle falling bell.
        val bells = listOf(Triple(0.10, 1318.51, 0.55), Triple(0.19, 987.77, 0.45))
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            // Paper swish: low-passed noise with a quick swell and fade over ~0.16 s.
            if (t < 0.16) {
                lowPassed += 0.18 * ((random.nextDouble() * 2.0 - 1.0) - lowPassed)
                val swell = sin(PI * (t / 0.16))
                sample += 0.30 * swell * swell * lowPassed
            }

            for ((start, frequency, amplitude) in bells) {
                val local = t - start
                if (local < 0) continue
                val attack = min(1.0, local / 0.006)
                val envelope = attack * exp(-local * 6.5)
                val tone = sin(2.0 * PI * frequency * local) + 0.22 * sin(2.0 * PI * frequency * 2.0 * local)
                sample += amplitude * envelope * tone
            }

            // Fade the tail so the sound never ends on a click.
            val tail = DURATION_S - t
            if (tail < 0.05) sample *= tail / 0.05

            val scaled = (sample * 0.32).coerceIn(-1.0, 1.0)
            out[i] = (scaled * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    /** RIFF/WAVE container around [pcm16]. */
    fun wav(): ByteArray {
        val pcm = pcm16()
        val dataSize = pcm.size * 2
        val bytes = ByteArray(44 + dataSize)
        var p = 0
        fun ascii(text: String) { text.forEach { bytes[p++] = it.code.toByte() } }
        fun int32(value: Int) { for (shift in 0 until 4) bytes[p++] = (value shr (8 * shift)).toByte() }
        fun int16(value: Int) { for (shift in 0 until 2) bytes[p++] = (value shr (8 * shift)).toByte() }
        ascii("RIFF"); int32(36 + dataSize); ascii("WAVE")
        ascii("fmt "); int32(16); int16(1); int16(1)
        int32(SAMPLE_RATE); int32(SAMPLE_RATE * 2); int16(2); int16(16)
        ascii("data"); int32(dataSize)
        pcm.forEach { int16(it.toInt()) }
        return bytes
    }

    fun wavDataUri(): String = "data:audio/wav;base64," + base64(wav())

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    private fun base64(input: ByteArray): String {
        val sb = StringBuilder((input.size + 2) / 3 * 4)
        var i = 0
        while (i < input.size) {
            val b0 = input[i].toInt() and 0xFF
            val b1 = if (i + 1 < input.size) input[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < input.size) input[i + 2].toInt() and 0xFF else 0
            sb.append(ALPHABET[b0 shr 2])
            sb.append(ALPHABET[((b0 and 0x03) shl 4) or (b1 shr 4)])
            sb.append(if (i + 1 < input.size) ALPHABET[((b1 and 0x0F) shl 2) or (b2 shr 6)] else '=')
            sb.append(if (i + 2 < input.size) ALPHABET[b2 and 0x3F] else '=')
            i += 3
        }
        return sb.toString()
    }
}
