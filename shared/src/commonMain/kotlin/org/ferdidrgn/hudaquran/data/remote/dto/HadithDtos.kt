package org.ferdidrgn.hudaquran.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ferdidrgn.hudaquran.domain.model.Hadith
import org.ferdidrgn.hudaquran.domain.model.HadithCategory
import org.ferdidrgn.hudaquran.domain.model.HadithLanguage
import org.ferdidrgn.hudaquran.domain.model.HadithPage
import org.ferdidrgn.hudaquran.domain.model.HadithSummary

@Serializable
data class HadithLanguageDto(val code: String, val native: String)

@Serializable
data class HadithCategoryDto(
    val id: String,
    val title: String,
    @SerialName("hadeeths_count") val hadithCount: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
)

@Serializable
data class HadithSummaryDto(
    val id: String,
    val title: String,
    val translations: List<String> = emptyList(),
)

@Serializable
data class HadithListMetaDto(
    @SerialName("current_page") val currentPage: String? = null,
    @SerialName("last_page") val lastPage: String? = null,
    @SerialName("total_items") val totalItems: String? = null,
    @SerialName("per_page") val perPage: String? = null,
)

@Serializable
data class HadithListResponseDto(
    val data: List<HadithSummaryDto> = emptyList(),
    val meta: HadithListMetaDto? = null,
)

@Serializable
data class HadithDetailDto(
    val id: String,
    val title: String,
    @SerialName("hadeeth") val hadith: String? = null,
    val arabic: String? = null,
    val text: String? = null,
    val explanation: String? = null,
    val grade: String? = null,
    val attribution: String? = null,
    val reference: String? = null,
    val source: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val categories: List<String> = emptyList(),
    val hints: List<String> = emptyList(),
    val translations: List<String> = emptyList(),
)

/** Shape of Vaktelya's inspected hadith-tr.json. It is intentionally not bundled in the app. */
@Serializable
data class HadithLocalDocumentDto(
    val metadata: String = "",
    @SerialName("source_url") val sourceUrl: String? = null,
    val hadiths: List<HadithLocalEntryDto> = emptyList(),
)

@Serializable
data class HadithLocalEntryDto(
    val id: Int,
    val title: String,
    val arabic: String? = null,
    val text: String? = null,
    val explanation: String? = null,
    val grade: String? = null,
    val reference: String? = null,
    val source: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
)

fun HadithLanguageDto.toDomain() = HadithLanguage(code = code, nativeName = native)

fun HadithCategoryDto.toDomain() = HadithCategory(
    id = id,
    title = title,
    hadithCount = hadithCount?.toIntOrNull(),
    parentId = parentId,
)

fun HadithSummaryDto.toDomain() = HadithSummary(id = id, title = title, availableTranslations = translations)

fun HadithListResponseDto.toDomain() = HadithPage(
    items = data.map(HadithSummaryDto::toDomain),
    currentPage = meta?.currentPage?.toIntOrNull() ?: 1,
    lastPage = meta?.lastPage?.toIntOrNull() ?: 1,
    totalItems = meta?.totalItems?.toIntOrNull() ?: data.size,
    perPage = meta?.perPage?.toIntOrNull() ?: data.size,
)

fun HadithDetailDto.toDomain(languageCode: String) = Hadith(
    id = id,
    title = title,
    arabic = arabic,
    text = hadith ?: text.orEmpty(),
    explanation = explanation,
    grade = grade,
    reference = reference ?: attribution,
    source = source.cleanMetadataOrNull() ?: "HadeethEnc",
    sourceUrl = sourceUrl.cleanMetadataOrNull() ?: "https://hadeethenc.com/${normalizeHadithLanguageCode(languageCode)}",
    languageCode = normalizeHadithLanguageCode(languageCode),
    categories = categories,
    hints = hints,
)

fun HadithLocalEntryDto.toDomain() = Hadith(
    id = id.toString(),
    title = title,
    arabic = arabic,
    text = text.orEmpty(),
    explanation = explanation,
    grade = grade,
    reference = reference,
    source = source.cleanMetadataOrNull() ?: "HadeethEnc",
    sourceUrl = sourceUrl.cleanMetadataOrNull() ?: "https://hadeethenc.com/tr",
    languageCode = "tr",
)

/** Provider codes are metadata, not user-facing prose. Keep them stable before filtering/saving. */
fun normalizeHadithLanguageCode(value: String): String = value
    .trim()
    .lowercase()
    .replace('_', '-')
    .substringBefore('-')
    .ifBlank { "und" }

private fun String?.cleanMetadataOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }
