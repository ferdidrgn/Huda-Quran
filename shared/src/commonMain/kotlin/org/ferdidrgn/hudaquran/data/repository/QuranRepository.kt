package org.ferdidrgn.hudaquran.data.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.ferdidrgn.hudaquran.data.local.QuranCache
import org.ferdidrgn.hudaquran.data.remote.QuranApi
import org.ferdidrgn.hudaquran.data.remote.QuranpediaApi
import org.ferdidrgn.hudaquran.data.remote.normalizeContentLanguageCode
import org.ferdidrgn.hudaquran.data.remote.TurkishQuranApi
import org.ferdidrgn.hudaquran.audio.arabicRecitationEditions
import org.ferdidrgn.hudaquran.data.remote.dto.SurahDto
import org.ferdidrgn.hudaquran.data.remote.retryOnce
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranEditions
import org.ferdidrgn.hudaquran.domain.model.QuranMeta
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.SearchMatch
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.domain.model.Tafsir
import org.ferdidrgn.hudaquran.domain.model.Translation
import org.ferdidrgn.hudaquran.domain.model.ContentSourceBadge

@Serializable
data class DailyAyah(
    val surahName: String,
    val surahNumber: Int,
    val numberInSurah: Int,
    val arabicText: String,
    val translationText: String,
    /** Local calendar day (yyyy-MM-dd) this ayah belongs to, and the meal it was fetched in. */
    val date: String = "",
    val translationEdition: String = "",
)

class QuranRepository(
    private val api: QuranApi = QuranApi(),
    private val turkishApi: TurkishQuranApi = TurkishQuranApi(),
    private val quranpediaApi: QuranpediaApi = QuranpediaApi(),
    private val cache: QuranCache = QuranCache(),
) {

    private var cachedSurahList: List<Surah>? = null

    suspend fun getSurahList(): List<Surah> {
        cachedSurahList?.let { return it }
        val fetched = runCatching { retryOnce { api.getSurahList() }.map { it.toDomain() } }
        fetched.getOrNull()?.let { list ->
            cachedSurahList = list
            cache.saveSurahList(list)
            return list
        }
        val offline = cache.getSurahList()
        if (offline != null) {
            cachedSurahList = offline
            return offline
        }
        throw fetched.exceptionOrNull() ?: IllegalStateException("Surah list unavailable")
    }

    suspend fun getSurahDetail(
        surahNumber: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): SurahDetail {
        val fetched = runCatching {
            val editions = retryOnce {
                api.getSurahWithEditions(
                    surahNumber,
                    listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, reciterEdition),
                )
            }
            val arabicEdition = editions[0]
            val translationEditionData = editions[1]
            val audioEdition = editions[2]

            val ayahs = arabicEdition.ayahs.mapIndexed { index, ayahDto ->
                Ayah(
                    surahNumber = arabicEdition.number,
                    surahName = arabicEdition.englishName,
                    numberInSurah = ayahDto.numberInSurah,
                    globalNumber = ayahDto.number,
                    arabicText = ayahDto.text,
                    translationText = translationEditionData.ayahs.getOrNull(index)?.text.orEmpty(),
                    audioUrl = audioEdition.ayahs.getOrNull(index)?.audio.orEmpty(),
                    juz = ayahDto.juz,
                    page = ayahDto.page,
                    isSajda = ayahDto.sajda,
                )
            }

            SurahDetail(
                surah = Surah(
                    number = arabicEdition.number,
                    name = arabicEdition.name,
                    englishName = arabicEdition.englishName,
                    englishNameTranslation = arabicEdition.englishNameTranslation,
                    numberOfAyahs = arabicEdition.numberOfAyahs,
                    revelationType = arabicEdition.revelationType,
                ),
                ayahs = ayahs,
                surahAudioUrl = QuranEditions.surahAudioUrl(surahNumber, reciterEdition),
            )
        }
        fetched.getOrNull()?.let { detail ->
            cache.saveSurahDetail(surahNumber, translationEdition, reciterEdition, detail)
            return detail
        }
        val offline = cache.getSurahDetail(surahNumber, translationEdition, reciterEdition)
        if (offline != null) return offline
        throw fetched.exceptionOrNull() ?: IllegalStateException("Surah detail unavailable")
    }

    suspend fun getSectionDetail(
        kind: SectionKind,
        number: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): QuranSectionDetail {
        val fetched = runCatching {
            val editions = retryOnce {
                api.getSectionWithEditions(
                    kind.apiPath,
                    number,
                    listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, reciterEdition),
                )
            }
            val arabicEdition = editions[0]
            val translationEditionData = editions[1]
            val audioEdition = editions[2]

            val ayahs = arabicEdition.ayahs.mapIndexed { index, ayahDto ->
                Ayah(
                    surahNumber = ayahDto.surah.number,
                    surahName = ayahDto.surah.englishName,
                    numberInSurah = ayahDto.numberInSurah,
                    globalNumber = ayahDto.number,
                    arabicText = ayahDto.text,
                    translationText = translationEditionData.ayahs.getOrNull(index)?.text.orEmpty(),
                    audioUrl = audioEdition.ayahs.getOrNull(index)?.audio.orEmpty(),
                    juz = ayahDto.juz,
                    page = ayahDto.page,
                    isSajda = ayahDto.sajda,
                )
            }

            QuranSectionDetail(sectionNumber = number, ayahs = ayahs)
        }
        fetched.getOrNull()?.let { detail ->
            cache.saveSectionDetail(kind.name, number, translationEdition, reciterEdition, detail)
            return detail
        }
        val offline = cache.getSectionDetail(kind.name, number, translationEdition, reciterEdition)
        if (offline != null) return offline
        throw fetched.exceptionOrNull() ?: IllegalStateException("Section detail unavailable")
    }

    // Recently opened Mushaf pages, kept in memory so turning back (or the page-synced recitation
    // prefetching the next page) never refetches or re-parses them. Insertion-ordered for eviction.
    private val mushafPageMemo = LinkedHashMap<String, QuranSectionDetail>()

    private fun mushafPageKey(page: Int, translationEdition: String, reciterEdition: String) =
        "$page|$translationEdition|$reciterEdition"

    /** A Mushaf page already in memory, or null — lets a page render on its first frame. */
    fun cachedMushafPage(
        page: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): QuranSectionDetail? = mushafPageMemo[mushafPageKey(page, translationEdition, reciterEdition)]

    /** One Mushaf page (Arabic + meal + per-ayah audio), memoised; falls back to the offline cache. */
    suspend fun getMushafPage(
        page: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): QuranSectionDetail {
        val key = mushafPageKey(page, translationEdition, reciterEdition)
        mushafPageMemo[key]?.let { return it }
        val detail = getSectionDetail(SectionKind.PAGE, page, translationEdition, reciterEdition)
        mushafPageMemo.remove(key)
        mushafPageMemo[key] = detail
        while (mushafPageMemo.size > MUSHAF_PAGE_MEMO_SIZE) {
            val eldest = mushafPageMemo.keys.firstOrNull() ?: break
            mushafPageMemo.remove(eldest)
        }
        return detail
    }

    suspend fun getSajdaAyahs(
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): List<Ayah> = coroutineScope {
        val editions = listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, reciterEdition)
            .map { edition -> async { retryOnce { api.getSajdaAyahs(edition) } } }
            .awaitAll()
        val arabic = editions[0]
        val translation = editions[1]
        val audio = editions[2]

        arabic.ayahs.mapIndexed { index, ayahDto ->
            Ayah(
                surahNumber = ayahDto.surah.number,
                surahName = ayahDto.surah.englishName,
                numberInSurah = ayahDto.numberInSurah,
                globalNumber = ayahDto.number,
                arabicText = ayahDto.text,
                translationText = translation.ayahs.getOrNull(index)?.text.orEmpty(),
                audioUrl = audio.ayahs.getOrNull(index)?.audio.orEmpty(),
                juz = ayahDto.juz,
                page = ayahDto.page,
                isSajda = true,
            )
        }
    }

    suspend fun getMeta(): QuranMeta {
        val meta = retryOnce { api.getMeta() }
        return QuranMeta(
            ayahCount = meta.ayahs.count,
            surahCount = meta.surahs.count,
            sajdaCount = meta.sajdas.count,
            rukuCount = meta.rukus.count,
            pageCount = meta.pages.count,
            manzilCount = meta.manzils.count,
            hizbQuarterCount = meta.hizbQuarters.count,
            juzCount = meta.juzs.count,
        )
    }

    private var cachedReciters: List<Reciter>? = null
    private var cachedTranslations: List<Translation>? = null
    private val reciterAvailability = mutableMapOf<String, Boolean?>()

    suspend fun getReciters(): List<Reciter> {
        cachedReciters?.let { return it }
        val loaded = runCatching {
            val editions = arabicRecitationEditions(api.getEditions(format = "audio", type = "versebyverse"))
            coroutineScope {
                editions.map { edition ->
                    async {
                        val available = reciterAvailability[edition.identifier]
                            ?: api.isAudioUrlReachable(QuranEditions.surahAudioUrl(1, edition.identifier))
                                .also { reciterAvailability[edition.identifier] = it }
                        // Only a confirmed HTTP failure removes an edition. A timeout/offline
                        // device leaves the catalogue intact so it cannot collapse to two items.
                        if (available != false) Reciter(edition.identifier, edition.englishName.ifBlank { edition.name }) else null
                    }
                }.awaitAll().filterNotNull()
            }.sortedWith(compareBy({ it.identifier != QuranEditions.DEFAULT_RECITER }, { it.displayName }))
        }.getOrElse {
            coroutineScope {
                QuranEditions.reciters.map { reciter ->
                    async {
                        val available = reciterAvailability[reciter.identifier]
                            ?: api.isAudioUrlReachable(QuranEditions.surahAudioUrl(1, reciter.identifier))
                                .also { reciterAvailability[reciter.identifier] = it }
                        if (available != false) reciter else null
                    }
                }.awaitAll().filterNotNull()
            }
        }
        cachedReciters = loaded
        return loaded
    }

    suspend fun getTranslations(languageCode: String? = null): List<Translation> {
        cachedTranslations?.let { return filterTranslations(it, languageCode) }
        val apiCatalog = runCatching {
            api.getEditions(format = "text", type = "translation")
                .map {
                    Translation(
                        identifier = it.identifier,
                        language = normalizeContentLanguageCode(it.language).orEmpty(),
                        displayName = it.englishName.ifBlank { it.name },
                        source = "AlQuran Cloud",
                        sourceUrl = "https://alquran.cloud/api",
                    )
                }
                .filter { it.identifier.isNotBlank() && it.language.isNotBlank() && it.displayName.isNotBlank() }
                .distinctBy { it.identifier }
                .sortedWith(compareBy<Translation>({ it.language != "tr" }, { it.language }, { it.displayName }))
        }.getOrNull()
        // An empty/error response must never be replaced with hand-written editions. A previous
        // API catalogue is useful offline, but a missing catalogue remains an honest empty state.
        val stable = apiCatalog?.takeIf { it.isNotEmpty() } ?: cache.getTranslations().orEmpty()
        // Do not memoise an empty catalogue: a transient network failure must be retryable
        // after connectivity returns instead of leaving the picker empty for the process lifetime.
        cachedTranslations = stable.takeIf { it.isNotEmpty() }
        if (!apiCatalog.isNullOrEmpty()) cache.saveTranslations(stable)
        return filterTranslations(stable, languageCode)
    }

    private var cachedTafsirs: List<Tafsir>? = null

    suspend fun getTafsirs(languageCode: String? = null): List<Tafsir> {
        cachedTafsirs?.let { return filterTafsirs(it, languageCode) }
        val cloudTafsirs = runCatching {
            api.getEditions(format = "text", type = "tafsir")
                .map {
                    Tafsir(
                        identifier = it.identifier,
                        language = normalizeContentLanguageCode(it.language).orEmpty(),
                        displayName = it.englishName.ifBlank { it.name },
                        source = "AlQuran Cloud",
                        sourceUrl = "https://alquran.cloud/api",
                    )
                }
                .filter { it.identifier.isNotBlank() && it.language.isNotBlank() && it.displayName.isNotBlank() }
                .distinctBy { it.identifier }
        }.getOrElse { emptyList() }
        val turkishTafsirs = runCatching {
            turkishApi.getEditions().map {
                Tafsir(
                    identifier = "tr.${it.slug}",
                    language = normalizeContentLanguageCode(it.language) ?: "tr",
                    displayName = it.name,
                    author = it.author,
                    source = "Kuran Meali (üçüncü taraf API)",
                    sourceUrl = TurkishQuranApi.BASE_URL,
                )
            }
                .filter { it.identifier.isNotBlank() && it.displayName.isNotBlank() }
        }.getOrElse { emptyList() }
        val quranpediaTafsirs = runCatching {
            quranpediaApi.getTafsirEditions().map {
                Tafsir(
                    identifier = it.identifier,
                    language = normalizeContentLanguageCode(it.language).orEmpty(),
                    displayName = it.displayName,
                    author = it.author,
                    source = "Quranpedia",
                    sourceUrl = QuranpediaApi.BASE_URL,
                )
            }
                .filter { it.identifier.isNotBlank() && it.language.isNotBlank() && it.displayName.isNotBlank() }
        }.getOrElse { emptyList() }
        // Keep Turkish commentary at the top, then English and other API-backed sources.
        val ordered = (turkishTafsirs + cloudTafsirs + quranpediaTafsirs)
            .distinctBy { it.identifier }
            .sortedWith(compareBy<Tafsir>({ it.language != "tr" }, { it.language }, { it.displayName }))
        val stable = ordered.takeIf { it.isNotEmpty() } ?: cache.getTafsirs().orEmpty()
        // Keep an empty result retryable; only a real catalogue is a valid in-memory cache entry.
        cachedTafsirs = stable.takeIf { it.isNotEmpty() }
        if (ordered.isNotEmpty()) cache.saveTafsirs(stable)
        return filterTafsirs(stable, languageCode)
    }

    private fun filterTranslations(items: List<Translation>, languageCode: String?): List<Translation> {
        val normalized = normalizeContentLanguageCode(languageCode)
        return items.sortedWith(compareBy<Translation>({ it.language != "tr" }, { it.language }, { it.displayName }))
            .filter { normalized == null || it.language == normalized }
    }

    private fun filterTafsirs(items: List<Tafsir>, languageCode: String?): List<Tafsir> {
        val normalized = normalizeContentLanguageCode(languageCode)
        return items.sortedWith(compareBy<Tafsir>({ it.language != "tr" }, { it.language }, { it.displayName }))
            .filter { normalized == null || it.language == normalized }
    }

    fun translationSourceBadge(translation: Translation): ContentSourceBadge = ContentSourceBadge(
        providerName = translation.source,
        sourceUrl = translation.sourceUrl,
        licenseNotice = translation.licenseNotice ?: "Lisans bilgisi sağlayıcı kataloğunda belirtilmedi.",
        languageCode = translation.language,
    )

    fun tafsirSourceBadge(tafsir: Tafsir): ContentSourceBadge = ContentSourceBadge(
        providerName = tafsir.source ?: "API kataloğu",
        sourceUrl = tafsir.sourceUrl,
        licenseNotice = tafsir.licenseNotice ?: "Lisans bilgisi sağlayıcı kataloğunda belirtilmedi.",
        languageCode = tafsir.language,
    )

    suspend fun getTafsirForAyah(
        globalAyahNumber: Int,
        tafsirEdition: String,
        surahNumber: Int? = null,
        numberInSurah: Int? = null,
    ): String {
        val cached = cache.getTafsir(globalAyahNumber, tafsirEdition)
        if (!cached.isNullOrBlank()) return cached

        if (tafsirEdition.startsWith("tr.") && surahNumber != null && numberInSurah != null) {
            val fetched = runCatching {
                turkishApi.getVerse(surahNumber, numberInSurah, tafsirEdition.removePrefix("tr."))
            }
            fetched.getOrNull()?.takeIf { it.isNotBlank() }?.let {
                cache.saveTafsir(globalAyahNumber, tafsirEdition, it)
                return it
            }
            return cache.getTafsir(globalAyahNumber, tafsirEdition).orEmpty()
        }

        if (tafsirEdition.startsWith(QuranpediaApi.EDITION_PREFIX) &&
            surahNumber != null && numberInSurah != null
        ) {
            val fetched = runCatching {
                quranpediaApi.getTafsirForAyah(surahNumber, numberInSurah, tafsirEdition)
            }
            fetched.getOrNull()?.takeIf { it.isNotBlank() }?.let {
                cache.saveTafsir(globalAyahNumber, tafsirEdition, it)
                return it
            }
            return cache.getTafsir(globalAyahNumber, tafsirEdition).orEmpty()
        }

        val fetched = runCatching {
            retryOnce { api.getAyahWithEditions(globalAyahNumber, listOf(tafsirEdition)) }
                .firstOrNull()?.text.orEmpty()
        }
        fetched.getOrNull()?.takeIf { it.isNotBlank() }?.let {
            cache.saveTafsir(globalAyahNumber, tafsirEdition, it)
            return it
        }

        // A failed request should not erase a previously saved explanation.
        return cache.getTafsir(globalAyahNumber, tafsirEdition).orEmpty()
    }

    /** Translation (meal) is intentionally separate from tafsir commentary. */
    suspend fun getMealForAyah(globalAyahNumber: Int, translationEdition: String): String = runCatching {
        retryOnce { api.getAyahWithEditions(globalAyahNumber, listOf(translationEdition)) }
            .firstOrNull()?.text.orEmpty()
    }.getOrDefault("")

    suspend fun searchQuran(keyword: String, edition: String = QuranEditions.DEFAULT_TRANSLATION): List<SearchMatch> {
        val result = retryOnce { api.search(keyword, edition) }
        return result.matches.map {
            SearchMatch(
                surahNumber = it.surah.number,
                surahName = it.surah.englishName,
                numberInSurah = it.numberInSurah,
                text = it.text,
            )
        }
    }

    /**
     * The "ayah of the day": one ayah per local calendar day, the same on web and mobile for
     * everyone that day, fetched once and then served from cache until midnight — opening Home
     * again no longer re-rolls a random ayah (and hits the API) every time.
     */
    suspend fun getDailyAyah(translationEdition: String = QuranEditions.DEFAULT_TRANSLATION): DailyAyah {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dateKey = today.toString()
        cache.getDailyAyah()
            ?.takeIf { it.date == dateKey && it.translationEdition == translationEdition }
            ?.let { return it }

        val globalAyahNumber = dailyAyahNumberFor(today.toEpochDays().toLong())
        val results = retryOnce {
            api.getAyahWithEditions(globalAyahNumber, listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition))
        }
        val arabic = results[0]
        val translation = results.getOrNull(1)
        val daily = DailyAyah(
            surahName = arabic.surah.englishName,
            surahNumber = arabic.surah.number,
            numberInSurah = arabic.numberInSurah,
            arabicText = arabic.text,
            translationText = translation?.text.orEmpty(),
            date = dateKey,
            translationEdition = translationEdition,
        )
        cache.saveDailyAyah(daily)
        return daily
    }

    private fun SurahDto.toDomain() = Surah(
        number = number,
        name = name,
        englishName = englishName,
        englishNameTranslation = englishNameTranslation,
        numberOfAyahs = numberOfAyahs,
        revelationType = revelationType,
    )
}

private const val TOTAL_AYAHS = 6236
private const val MUSHAF_PAGE_MEMO_SIZE = 40

/**
 * Day → global ayah number (1..6236). Stepping by 1683 — coprime with 6236 — walks every ayah once
 * before repeating, and consecutive days land far apart in the mushaf rather than on neighbours.
 */
internal fun dailyAyahNumberFor(epochDay: Long): Int {
    val index = ((epochDay * 1683L) % TOTAL_AYAHS + TOTAL_AYAHS) % TOTAL_AYAHS
    return index.toInt() + 1
}
