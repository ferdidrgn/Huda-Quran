package org.ferdidrgn.hudaquran.data.repository

import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.domain.model.DuaContent
import org.ferdidrgn.hudaquran.domain.model.ContentSourceBadge
import org.ferdidrgn.hudaquran.domain.model.duaList

/**
 * Curated dua catalogue used by the common UI. It is deliberately backed by the existing
 * built-in content, so the app remains useful without a network connection. A future remote
 * provider can be merged here without changing the old Dua model or its consumers.
 */
class DuaContentRepository(
    private val builtIn: List<DuaContent> = duaList.map { dua ->
        DuaContent(
            dua = dua,
            source = "Kur'an ve sahih sünnet kaynaklı dua metni",
            tags = listOf(dua.category),
        )
    },
) {
    fun getAll(language: String = "tr"): List<DuaContent> =
        builtIn.filter { it.language == language }

    fun getById(id: String, language: String = "tr"): DuaContent? =
        getAll(language).firstOrNull { it.dua.id == id }

    fun getCategories(language: String = "tr"): List<String> =
        getAll(language).map { it.dua.category }.distinct()

    fun search(query: String, language: String = "tr"): List<DuaContent> {
        val needle = query.trim().lowercase()
        if (needle.isBlank()) return getAll(language)
        return getAll(language).filter { content ->
            listOf(content.dua.title, content.dua.category, content.dua.meaning, *content.tags.toTypedArray())
                .any { it.lowercase().contains(needle) }
        }
    }

    /** A single, reusable badge model prevents cards from silently losing provenance. */
    fun sourceBadge(language: String = "tr"): ContentSourceBadge = ContentSourceBadge(
        providerName = "Huda Quran seçkisi",
        sourceUrl = null,
        licenseNotice = "Dua metinleri kaynak bilgisi korunarak gösterilir; doğrulanmamış uzak içerik otomatik eklenmez.",
        languageCode = language,
    )

    /** Stable day rotation: the same dua is shown all day and changes at the next UTC day. */
    fun getDuaOfTheDay(language: String = "tr"): DuaContent? {
        val items = getAll(language)
        if (items.isEmpty()) return null
        val dayIndex = (Clock.System.now().toEpochMilliseconds() / 86_400_000L).toInt()
        val index = ((dayIndex % items.size) + items.size) % items.size
        return items[index]
    }
}
