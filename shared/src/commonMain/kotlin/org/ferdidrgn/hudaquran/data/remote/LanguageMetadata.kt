package org.ferdidrgn.hudaquran.data.remote

/**
 * Converts provider language metadata to the two-letter code used by the picker. Missing values
 * stay missing so a catalogue cannot silently advertise a guessed language.
 */
internal fun normalizeContentLanguageCode(value: String?): String? = value
    ?.trim()
    ?.lowercase()
    ?.replace('_', '-')
    ?.substringBefore('-')
    ?.takeIf(String::isNotBlank)
