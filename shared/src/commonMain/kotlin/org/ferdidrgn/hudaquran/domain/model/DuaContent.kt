package org.ferdidrgn.hudaquran.domain.model

/**
 * Extended dua content. The original [Dua] model remains untouched for compatibility with the
 * existing UI and persisted references; this model adds provenance and optional metadata.
 */
data class DuaContent(
    val dua: Dua,
    val source: String,
    val sourceUrl: String? = null,
    val language: String = "tr",
    val tags: List<String> = emptyList(),
    val isVerified: Boolean = true,
)

data class DuaContentDto(
    val id: String,
    val category: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val meaning: String,
    val source: String,
    val sourceUrl: String? = null,
    val language: String = "tr",
    val tags: List<String> = emptyList(),
)

fun DuaContentDto.toDomain(): DuaContent = DuaContent(
    dua = Dua(id, category, title, arabic, transliteration, meaning),
    source = source,
    sourceUrl = sourceUrl,
    language = language,
    tags = tags,
)
