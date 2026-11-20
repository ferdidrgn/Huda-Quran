package org.ferdidrgn.hudaquran.ui.share

import org.ferdidrgn.hudaquran.domain.model.Hadith
import org.ferdidrgn.hudaquran.ui.navigation.DeepLink
import org.ferdidrgn.hudaquran.ui.navigation.Screen

data class HadithShareData(
    val id: String,
    val title: String,
    val arabic: String,
    val translation: String,
    val explanation: String,
    val grade: String,
    val reference: String,
    val source: String,
    val link: String,
    val languageCode: String = "tr",
    val sourceUrl: String = "",
) {
    /** The app route is generated centrally; [link] is its equivalent HTTPS web route. */
    val appLink: String get() = DeepLink.buildLink(Screen.HadithDetail(id))
}

fun Hadith.toShareData(): HadithShareData = HadithShareData(
    id = id,
    title = title,
    arabic = arabic.orEmpty(),
    translation = text,
    explanation = explanation.orEmpty(),
    grade = grade.orEmpty(),
    reference = reference.orEmpty(),
    source = source.orEmpty().ifBlank { "HadeethEnc.com" },
    link = "https://$SHARE_SITE_HOST/hadith/$id?src=hadith_card",
    languageCode = languageCode,
    sourceUrl = sourceUrl.orEmpty(),
)

fun hadithShareMessage(data: HadithShareData): String = buildString {
    append(data.title)
    if (data.arabic.isNotBlank()) append("\n\nArapça metin:\n${data.arabic}")
    if (data.translation.isNotBlank()) append("\n\nTürkçe anlam:\n${data.translation}")
    if (data.explanation.isNotBlank()) append("\n\nAçıklama:\n${data.explanation}")
    append("\n\nDil: ${hadithLanguageLabel(data.languageCode)}")
    if (data.grade.isNotBlank()) append("\n\nSıhhat: ${data.grade}")
    if (data.reference.isNotBlank()) append("\nKaynak / referans: ${data.reference}")
    append("\nYayın: ${data.source}")
    if (data.sourceUrl.isNotBlank()) append("\nKaynak sayfası: ${data.sourceUrl}")
    append("\n\nWeb bağlantısı:\n${data.link}")
    append("\nUygulamada aç:\n${data.appLink}")
}

fun hadithLanguageLabel(code: String): String = when (code.trim().lowercase()) {
    "tr", "tr-tr" -> "Türkçe"
    "ar", "ar-sa" -> "العربية"
    "en", "en-us", "en-gb" -> "English"
    "fr", "fr-fr" -> "Français"
    "de", "de-de" -> "Deutsch"
    "es", "es-es" -> "Español"
    else -> code.trim().ifBlank { "Dil bilgisi yok" }
}
