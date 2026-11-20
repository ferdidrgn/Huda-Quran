package org.ferdidrgn.hudaquran.data.remote

import org.ferdidrgn.hudaquran.data.remote.dto.HadithDetailDto
import org.ferdidrgn.hudaquran.data.remote.dto.normalizeHadithLanguageCode
import org.ferdidrgn.hudaquran.data.remote.dto.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HadithDtoTest {
    @Test
    fun detailKeepsExplanationSeparateFromTheHadithText() {
        val hadith = HadithDetailDto(
            id = "7",
            title = "Merhamet",
            hadith = "Hadis metni",
            text = "Alternatif metin",
            arabic = "النص",
            explanation = "Açıklama",
            reference = "Buhari",
            source = "HadeethEnc",
        ).toDomain("tr")

        assertEquals("Hadis metni", hadith.text)
        assertEquals("Açıklama", hadith.explanation)
        assertTrue(hadith.text != hadith.explanation)
        assertEquals("Buhari", hadith.reference)
    }

    @Test
    fun detailFallsBackToTextWhenProviderUsesTextField() {
        val hadith = HadithDetailDto(
            id = "8",
            title = "Sabır",
            text = "Sağlanan metin",
        ).toDomain("tr")

        assertEquals("Sağlanan metin", hadith.text)
    }

    @Test
    fun detailKeepsLanguageAndAddsTruthfulProviderMetadataWhenOptionalFieldsAreMissing() {
        val hadith = HadithDetailDto(
            id = "9",
            title = "Bilgi",
            hadith = "Metin",
            source = "   ",
            sourceUrl = null,
        ).toDomain(" TR_tr ")

        assertEquals("tr", hadith.languageCode)
        assertEquals("HadeethEnc", hadith.source)
        assertEquals("https://hadeethenc.com/tr", hadith.sourceUrl)
    }

    @Test
    fun languageNormalizationDoesNotInventTurkishForUnknownMetadata() {
        assertEquals("fr", normalizeHadithLanguageCode(" FR-fr "))
        assertEquals("und", normalizeHadithLanguageCode("  "))
    }
}
