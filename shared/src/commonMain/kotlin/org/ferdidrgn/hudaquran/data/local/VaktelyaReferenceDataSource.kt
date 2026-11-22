package org.ferdidrgn.hudaquran.data.local

import hudaquran.shared.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class VaktelyaEsmaRecord(
    val id: Int,
    val name: String,
    val arabic: String,
    val meaning: String,
    val source: String,
    val sourceUrl: String,
)

data class VaktelyaAdditiveRecord(
    val id: String,
    val title: String,
    val code: String,
    val meaning: String,
    val status: String,
    val source: String,
    val sourceUrl: String,
)

data class VaktelyaReferenceContent(
    val names: List<VaktelyaEsmaRecord>,
    val additives: List<VaktelyaAdditiveRecord>,
    val surahNames: List<String>,
)

/** Source-attributed reference content bundled from the Vaktelya APK. */
object VaktelyaReferenceDataSource {
    private const val RESOURCE = "files/vaktelya_content_tr.json"
    private var cached: VaktelyaReferenceContent? = null

    suspend fun load(): VaktelyaReferenceContent {
        cached?.let { return it }
        val loaded = runCatching {
        val root = Json.parseToJsonElement(Res.readBytes(RESOURCE).decodeToString()).jsonObject
        val names = root["names"]?.jsonArray.orEmpty().mapNotNull { element ->
            runCatching {
                val item = element.jsonObject
                val url = item["source_url"]?.jsonPrimitive?.content?.trim().orEmpty()
                VaktelyaEsmaRecord(
                    id = item["id"]?.jsonPrimitive?.content?.toIntOrNull() ?: return@runCatching null,
                    name = item["title"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    arabic = item["arabic"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    meaning = item["text"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    source = item["source"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    sourceUrl = url,
                ).takeIf { it.name.isNotBlank() && it.meaning.isNotBlank() && url.startsWith("https://") }
            }.getOrNull()
        }
        val additives = root["additives"]?.jsonArray.orEmpty().mapNotNull { element ->
            runCatching {
                val item = element.jsonObject
                val url = item["source_url"]?.jsonPrimitive?.content?.trim().orEmpty()
                VaktelyaAdditiveRecord(
                    id = item["id"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    title = item["title"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    code = item["code"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    meaning = item["text"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    status = item["status"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    source = item["source"]?.jsonPrimitive?.content?.trim().orEmpty(),
                    sourceUrl = url,
                ).takeIf { it.id.isNotBlank() && it.title.isNotBlank() && it.meaning.isNotBlank() && url.startsWith("https://") }
            }.getOrNull()
        }
        val surahNames = root["surah_names"]?.jsonArray.orEmpty().mapNotNull { element ->
            element.jsonPrimitive.content.trim().takeIf(String::isNotBlank)
        }
            VaktelyaReferenceContent(names, additives, surahNames)
        }.getOrDefault(VaktelyaReferenceContent(emptyList(), emptyList(), emptyList()))
        cached = loaded
        return loaded
    }
}
