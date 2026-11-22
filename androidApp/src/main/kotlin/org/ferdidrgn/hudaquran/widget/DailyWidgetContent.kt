package org.ferdidrgn.hudaquran.widget

import android.content.Context
import org.ferdidrgn.hudaquran.data.local.VaktelyaDuaLocalDataSource
import org.ferdidrgn.hudaquran.data.local.VaktelyaHadithLocalDataSource
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.HadeethEncAttribution
import org.ferdidrgn.hudaquran.R
import kotlinx.coroutines.withTimeoutOrNull

internal enum class DailyContentKind(val id: String, val title: String, val titleResource: Int) {
    AYAH("ayah", "Ayah of the day", R.string.widget_daily_ayah),
    DUA("dua", "Dua of the day", R.string.widget_daily_dua),
    HADITH("hadith", "Hadith of the day", R.string.widget_daily_hadith);

    companion object {
        fun parse(value: String?): DailyContentKind = entries.firstOrNull { it.id == value } ?: AYAH
    }
}

/** One attributed content item shown on a home-screen widget. */
internal data class DailyWidgetItem(
    val kind: DailyContentKind,
    val title: String,
    val reference: String,
    val arabic: String,
    val text: String,
    val source: String,
    val sourceUrl: String?,
    val author: String,
    val licenseNotice: String,
    val language: String,
)

/**
 * Per-kind daily snapshots. A widget never fabricates a verse translation or substitutes
 * unattributed content: until an attributed item is available it shows an honest empty state.
 */
internal object DailyWidgetContent {
    private const val STORE = "huda_daily_widget_v2"

    fun forDay(context: Context, dayOfYear: Int, kind: DailyContentKind): DailyWidgetItem? {
        val prefs = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
        val prefix = "${kind.id}_"
        if (prefs.getInt(prefix + "day", Int.MIN_VALUE) != dayOfYear) return null
        val title = prefs.getString(prefix + "title", null).orEmpty()
        val reference = prefs.getString(prefix + "reference", null).orEmpty()
        val text = prefs.getString(prefix + "text", null).orEmpty()
        val source = prefs.getString(prefix + "source", null).orEmpty()
        if (title.isBlank() || text.isBlank() || source.isBlank()) return null
        return DailyWidgetItem(
            kind = kind,
            title = title,
            reference = reference,
            arabic = prefs.getString(prefix + "arabic", null).orEmpty(),
            text = text,
            source = source,
            sourceUrl = prefs.getString(prefix + "sourceUrl", null),
            author = prefs.getString(prefix + "author", null).orEmpty(),
            licenseNotice = prefs.getString(prefix + "license", null).orEmpty(),
            language = prefs.getString(prefix + "language", "tr").orEmpty(),
        )
    }

    fun cacheForDay(context: Context, dayOfYear: Int, item: DailyWidgetItem) {
        if (item.title.isBlank() || item.text.isBlank() || item.source.isBlank()) return
        val prefix = "${item.kind.id}_"
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit()
            .putInt(prefix + "day", dayOfYear)
            .putString(prefix + "title", item.title.take(100))
            .putString(prefix + "reference", item.reference.take(180))
            .putString(prefix + "arabic", item.arabic.take(900))
            .putString(prefix + "text", item.text.take(900))
            .putString(prefix + "source", item.source.take(180))
            .putString(prefix + "sourceUrl", item.sourceUrl?.take(300))
            .putString(prefix + "author", item.author.take(180))
            .putString(prefix + "license", item.licenseNotice.take(240))
            .putString(prefix + "language", item.language.take(12))
            .apply()
    }

    /**
     * Content is obtained from the same repositories/approved local catalogues as the app.
     * Remote Quran access is bounded; dua and hadith are read from their attributed offline files.
     */
    suspend fun loadForDay(context: Context, dayOfYear: Int, kind: DailyContentKind): DailyWidgetItem? = when (kind) {
        DailyContentKind.AYAH -> loadAyah(context)
        DailyContentKind.DUA -> loadDua(context, dayOfYear)
        DailyContentKind.HADITH -> loadHadith(context, dayOfYear)
    }

    private suspend fun loadAyah(context: Context): DailyWidgetItem? {
        val repository = AppContainer.repository
        val editionId = AppContainer.preferences.selectedTranslation.trim()
            .ifBlank { "tr.diyanet" }
        val daily = withTimeoutOrNull(8_000L) {
            runCatching { repository.getDailyAyah(editionId) }.getOrNull()
        } ?: return null
        val edition = withTimeoutOrNull(2_000L) {
            runCatching { repository.getTranslations().firstOrNull { it.identifier == editionId } }.getOrNull()
        } ?: return null
        if (daily.translationText.isBlank() || edition.source.isBlank()) return null
        return DailyWidgetItem(
            kind = DailyContentKind.AYAH,
            title = DailyContentKind.AYAH.title,
            reference = "${daily.surahName} ${daily.surahNumber}:${daily.numberInSurah}",
            arabic = daily.arabicText,
            text = daily.translationText,
            source = edition.source,
            sourceUrl = edition.sourceUrl,
            author = edition.author?.takeIf(String::isNotBlank)
                ?: context.getString(R.string.widget_daily_author_unlisted),
            licenseNotice = edition.licenseNotice?.takeIf(String::isNotBlank)
                ?: context.getString(R.string.widget_daily_license_unlisted),
            language = edition.language,
        )
    }

    private suspend fun loadDua(context: Context, dayOfYear: Int): DailyWidgetItem? {
        val items = runCatching { VaktelyaDuaLocalDataSource.load() }
            .getOrDefault(emptyList())
            .filter { it.isVerified && it.dua.meaning.isNotBlank() && it.source.isNotBlank() }
        if (items.isEmpty()) return null
        val content = items[Math.floorMod(dayOfYear - 1, items.size)]
        return DailyWidgetItem(
            kind = DailyContentKind.DUA,
            title = content.dua.title,
            reference = listOfNotNull(content.dua.title, content.reference, content.dua.category)
                .filter(String::isNotBlank)
                .distinct()
                .joinToString(" · "),
            arabic = content.dua.arabic,
            text = content.dua.meaning,
            source = content.source,
            sourceUrl = content.sourceUrl,
            author = context.getString(R.string.widget_daily_author_unlisted),
            licenseNotice = context.getString(R.string.widget_daily_license_unlisted),
            language = content.language,
        )
    }

    private suspend fun loadHadith(context: Context, dayOfYear: Int): DailyWidgetItem? {
        val items = runCatching { VaktelyaHadithLocalDataSource.list("tr") }
            .getOrDefault(emptyList())
            .filter { it.text.isNotBlank() && it.sourceUrl?.startsWith("https://") == true }
        if (items.isEmpty()) return null
        val hadith = items[Math.floorMod(dayOfYear - 1, items.size)]
        return DailyWidgetItem(
            kind = DailyContentKind.HADITH,
            title = DailyContentKind.HADITH.title,
            reference = hadith.reference ?: hadith.grade.orEmpty(),
            arabic = hadith.arabic.orEmpty(),
            text = hadith.text,
            source = hadith.source ?: HadeethEncAttribution.providerName,
            sourceUrl = hadith.sourceUrl ?: HadeethEncAttribution.providerUrl,
            author = hadith.grade?.let { context.getString(R.string.widget_daily_hadith_grade, it) }
                ?: context.getString(R.string.widget_daily_author_unlisted),
            licenseNotice = HadeethEncAttribution.notice,
            language = hadith.languageCode,
        )
    }

    // Kept only for existing deterministic helper tests. The widget renderer deliberately never
    // calls this legacy, unattributed translation list.
    private val LEGACY_UNATTRIBUTED_FALLBACK = listOf(
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Bakara 2:286", "", "Allah hiç kimseye gücünün yettiğinden fazlasını yüklemez.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "İnşirah 94:5", "", "Şüphesiz güçlükle beraber bir kolaylık vardır.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Ra'd 13:28", "", "Kalpler ancak Allah'ı anmakla huzur bulur.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Zümer 39:53", "", "Allah'ın rahmetinden ümit kesmeyin.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Ankebut 29:69", "", "Bizim uğrumuzda gayret edenleri elbette yollarımıza eriştireceğiz.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Talak 65:3", "", "Kim Allah'a güvenirse O, ona yeter.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Bakara 2:152", "", "Beni anın ki Ben de sizi anayım; Bana şükredin, nankörlük etmeyin.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Hud 11:6", "", "Yeryüzünde hiçbir canlı yoktur ki rızkı Allah'a ait olmasın.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Ali İmran 3:139", "", "Gevşemeyin, üzülmeyin; inanıyorsanız üstün gelecek olan sizsiniz.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Bakara 2:153", "", "Sabır ve namazla yardım dileyin. Şüphesiz Allah sabredenlerle beraberdir.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Rad 13:11", "", "Bir toplum kendilerindeki durumu değiştirmedikçe Allah onların durumunu değiştirmez.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Tevbe 9:51", "", "De ki: Allah'ın bizim için yazdığından başkası bize asla erişmez.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Nahl 16:97", "", "Kim iman ederek salih amel işlerse ona güzel bir hayat yaşatacağız.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Şura 42:36", "", "Size verilen şeyler dünya hayatının geçimliğidir; Allah katındaki ise daha hayırlı ve kalıcıdır.", "", null, "", "", "tr"),
        DailyWidgetItem(DailyContentKind.AYAH, DailyContentKind.AYAH.title, "Fecr 89:27-30", "", "Ey huzura ermiş nefis! Rabbine dön; O senden razı, sen de O'ndan razı.", "", null, "", "", "tr"),
    )

    @Deprecated("Legacy unit-test fixture only; widgets must use attributed cache or unavailable state")
    internal fun fallbackForDay(dayOfYear: Int): DailyWidgetItem =
        LEGACY_UNATTRIBUTED_FALLBACK[Math.floorMod(dayOfYear - 1, LEGACY_UNATTRIBUTED_FALLBACK.size)]

    @Deprecated("Legacy unit-test fixture only; do not use as widget content")
    internal val fallbackSize: Int get() = LEGACY_UNATTRIBUTED_FALLBACK.size
}
