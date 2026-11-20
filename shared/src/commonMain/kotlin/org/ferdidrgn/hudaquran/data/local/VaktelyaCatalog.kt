package org.ferdidrgn.hudaquran.data.local

import hudaquran.shared.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Verified, attribution-carrying recordings shipped by the Vaktelya APK. */
data class VaktelyaRecording(
    val id: String,
    val title: String,
    val reader: String,
    val ayah: Int,
    val sourceUrl: String,
    val localResourcePath: String,
)

object VaktelyaCatalog {
    private const val MANIFEST = "files/vaktelya_recordings.json"

    suspend fun verifiedRecordings(): List<VaktelyaRecording> = runCatching {
        val root = Json.parseToJsonElement(Res.readBytes(MANIFEST).decodeToString()).jsonObject
        root["duas"]?.jsonObject.orEmpty().values.mapNotNull { value ->
            val item = value.jsonObject
            val asset = item["asset"]?.jsonPrimitive?.content ?: return@mapNotNull null
            VaktelyaRecording(
                id = item["id"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                title = item["title"]?.jsonPrimitive?.content.orEmpty(),
                reader = item["reader"]?.jsonPrimitive?.content.orEmpty(),
                ayah = item["ayah"]?.jsonPrimitive?.content?.toIntOrNull() ?: return@mapNotNull null,
                sourceUrl = item["source_url"]?.jsonPrimitive?.content.orEmpty(),
                localResourcePath = asset.removePrefix("assets/").let { "files/vaktelya_${it.substringAfterLast('/').lowercase().replace('-', '_')}" },
            )
        }
    }.getOrDefault(emptyList())

    fun localUri(recording: VaktelyaRecording): String = Res.getUri(recording.localResourcePath)

    const val esmaLocalResourcePath: String = "files/vaktelya_esma_ul_husna.ogg"
}
