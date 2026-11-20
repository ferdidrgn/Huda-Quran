package org.ferdidrgn.hudaquran.audio

import org.ferdidrgn.hudaquran.data.remote.dto.EditionDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AudioEditionPolicyTest {

    private fun edition(
        identifier: String,
        language: String = "ar",
        format: String = "audio",
        type: String = "versebyverse",
    ) = EditionDto(identifier, language, identifier, identifier, format, type)

    @Test
    fun liveCatalogueKeepsOnlyArabicVerseByVerseAudio() {
        val liveLikeCatalogue = listOf(
            edition("ar.alafasy"),
            edition("ar.husary"),
            edition("tr.diyanet", language = "tr"),
            edition("ar.alafasy", format = "text"),
            edition("ar.alafasy", type = "translation"),
        )

        assertEquals(
            listOf("ar.alafasy", "ar.husary"),
            arabicRecitationEditions(liveLikeCatalogue).map { it.identifier },
        )
    }

    @Test
    fun duplicateEditionIdentifiersAreReturnedOnce() {
        val editions = arabicRecitationEditions(listOf(edition("ar.alafasy"), edition("ar.alafasy")))

        assertEquals(1, editions.size)
    }

    @Test
    fun bitrateFallbackFollowsSupportedCdnOrder() {
        val first = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3"
        val second = nextAudioBitrateUrl(first, setOf(128))
        val third = nextAudioBitrateUrl(second!!.first, setOf(128, second.second))

        assertEquals(64, second.second)
        assertEquals("https://cdn.islamic.network/quran/audio/64/ar.alafasy/1.mp3", second.first)
        assertEquals(192, third!!.second)
    }

    @Test
    fun bitrateFallbackSupportsWholeSurahCdnPath() {
        val url = "https://cdn.islamic.network/quran/audio-surah/128/ar.husary/2.mp3"

        assertEquals(
            "https://cdn.islamic.network/quran/audio-surah/64/ar.husary/2.mp3" to 64,
            nextAudioBitrateUrl(url, setOf(128)),
        )
    }

    @Test
    fun bitrateFallbackStopsForNonCdnUrlsOrExhaustedAlternatives() {
        assertNull(nextAudioBitrateUrl("https://example.com/audio.mp3", emptySet()))
        assertNull(nextAudioBitrateUrl("https://cdn.islamic.network/quran/audio/32/ar.x/1.mp3", setOf(32, 128, 64, 192, 48, 40)))
    }

    @Test
    fun loadingWatchdogOnlyFailsTheSameStillLoadingTrack() {
        val url = "https://cdn.islamic.network/quran/audio/128/ar.x/1.mp3"
        assertEquals(true, isStuckLoadingTrack(PlaybackState(PlaybackStatus.LOADING, url), url))
        assertEquals(false, isStuckLoadingTrack(PlaybackState(PlaybackStatus.PLAYING, url), url))
        assertEquals(false, isStuckLoadingTrack(PlaybackState(PlaybackStatus.LOADING, "other"), url))
        assertEquals(false, isStuckLoadingTrack(PlaybackState(PlaybackStatus.LOADING, null), null))
    }
}
