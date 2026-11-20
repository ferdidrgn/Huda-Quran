package org.ferdidrgn.hudaquran.ui.share

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShareCardStyleTest {
    @Test
    fun socialFormatsHaveExpectedExportDimensions() {
        assertEquals(1080, ShareCardFormat.PORTRAIT_1080X1350.widthPx)
        assertEquals(1350, ShareCardFormat.PORTRAIT_1080X1350.heightPx)
        assertEquals(1080, ShareCardFormat.SQUARE_1080X1080.widthPx)
        assertEquals(1080, ShareCardFormat.SQUARE_1080X1080.heightPx)
    }

    @Test
    fun cardsFollowTheActiveAppThemeByDefault() {
        assertEquals(ShareCardTheme.APP, ShareCardOptions().theme)
    }

    @Test
    fun exportsAreExplicitlyLosslessPngAndFileNamesCannotInjectAPath() {
        assertEquals("png", ShareCardImageFormat.PNG.extension)
        assertEquals("image/png", ShareCardImageFormat.PNG.mimeType)
        assertEquals(
            "huda-hadith-42-1080x1350.png",
            shareCardFileName("huda-hadith", "42/../../secret", ShareCardFormat.PORTRAIT_1080X1350),
        )
    }

    @Test
    fun excerptCutsAtWordBoundaryAndSignalsOmission() {
        val result = shareCardExcerpt("Birinci uzun açıklama ikinci uzun açıklama", 27)
        assertTrue(result.endsWith("…"))
        assertTrue(result.length <= 27)
        assertTrue(!result.contains("açıklama ikinci"))
    }

    @Test
    fun excerptHandlesTinyOrEmptyBudgetsWithoutCrashing() {
        assertEquals("", shareCardExcerpt("metin", 0))
        assertEquals("…", shareCardExcerpt("metin", 1))
        assertEquals("", shareCardExcerpt("   ", 20))
    }

    @Test
    fun readableLinksAddOnlyVisualBreakOpportunities() {
        val original = "https://hudaquran.web.app/hadith/42?src=hadith_card"
        val readable = shareCardReadableLink(original)

        assertTrue(readable.contains("/\u200B"))
        assertTrue(readable.contains("?\u200B"))
        assertEquals(original, readable.replace("\u200B", ""))
    }

    @Test
    fun hadithBudgetsLeaveMoreRoomForPortraitCards() {
        val square = hadithShareTextLimits(ShareCardFormat.SQUARE_1080X1080)
        val portrait = hadithShareTextLimits(ShareCardFormat.PORTRAIT_1080X1350)

        assertTrue(portrait.arabicCharacters > square.arabicCharacters)
        assertTrue(portrait.translationCharacters > square.translationCharacters)
        assertTrue(portrait.explanationCharacters > square.explanationCharacters)
    }

    @Test
    fun hadithBudgetsArePositiveAndKeepExplanationMoreCompact() {
        ShareCardFormat.values().forEach { format ->
            val limits = hadithShareTextLimits(format)
            assertTrue(limits.arabicCharacters > 0)
            assertTrue(limits.translationCharacters > 0)
            assertTrue(limits.explanationCharacters > 0)
            assertTrue(limits.explanationCharacters < limits.translationCharacters)
        }
    }

    @Test
    fun hadithShareMessageKeepsArabicMeaningExplanationAndBothLinksSeparated() {
        val message = hadithShareMessage(
            HadithShareData(
                id = "42",
                title = "İyilik",
                arabic = "عربي",
                translation = "Türkçe anlam",
                explanation = "Açıklama metni",
                grade = "Sahih",
                reference = "Buhari",
                source = "HadeethEnc",
                link = "https://hudaquran.web.app/hadith/42",
            ),
        )

        assertTrue(message.contains("Arapça metin:"))
        assertTrue(message.contains("Türkçe anlam:"))
        assertTrue(message.contains("Açıklama:"))
        assertTrue(message.contains("Web bağlantısı:"))
        assertTrue(message.contains("Uygulamada aç:"))
        assertTrue(message.contains("hudaquran:/hadith/42"))
        assertFalse(message.contains("Açıklama: \n"))
    }

    @Test
    fun hadithMessageIncludesLanguageAndSourcePageMetadata() {
        val message = hadithShareMessage(
            HadithShareData(
                id = "9",
                title = "Bilgi",
                arabic = "",
                translation = "Metin",
                explanation = "",
                grade = "",
                reference = "",
                source = "HadeethEnc",
                link = "https://hudaquran.web.app/hadith/9",
                languageCode = "fr",
                sourceUrl = "https://hadeethenc.com/fr",
            ),
        )

        assertTrue(message.contains("Dil: Français"))
        assertTrue(message.contains("Kaynak sayfası: https://hadeethenc.com/fr"))
    }
}
