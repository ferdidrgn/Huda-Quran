package org.ferdidrgn.hudaquran.data.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.ferdidrgn.hudaquran.data.local.QuranCache
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.remote.QuranApi
import org.ferdidrgn.hudaquran.data.remote.QuranFoundationApi
import org.ferdidrgn.hudaquran.data.remote.QuranFoundationProxyConfig
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
    private val quranFoundation: QuranFoundationApi = QuranFoundationApi(
        QuranFoundationProxyConfig("https://hudaquran.web.app/api/qf"),
    ),
    private val quranpediaApi: QuranpediaApi = QuranpediaApi(),
    private val cache: QuranCache = QuranCache(),
) {

    private var cachedSurahList: List<Surah>? = null
    private val cachedSurahMeanings = mutableMapOf<String, Map<Int, String>>()

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

    /**
     * Returns translated chapter meanings from Quran Foundation for the selected UI language.
     * Arabic UI intentionally requests English meanings. Failed/empty responses are not cached so
     * a later visit can recover; the caller keeps its English fallback for offline use.
     */
    suspend fun getLocalizedSurahMeanings(language: AppLanguage): Map<Int, String> {
        val apiLanguage = if (language == AppLanguage.ARABIC) AppLanguage.ENGLISH.code else language.code
        cachedSurahMeanings[apiLanguage]?.let { return it }

        val payload = try {
            withTimeoutOrNull(3_000L) { quranFoundation.chapters(apiLanguage) } ?: return emptyMap()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return emptyMap()
        }

        val chapters = payload["chapters"] as? JsonArray ?: return emptyMap()
        val meanings = chapters.mapNotNull { element ->
            val chapter = element as? JsonObject ?: return@mapNotNull null
            val number = (chapter["id"] as? JsonPrimitive)?.intOrNull ?: return@mapNotNull null
            val translatedName = chapter["translated_name"] as? JsonObject ?: return@mapNotNull null
            val meaning = (translatedName["name"] as? JsonPrimitive)?.content?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: return@mapNotNull null
            number to meaning
        }.toMap()

        if (meanings.isNotEmpty()) cachedSurahMeanings[apiLanguage] = meanings
        return meanings
    }

    suspend fun getSurahDetail(
        surahNumber: Int,
        translationEdition: String = QuranEditions.DEFAULT_TRANSLATION,
        reciterEdition: String = QuranEditions.DEFAULT_RECITER,
    ): SurahDetail {
        // Quran Foundation chapter reciters provide complete-surah recordings. AlQuran Cloud
        // remains the ayah-by-ayah source so selecting one never sends a chapter ID as an edition.
        val ayahAudioEdition = reciterEdition.takeUnless { it.startsWith("qf.") }
            ?: QuranEditions.DEFAULT_RECITER
        val fetched = runCatching {
            val editions = retryOnce {
                api.getSurahWithEditions(
                    surahNumber,
                    listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, ayahAudioEdition),
                )
            }
            val arabicEdition = editions[0]
            val translationEditionData = editions[1]
            val audioEdition = editions[2]

            val qfAyahAudio = getQuranFoundationAyahAudio(
                reciterEdition,
                mapOf(surahNumber to (arabicEdition.ayahs.maxOfOrNull { it.numberInSurah } ?: 0)),
            )
            val ayahs = arabicEdition.ayahs.mapIndexed { index, ayahDto ->
                Ayah(
                    surahNumber = arabicEdition.number,
                    surahName = arabicEdition.englishName,
                    numberInSurah = ayahDto.numberInSurah,
                    globalNumber = ayahDto.number,
                    arabicText = ayahDto.text,
                    translationText = translationEditionData.ayahs.getOrNull(index)?.text.orEmpty(),
                    audioUrl = qfAyahAudio["$surahNumber:${ayahDto.numberInSurah}"]
                        ?: audioEdition.ayahs.getOrNull(index)?.audio.orEmpty(),
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
                surahAudioUrl = when {
                    reciterEdition.startsWith("qf.chapter.") ->
                        runCatching { getQuranFoundationChapterAudioUrl(reciterEdition, surahNumber) }
                            .getOrNull()?.takeIf(String::isNotBlank)
                            ?: QuranEditions.surahAudioUrl(surahNumber, ayahAudioEdition)
                    reciterEdition.startsWith("qf.ayah.") ->
                        QuranEditions.surahAudioUrl(surahNumber, QuranEditions.DEFAULT_RECITER)
                    else -> QuranEditions.surahAudioUrl(surahNumber, reciterEdition)
                },
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
        val ayahAudioEdition = reciterEdition.takeUnless { it.startsWith("qf.") }
            ?: QuranEditions.DEFAULT_RECITER
        val fetched = runCatching {
            val editions = retryOnce {
                api.getSectionWithEditions(
                    kind.apiPath,
                    number,
                    listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, ayahAudioEdition),
                )
            }
            val arabicEdition = editions[0]
            val translationEditionData = editions[1]
            val audioEdition = editions[2]

            val qfAyahAudio = getQuranFoundationAyahAudio(
                reciterEdition,
                arabicEdition.ayahs.groupBy { it.surah.number }
                    .mapValues { (_, rows) -> rows.maxOfOrNull { it.numberInSurah } ?: 0 },
            )
            val ayahs = arabicEdition.ayahs.mapIndexed { index, ayahDto ->
                Ayah(
                    surahNumber = ayahDto.surah.number,
                    surahName = ayahDto.surah.englishName,
                    numberInSurah = ayahDto.numberInSurah,
                    globalNumber = ayahDto.number,
                    arabicText = ayahDto.text,
                    translationText = translationEditionData.ayahs.getOrNull(index)?.text.orEmpty(),
                    audioUrl = qfAyahAudio["${ayahDto.surah.number}:${ayahDto.numberInSurah}"]
                        ?: audioEdition.ayahs.getOrNull(index)?.audio.orEmpty(),
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
        val ayahAudioEdition = reciterEdition.takeUnless { it.startsWith("qf.") }
            ?: QuranEditions.DEFAULT_RECITER
        val editions = listOf(QuranEditions.ARABIC_TEXT_EDITION, translationEdition, ayahAudioEdition)
            .map { edition -> async { retryOnce { api.getSajdaAyahs(edition) } } }
            .awaitAll()
        val arabic = editions[0]
        val translation = editions[1]
        val audio = editions[2]

        val qfAyahAudio = getQuranFoundationAyahAudio(
            reciterEdition,
            arabic.ayahs.groupBy { it.surah.number }
                .mapValues { (_, rows) -> rows.maxOfOrNull { it.numberInSurah } ?: 0 },
        )
        arabic.ayahs.mapIndexed { index, ayahDto ->
            Ayah(
                surahNumber = ayahDto.surah.number,
                surahName = ayahDto.surah.englishName,
                numberInSurah = ayahDto.numberInSurah,
                globalNumber = ayahDto.number,
                arabicText = ayahDto.text,
                translationText = translation.ayahs.getOrNull(index)?.text.orEmpty(),
                audioUrl = qfAyahAudio["${ayahDto.surah.number}:${ayahDto.numberInSurah}"]
                    ?: audio.ayahs.getOrNull(index)?.audio.orEmpty(),
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

    suspend fun getReciters(): List<Reciter> {
        cachedReciters?.let { return it }
        // Keep the curated catalogue visible even when an audio CDN blocks HEAD/range probes.
        // Preflighting every voice here made the picker shrink to only one or two items on some
        // networks; playback itself reports a genuinely unavailable recording when previewed.
        val apiReciters = runCatching {
            arabicRecitationEditions(api.getEditions(format = "audio", type = "versebyverse"))
                .map { edition -> Reciter(edition.identifier, edition.englishName.ifBlank { edition.name }) }
        }.getOrDefault(emptyList())
        val loaded = (apiReciters + QuranEditions.reciters)
            .distinctBy(Reciter::identifier)
            .sortedWith(compareBy({ it.identifier != QuranEditions.DEFAULT_RECITER }, { it.displayName }))
        // Chapter-reciter and ayah-by-ayah IDs are separate catalogs in Quran Foundation.
        val qfAyahReciters = withTimeoutOrNull(2_500L) {
            runCatching {
                val payload = quranFoundation.recitations("tr")
                (payload["recitations"] as? JsonArray).orEmpty().mapNotNull { item ->
                    val recitation = item as? JsonObject ?: return@mapNotNull null
                    val id = (recitation["id"] as? JsonPrimitive)?.intOrNull ?: return@mapNotNull null
                    val translatedName = (recitation["translated_name"] as? JsonObject)?.get("name")
                        ?.jsonPrimitive?.content
                    val baseName = translatedName?.takeIf(String::isNotBlank)
                        ?: recitation["reciter_name"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank)
                        ?: return@mapNotNull null
                    val style = recitation["style"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank)
                    Reciter("qf.ayah.$id", listOfNotNull(baseName, style).joinToString(" · "))
                }
            }.getOrDefault(emptyList())
        }.orEmpty()
        val chapterReciters = withTimeoutOrNull(2_500L) {
            runCatching {
                val payload = quranFoundation.chapterReciters("tr")
                (payload["reciters"] as? JsonArray).orEmpty().mapNotNull { item ->
                    val reciter = item as? JsonObject ?: return@mapNotNull null
                    val id = (reciter["id"] as? JsonPrimitive)?.intOrNull ?: return@mapNotNull null
                    val translatedName = (reciter["translated_name"] as? JsonObject)?.get("name")
                        ?.jsonPrimitive?.content
                    val baseName = translatedName?.takeIf(String::isNotBlank)
                        ?: reciter["name"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank)
                        ?: return@mapNotNull null
                    val style = (reciter["style"] as? JsonObject)?.get("name")?.jsonPrimitive?.content
                    Reciter("qf.chapter.$id", listOfNotNull(baseName, style?.takeIf(String::isNotBlank)).joinToString(" · "))
                }
            }.getOrDefault(emptyList())
        }.orEmpty()
        val combined = (loaded + qfAyahReciters + chapterReciters).distinctBy(Reciter::identifier)
        cachedReciters = combined
        return combined
    }

    suspend fun getQuranFoundationChapterAudioUrl(reciterIdentifier: String, chapter: Int): String? {
        val id = reciterIdentifier.removePrefix("qf.chapter.").toIntOrNull() ?: return null
        if (chapter !in 1..114) return null
        val payload = quranFoundation.chapterAudio(id, chapter)
        return (payload["audio_file"] as? JsonObject)?.get("audio_url")?.jsonPrimitive?.content
            ?: payload["audio_file"]?.jsonPrimitive?.content
    }

    private suspend fun getQuranFoundationAyahAudio(
        reciterIdentifier: String,
        chapterMaxVerseNumbers: Map<Int, Int>,
    ): Map<String, String> {
        val recitationId = reciterIdentifier.removePrefix("qf.ayah.").toIntOrNull() ?: return emptyMap()
        val chapters = chapterMaxVerseNumbers.filter { (chapter, maxVerse) -> chapter in 1..114 && maxVerse > 0 }
        if (chapters.isEmpty()) return emptyMap()
        return withTimeoutOrNull(8_000L) {
            runCatching {
                coroutineScope {
                    chapters.flatMap { (chapter, maxVerse) ->
                        (1..((maxVerse + 49) / 50)).map { page ->
                            async {
                                runCatching {
                                    quranFoundation.verses(
                                        chapter = chapter,
                                        language = "tr",
                                        page = page,
                                        perPage = 50,
                                        audioRecitationId = recitationId,
                                    )
                                }.getOrNull()
                            }
                        }
                    }.awaitAll().flatMap { payload ->
                        (payload?.get("verses") as? JsonArray).orEmpty().mapNotNull { item ->
                            val verse = item as? JsonObject ?: return@mapNotNull null
                            val verseKey = verse["verse_key"]?.jsonPrimitive?.content
                                ?: return@mapNotNull null
                            val audio = verse["audio"] as? JsonObject
                            val url = audio?.get("url")?.jsonPrimitive?.content
                                ?: verse["audio_url"]?.jsonPrimitive?.content
                                ?: return@mapNotNull null
                            verseKey to url
                        }
                    }.toMap()
                }
            }.getOrDefault(emptyMap())
        }.orEmpty()
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
