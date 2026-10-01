package org.ferdidrgn.hudaquran.data.repository

import com.russhwolf.settings.Settings
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.ferdidrgn.hudaquran.data.local.createSettings
import org.ferdidrgn.hudaquran.data.remote.QuranHttpClient

/** One year's fitre (sadaka-i fıtır) as announced by Diyanet; the same amount is the daily fidye. */
@Serializable
data class FitreEntry(
    val period: String,
    val amountTry: Double,
    val validFrom: String = "",
    val source: String = "",
)

/**
 * The religious reference values the calculators need. Hosted as
 * `webApp/src/webMain/resources/config/religious-values.json` (served at [CONFIG_URL]) so a new
 * year's fitre can be published with a one-line edit and a web deploy — every installed app picks
 * it up on its next launch, no store release needed.
 */
@Serializable
data class ReligiousValues(
    val updatedAt: String = "",
    val nisabGoldGrams: Double = DIYANET_NISAB_GOLD_GRAMS,
    val zakatRate: Double = ZAKAT_RATE,
    val fitre: List<FitreEntry> = emptyList(),
) {
    /** The entry in force today: the latest one whose validFrom has passed (ISO dates sort as text). */
    fun currentFitre(today: String): FitreEntry? =
        fitre.filter { it.validFrom.isEmpty() || it.validFrom <= today }.maxByOrNull { it.validFrom }
            ?: fitre.maxByOrNull { it.validFrom }
}

/** Live gold price: grams of 24k gold priced in USD, plus the USD→TRY/EUR rates of the same fetch. */
@Serializable
data class GoldQuote(
    val gramUsd: Double,
    val usdToTry: Double,
    val usdToEur: Double,
    val fetchedOn: String,
) {
    fun gramPrice(currency: ZakatCurrency): Double = when (currency) {
        ZakatCurrency.TRY -> gramUsd * usdToTry
        ZakatCurrency.EUR -> gramUsd * usdToEur
        ZakatCurrency.USD -> gramUsd
    }
}

enum class ZakatCurrency(val symbol: String) { TRY("₺"), EUR("€"), USD("$") }

const val DIYANET_NISAB_GOLD_GRAMS = 80.18
const val ZAKAT_RATE = 0.025
private const val GRAMS_PER_TROY_OUNCE = 31.1034768

/** Shipped with the app so the calculators still work offline on first launch. */
val BundledReligiousValues = ReligiousValues(
    updatedAt = "2026-09-30",
    fitre = listOf(
        FitreEntry(
            period = "2026",
            amountTry = 240.0,
            validFrom = "2026-02-18",
            source = "Diyanet İşleri Başkanlığı Din İşleri Yüksek Kurulu",
        ),
    ),
)

@Serializable
private data class GoldApiResponse(val price: Double)

@Serializable
private data class FxResponse(val rates: Map<String, Double> = emptyMap())

/**
 * Loads the zakat/fitre reference values and the live gold price, caching the last good copy of
 * each so the screen still shows real numbers offline (marked with the date they were fetched).
 *
 * Sources: gold spot price from gold-api.com (USD per troy ounce), currency rates from Frankfurter
 * (European Central Bank reference rates) — both free, keyless and CORS-enabled for the website.
 */
class ReligiousValuesRepository(private val settings: Settings = createSettings()) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client get() = QuranHttpClient.client

    suspend fun loadValues(): ReligiousValues {
        val remote = runCatching {
            json.decodeFromString<ReligiousValues>(client.get(CONFIG_URL).bodyAsText())
        }.getOrNull()?.takeIf { it.fitre.isNotEmpty() }
        if (remote != null) {
            runCatching { settings.putString(KEY_VALUES, json.encodeToString(ReligiousValues.serializer(), remote)) }
            return remote
        }
        return settings.getStringOrNull(KEY_VALUES)
            ?.let { runCatching { json.decodeFromString<ReligiousValues>(it) }.getOrNull() }
            ?: BundledReligiousValues
    }

    /** Today's gold quote, or the last one fetched successfully; null only if never fetched. */
    suspend fun loadGoldQuote(): GoldQuote? {
        val live = runCatching {
            val ounceUsd = json.decodeFromString<GoldApiResponse>(client.get(GOLD_URL).bodyAsText()).price
            val rates = json.decodeFromString<FxResponse>(client.get(FX_URL).bodyAsText()).rates
            GoldQuote(
                gramUsd = ounceUsd / GRAMS_PER_TROY_OUNCE,
                usdToTry = rates.getValue("TRY"),
                usdToEur = rates.getValue("EUR"),
                fetchedOn = today(),
            )
        }.getOrNull()?.takeIf { it.gramUsd > 0 && it.usdToTry > 0 }
        if (live != null) {
            runCatching { settings.putString(KEY_GOLD, json.encodeToString(GoldQuote.serializer(), live)) }
            return live
        }
        return settings.getStringOrNull(KEY_GOLD)
            ?.let { runCatching { json.decodeFromString<GoldQuote>(it) }.getOrNull() }
    }

    companion object {
        const val CONFIG_URL = "https://hudaquran.web.app/config/religious-values.json"
        private const val GOLD_URL = "https://api.gold-api.com/price/XAU"
        private const val FX_URL = "https://api.frankfurter.dev/v1/latest?base=USD&symbols=TRY,EUR"
        private const val KEY_VALUES = "religious_values"
        private const val KEY_GOLD = "gold_quote"

        fun today(): String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
}

/** Zakat on net zakatable wealth: nothing below nisab, otherwise the zakat rate on the whole amount. */
fun zakatDue(netWealth: Double, nisab: Double, rate: Double = ZAKAT_RATE): Double =
    if (netWealth >= nisab && netWealth > 0) netWealth * rate else 0.0
