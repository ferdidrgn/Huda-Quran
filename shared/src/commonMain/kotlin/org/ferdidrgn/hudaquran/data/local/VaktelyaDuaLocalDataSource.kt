package org.ferdidrgn.hudaquran.data.local

import hudaquran.shared.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.ferdidrgn.hudaquran.domain.model.Dua
import org.ferdidrgn.hudaquran.domain.model.DuaContent

/** Curated Quran duas migrated from the Vaktelya APK, with their ayah and source attribution. */
object VaktelyaDuaLocalDataSource {
    private const val RESOURCE = "files/vaktelya_content_tr.json"
    private const val CATEGORY = "Kur'an'dan Dualar"

    suspend fun load(): List<DuaContent> = runCatching {
        val root = Json.parseToJsonElement(Res.readBytes(RESOURCE).decodeToString()).jsonObject
        root["duas"]?.jsonArray.orEmpty().mapNotNull { element ->
            runCatching {
                val item = element.jsonObject
                val sourceId = item["id"]?.jsonPrimitive?.content?.trim().orEmpty()
                val title = item["title"]?.jsonPrimitive?.content?.trim().orEmpty()
                val arabic = item["arabic"]?.jsonPrimitive?.content?.trim().orEmpty()
                val meaning = item["text"]?.jsonPrimitive?.content?.trim().orEmpty()
                val reference = item["reference"]?.jsonPrimitive?.content?.trim().orEmpty()
                val source = item["source"]?.jsonPrimitive?.content?.trim().orEmpty()
                val sourceUrl = item["source_url"]?.jsonPrimitive?.content?.trim().orEmpty()

                if (sourceId.isBlank() || title.isBlank() || meaning.isBlank() || source.isBlank() ||
                    !sourceUrl.startsWith("https://")
                ) return@runCatching null

                DuaContent(
                    dua = Dua(
                        id = "vaktelya_$sourceId",
                        category = CATEGORY,
                        title = title,
                        arabic = arabic,
                        transliteration = "",
                        meaning = meaning,
                    ),
                    source = source,
                    sourceUrl = sourceUrl,
                    reference = reference.ifBlank { null },
                    tags = listOfNotNull(reference.ifBlank { null }),
                )
            }.getOrNull()
        }
    }.getOrDefault(emptyList())
}
