package org.ferdidrgn.hudaquran.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

@Serializable
data class ApiResponseDto<T>(
    val code: Int,
    val status: String,
    val data: T,
)

@Serializable
data class SurahDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
)

@Serializable
data class EditionDto(
    val identifier: String,
    val language: String,
    val name: String,
    val englishName: String,
    val format: String,
    val type: String,
)

@Serializable
data class AyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    @Serializable(with = SajdaSerializer::class)
    val sajda: Boolean = false,
    val audio: String? = null,
)

@Serializable
data class SurahEditionDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val ayahs: List<AyahDto>,
    val edition: EditionDto? = null,
)

@Serializable
data class AyahWithSurahDto(
    val number: Int,
    val text: String,
    val surah: SurahDto,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    @Serializable(with = SajdaSerializer::class)
    val sajda: Boolean = false,
    val audio: String? = null,
)

@Serializable
data class JuzEditionDto(
    val number: Int,
    val ayahs: List<AyahWithSurahDto>,
    val edition: EditionDto? = null,
)

@Serializable
data class SearchMatchDto(
    val number: Int,
    val text: String,
    val edition: EditionDto? = null,
    val surah: SurahDto,
    val numberInSurah: Int,
)

@Serializable
data class SearchResultDto(
    val count: Int,
    val matches: List<SearchMatchDto>,
)

/** The `sajda` field is `false` or an object when the ayah requires prostration. */
object SajdaSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Sajda", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> element.booleanOrNull ?: true
            else -> true
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}
