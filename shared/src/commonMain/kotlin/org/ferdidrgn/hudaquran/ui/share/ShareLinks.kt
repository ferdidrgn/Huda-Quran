package org.ferdidrgn.hudaquran.ui.share

import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.repository.DailyAyah
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName

/**
 * The single place shared links are built. Every link carries a `src` attribution (see
 * `DeepLinkController`, which logs it when the link is opened) so we can tell which share
 * surface brought a visitor in. The website route `/surah/{surah}/{ayah}` opens the ayah in the
 * web build, and Android App Links / iOS hand it to the installed app first.
 */
const val SHARE_SITE_HOST = "hudaquran.web.app"
private const val SHARE_SITE_URL = "https://$SHARE_SITE_HOST"

/** `src` values used by the share surfaces. */
const val SRC_AYAH_CARD = "ayah_card"
const val SRC_APP_SHARE = "app_share"

fun ayahLink(surah: Int, ayah: Int, src: String = SRC_AYAH_CARD): String =
    "$SHARE_SITE_URL/surah/$surah/$ayah?src=$src"

fun appLink(src: String = SRC_APP_SHARE): String = "$SHARE_SITE_URL/?src=$src"

/** What to share about one ayah: shown on the postcard and sent as text / link. */
data class AyahShareData(
    val surahNumber: Int,
    val ayahNumber: Int,
    /** Already localised surah name. */
    val surahName: String,
    val arabic: String,
    /** The reader's current translation; may be blank. */
    val translation: String,
) {
    val reference: String get() = "$surahNumber:$ayahNumber"
}

/** `{arabic}\n\n{translation}\n— {surahName} {surah}:{ayah}\n\n{link}` */
fun ayahShareMessage(data: AyahShareData, src: String = SRC_AYAH_CARD): String = buildString {
    append(data.arabic)
    if (data.translation.isNotBlank()) {
        append("\n\n")
        append(data.translation)
    }
    append("\n— ")
    append(data.surahName)
    append(' ')
    append(data.reference)
    append("\n\n")
    append(ayahLink(data.surahNumber, data.ayahNumber, src))
}

fun DailyAyah.toShareData(language: AppLanguage): AyahShareData = AyahShareData(
    surahNumber = surahNumber,
    ayahNumber = numberInSurah,
    surahName = localizedSurahName(surahNumber, surahName, language),
    arabic = arabicText,
    translation = translationText,
)
