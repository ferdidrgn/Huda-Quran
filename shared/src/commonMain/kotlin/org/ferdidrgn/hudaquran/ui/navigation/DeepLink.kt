package org.ferdidrgn.hudaquran.ui.navigation

import org.ferdidrgn.hudaquran.domain.model.SectionKind

/**
 * Maps between app URLs (custom scheme `hudaquran:`, https App Links, or a bare path such as
 * the one the web build reads from `window.location.pathname`) and [Screen] destinations.
 *
 * Anything that builds a shareable `hudaquran:` link (a future share button, a QR code, ...)
 * MUST go through [buildLink] rather than hand-concatenating `"hudaquran://" + path`: the
 * double-slash form treats the first path segment as a host per RFC 3986 and silently swallows
 * it (`hudaquran://surah/2` would parse "surah" as the host, leaving only "/2"). [buildLink]
 * uses the single-slash, no-authority form (`hudaquran:/surah/2`) to sidestep that entirely.
 */
object DeepLink {
    const val SCHEME = "hudaquran"

    /** The one correct way to build a shareable app link — see the class doc for why. */
    fun buildLink(screen: Screen): String = "$SCHEME:${toPath(screen)}"

    fun parse(rawUrl: String): Screen? {
        val path = extractPath(rawUrl) ?: return null
        val segments = path.trim('/').split("/").filter { it.isNotEmpty() }
        if (segments.isEmpty()) return Screen.Home

        val sectionKind = SectionKind.entries.firstOrNull { it.apiPath == segments[0] }
        if (sectionKind != null) {
            val number = segments.getOrNull(1)?.toIntOrNull()
            return if (number != null) Screen.SectionDetail(sectionKind, number) else Screen.SectionList(sectionKind)
        }

        return when (segments[0]) {
            "surah" -> {
                val number = segments.getOrNull(1)?.toIntOrNull() ?: return null
                val ayah = segments.getOrNull(2)?.toIntOrNull()
                Screen.SurahDetail(number, ayah)
            }
            "favorites" -> Screen.Favorites
            "search" -> Screen.Search
            "settings" -> Screen.Settings
            "reciters" -> Screen.ReciterPicker
            "sajda" -> Screen.SajdaAyahs
            "lessons" -> {
                val lessonId = segments.getOrNull(1)
                if (lessonId != null) Screen.TajwidLessonDetail(lessonId) else Screen.TajwidLessonList
            }
            "mushaf" -> Screen.MushafPage(segments.getOrNull(1)?.toIntOrNull() ?: 1)
            "qibla" -> Screen.Qibla
            else -> null
        }
    }

    /** Extracts a bare `/path` from a custom-scheme URL, an https URL, or a path already. */
    private fun extractPath(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return null

        // hudaquran://host/path — double-slash authority form. Only tolerated for links this
        // app didn't itself generate; [buildLink] never produces this form (see class doc).
        val authorityPrefix = "$SCHEME://"
        if (trimmed.startsWith(authorityPrefix)) {
            val afterScheme = trimmed.removePrefix(authorityPrefix)
            val hostAndPath = afterScheme.substringBefore('?').substringBefore('#')
            val slashIndex = hostAndPath.indexOf('/')
            return if (slashIndex >= 0) hostAndPath.substring(slashIndex) else "/"
        }

        // hudaquran:/path — the form [buildLink] actually produces: no authority, so the very
        // next character is the path, with nothing to mistake for a host.
        val opaquePrefix = "$SCHEME:"
        if (trimmed.startsWith(opaquePrefix)) {
            val pathAndQuery = trimmed.removePrefix(opaquePrefix).substringBefore('?').substringBefore('#')
            return if (pathAndQuery.startsWith("/")) pathAndQuery else "/$pathAndQuery"
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val afterScheme = trimmed.substringAfter("://")
            val slashIndex = afterScheme.indexOf('/')
            val pathAndQuery = if (slashIndex >= 0) afterScheme.substring(slashIndex) else "/"
            return pathAndQuery.substringBefore('?').substringBefore('#')
        }

        if (trimmed.startsWith("/")) return trimmed.substringBefore('?').substringBefore('#')

        return null
    }

    /** Reverse mapping used to keep the browser address bar in sync as the user navigates. */
    fun toPath(screen: Screen): String = when (screen) {
        is Screen.Splash -> "/"
        is Screen.Onboarding -> "/"
        is Screen.Home -> "/"
        is Screen.SurahList -> "/surah"
        is Screen.SurahDetail -> if (screen.scrollToAyah != null) {
            "/surah/${screen.surahNumber}/${screen.scrollToAyah}"
        } else {
            "/surah/${screen.surahNumber}"
        }
        is Screen.Favorites -> "/favorites"
        is Screen.Settings -> "/settings"
        is Screen.ReciterPicker -> "/reciters"
        is Screen.TranslationPicker -> "/settings"
        is Screen.TafsirPicker -> "/settings"
        is Screen.AyahTafsir -> "/tafsir/${screen.globalAyahNumber}"
        is Screen.PrayerLocationPicker -> "/settings"
        is Screen.LanguagePicker -> "/settings"
        is Screen.TajwidLessonList -> "/lessons"
        is Screen.TajwidLessonDetail -> "/lessons/${screen.lessonId}"
        is Screen.Search -> "/search"
        is Screen.SectionList -> "/${screen.kind.apiPath}"
        is Screen.SectionDetail -> "/${screen.kind.apiPath}/${screen.number}"
        is Screen.SajdaAyahs -> "/sajda"
        is Screen.NowPlaying -> "/"
        is Screen.MushafPage -> "/mushaf/${screen.pageNumber}"
        is Screen.Qibla -> "/qibla"
    }
}
