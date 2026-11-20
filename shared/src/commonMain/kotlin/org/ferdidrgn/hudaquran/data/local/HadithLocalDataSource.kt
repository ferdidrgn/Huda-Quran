package org.ferdidrgn.hudaquran.data.local

import hudaquran.shared.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ferdidrgn.hudaquran.data.remote.dto.HadithLocalDocumentDto
import org.ferdidrgn.hudaquran.domain.model.Hadith
import org.ferdidrgn.hudaquran.domain.model.HadithAttribution
import org.ferdidrgn.hudaquran.domain.model.HadeethEncAttribution
import org.ferdidrgn.hudaquran.data.remote.dto.toDomain

/**
 * Platform code may provide an approved JSON snapshot without coupling common code to a file API.
 */
interface HadithLocalDataSource {
    suspend fun get(languageCode: String, id: String): Hadith? = null
    suspend fun list(languageCode: String): List<Hadith> = emptyList()
    val attribution: HadithAttribution
}

object EmptyHadithLocalDataSource : HadithLocalDataSource {
    override val attribution: HadithAttribution = HadeethEncAttribution
}

/** Decodes an approved snapshot supplied by the host app or a test; it never reads arbitrary files. */
class HadithJsonLocalReader(
    private val rawJsonProvider: suspend (languageCode: String) -> String?,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true },
    override val attribution: HadithAttribution = HadeethEncAttribution,
) : HadithLocalDataSource {
    private val cacheMutex = Mutex()
    private val cache = mutableMapOf<String, List<Hadith>>()

    override suspend fun list(languageCode: String): List<Hadith> = cacheMutex.withLock {
        cache[languageCode]?.let { return@withLock it }
        val decoded = rawJsonProvider(languageCode)
            ?.let { runCatching { json.decodeFromString<HadithLocalDocumentDto>(it) }.getOrNull() }
            ?.hadiths
            ?.map { it.toDomain() }
            .orEmpty()
        if (decoded.isNotEmpty()) cache[languageCode] = decoded
        decoded
    }

    override suspend fun get(languageCode: String, id: String): Hadith? =
        list(languageCode).firstOrNull { it.id == id }
}

/**
 * The reviewed Vaktelya Turkish catalogue is shipped as an attributed Compose resource.
 * It is used only as an offline/fallback source; the remote provider remains preferred.
 */
object VaktelyaHadithLocalDataSource : HadithLocalDataSource {
    private val delegate = HadithJsonLocalReader(
        rawJsonProvider = { languageCode ->
            if (languageCode.trim().lowercase().substringBefore('-') != "tr") return@HadithJsonLocalReader null
            Res.readBytes("files/vaktelya_hadith_tr.json").decodeToString()
        },
        attribution = HadeethEncAttribution,
    )

    override val attribution: HadithAttribution = delegate.attribution
    override suspend fun get(languageCode: String, id: String): Hadith? = delegate.get(languageCode, id)
    override suspend fun list(languageCode: String): List<Hadith> = delegate.list(languageCode)
}
