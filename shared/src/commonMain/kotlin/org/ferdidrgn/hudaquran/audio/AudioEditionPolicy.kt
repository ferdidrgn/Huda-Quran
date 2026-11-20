package org.ferdidrgn.hudaquran.audio

import org.ferdidrgn.hudaquran.data.remote.dto.EditionDto

/**
 * Rules shared by the live edition catalogue and audio playback.
 *
 * The AlQuran Cloud audio catalogue also contains translations and recordings that are not
 * Arabic verse-by-verse recitations. Those editions must never appear as qari choices.
 */
internal fun isArabicVerseByVerseRecitation(edition: EditionDto): Boolean =
    edition.format.equals("audio", ignoreCase = true) &&
        edition.type.equals("versebyverse", ignoreCase = true) &&
        edition.language.equals("ar", ignoreCase = true)

internal fun arabicRecitationEditions(editions: List<EditionDto>): List<EditionDto> =
    editions.filter(::isArabicVerseByVerseRecitation).distinctBy { it.identifier }

private val fallbackBitrates = listOf(128, 64, 192, 48, 40, 32)
private val cdnBitrate = Regex("""(/quran/audio(?:-surah)?/)(\d+)(/)""")

/** Returns the next CDN URL and bitrate, or null after all supported alternatives were tried. */
internal fun nextAudioBitrateUrl(url: String, tried: Set<Int>): Pair<String, Int>? {
    val match = cdnBitrate.find(url) ?: return null
    val current = match.groupValues[2].toIntOrNull() ?: return null
    val next = fallbackBitrates.firstOrNull { it != current && it !in tried } ?: return null
    val currentSegment = "${match.groupValues[1]}$current${match.groupValues[3]}"
    return url.replace(currentSegment, "${match.groupValues[1]}$next${match.groupValues[3]}") to next
}

internal fun audioBitrate(url: String): Int? = cdnBitrate.find(url)?.groupValues?.get(2)?.toIntOrNull()
