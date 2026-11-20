package org.ferdidrgn.hudaquran.data.local

import hudaquran.shared.generated.resources.Res
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class VaktelyaCalendarEvent(
    val title: String,
    val date: LocalDate,
    val sourceUrl: String,
)

data class VaktelyaCalendarSnapshot(
    val checkedOn: LocalDate,
    val region: String,
    val events: List<VaktelyaCalendarEvent>,
)

/** Offline calendar facts shipped by Vaktelya, kept separate from calculated Hijri dates. */
object VaktelyaCalendarRepository {
    private const val RESOURCE = "files/vaktelya_religious_calendar.json"
    private const val MAX_SNAPSHOT_AGE_DAYS = 120

    suspend fun getUpcomingEvents(today: LocalDate): VaktelyaCalendarSnapshot? = runCatching {
        val root = Json.parseToJsonElement(Res.readBytes(RESOURCE).decodeToString()).jsonObject
        val checkedOn = LocalDate.parse(root.getValue("checked_on").jsonPrimitive.content)
        val ageDays = checkedOn.daysUntil(today)
        require(ageDays in 0..MAX_SNAPSHOT_AGE_DAYS) { "Calendar snapshot is stale or dated in the future" }

        val events = root.getValue("events").jsonArray.mapNotNull { element ->
            runCatching {
                val item = element.jsonObject
                val title = item.getValue("title").jsonPrimitive.content.trim()
                val date = LocalDate.parse(item.getValue("date").jsonPrimitive.content)
                val sourceUrl = item.getValue("source").jsonPrimitive.content.trim()
                if (title.isBlank() || !sourceUrl.startsWith("https://")) return@runCatching null
                VaktelyaCalendarEvent(title, date, sourceUrl)
            }.getOrNull()?.takeIf { it.date >= today }
        }.sortedBy { it.date }

        VaktelyaCalendarSnapshot(
            checkedOn = checkedOn,
            region = root["region"]?.jsonPrimitive?.content.orEmpty(),
            events = events,
        )
    }.getOrNull()
}
