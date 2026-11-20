package org.ferdidrgn.hudaquran.domain.model

import kotlinx.serialization.Serializable

/** A language published by the selected hadith provider. */
data class HadithLanguage(
    val code: String,
    val nativeName: String,
)

data class HadithCategory(
    val id: String,
    val title: String,
    val hadithCount: Int? = null,
    val parentId: String? = null,
)

@Serializable
data class HadithSummary(
    val id: String,
    val title: String,
    val availableTranslations: List<String> = emptyList(),
)

/**
 * A provider-neutral hadith model. The Arabic text is kept separate from the selected translation
 * so callers never have to treat a translation as the original narration.
 */
@Serializable
data class Hadith(
    val id: String,
    val title: String,
    val arabic: String? = null,
    val text: String,
    val explanation: String? = null,
    val grade: String? = null,
    val reference: String? = null,
    val source: String? = null,
    val sourceUrl: String? = null,
    val languageCode: String,
    val categories: List<String> = emptyList(),
    val hints: List<String> = emptyList(),
)

@Serializable
data class HadithPage(
    val items: List<HadithSummary>,
    val currentPage: Int,
    val lastPage: Int,
    val totalItems: Int,
    val perPage: Int,
)

/** Attribution that must remain visible in any UI built on top of the provider. */
data class HadithAttribution(
    val providerName: String,
    val providerUrl: String,
    val downloadUrl: String? = null,
    val datasetVersion: String? = null,
    val lastUpdated: String? = null,
    val notice: String,
)

val HadeethEncAttribution = HadithAttribution(
    providerName = "HadeethEnc · Nebevi Hadisler Ansiklopedisi",
    providerUrl = "https://hadeethenc.com/tr",
    downloadUrl = "https://hadeethenc.com/browse/download/tr",
    notice = "İçerik HadeethEnc.com tarafından sağlanır. Kaynak ve güncelleme notu korunmalıdır.",
)

/** A compact source badge for content cards and source-detail screens. */
data class ContentSourceBadge(
    val providerName: String,
    val sourceUrl: String? = null,
    val licenseNotice: String,
    val languageCode: String? = null,
)
