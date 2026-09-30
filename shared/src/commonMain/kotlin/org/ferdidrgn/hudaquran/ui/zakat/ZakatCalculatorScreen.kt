package org.ferdidrgn.hudaquran.ui.zakat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.repository.BundledReligiousValues
import org.ferdidrgn.hudaquran.data.repository.GoldQuote
import org.ferdidrgn.hudaquran.data.repository.ReligiousValues
import org.ferdidrgn.hudaquran.data.repository.ReligiousValuesRepository
import org.ferdidrgn.hudaquran.data.repository.ZakatCurrency
import org.ferdidrgn.hudaquran.data.repository.zakatDue
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.FilterPill
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.OrnamentRule
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.SectionHeader
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

/**
 * Zakat, fitra and fidya with real numbers instead of a bare "2.5% of whatever you type":
 * - nisab = 80.18 g of gold (Diyanet) × today's live gold price, and zakat is only due at or
 *   above it; the price is fetched daily and the last good one is kept for offline use;
 * - fitra/fidya use the amount Diyanet announces each year, read from the hosted
 *   `config/religious-values.json` so a new year's figure reaches every install without a release.
 */
@Composable
fun ZakatCalculatorScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val preferences = AppContainer.preferences
    val repository = AppContainer.religiousValues
    val appLanguage by preferences.appLanguage.collectAsState()
    val showAds = !preferences.isAdFree()

    var values by remember { mutableStateOf<ReligiousValues>(BundledReligiousValues) }
    var quote by remember { mutableStateOf<GoldQuote?>(null) }
    var isLoadingQuote by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        values = repository.loadValues()
        quote = repository.loadGoldQuote()
        isLoadingQuote = false
    }

    var currency by remember(appLanguage) { mutableStateOf(defaultCurrencyFor(appLanguage)) }
    var manualGramPrice by remember { mutableStateOf("") }
    var cash by remember { mutableStateOf("") }
    var goldGrams by remember { mutableStateOf("") }
    var otherAssets by remember { mutableStateOf("") }
    var tradeGoods by remember { mutableStateOf("") }
    var debts by remember { mutableStateOf("") }
    var persons by remember { mutableStateOf(1) }
    var fastingDays by remember { mutableStateOf("") }

    val gramPrice = quote?.gramPrice(currency) ?: manualGramPrice.toAmount()
    val nisab = gramPrice?.let { it * values.nisabGoldGrams }
    val netWealth = (cash.toAmount() ?: 0.0) +
        (goldGrams.toAmount() ?: 0.0) * (gramPrice ?: 0.0) +
        (otherAssets.toAmount() ?: 0.0) +
        (tradeGoods.toAmount() ?: 0.0) -
        (debts.toAmount() ?: 0.0)
    val hasInput = listOf(cash, goldGrams, otherAssets, tradeGoods).any { it.isNotBlank() }
    val fitre = values.currentFitre(ReligiousValuesRepository.today())

    Box(modifier = modifier.fillMaxSize().screenBackground(), contentAlignment = Alignment.TopCenter) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PageHeader(title = strings.zakatCalculatorTitle, onBack = onBack)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.zakatCurrencyLabel, style = MaterialTheme.typography.labelLarge)
                    ZakatCurrency.entries.forEach { option ->
                        FilterPill("${option.name} ${option.symbol}", currency == option, { currency = option })
                    }
                }

                // 1. Today's reference values.
                GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = true) {
                    Text(strings.zakatLiveValuesTitle, style = MaterialTheme.typography.titleMedium)
                    OrnamentRule(modifier = Modifier.padding(vertical = 8.dp))
                    when {
                        isLoadingQuote -> Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                        quote != null -> {
                            ValueRow(strings.zakatGoldPriceLabel, formatMoney(quote!!.gramPrice(currency), currency))
                            ValueRow(
                                strings.zakatNisabTemplate.replace("{grams}", formatNumber(values.nisabGoldGrams)),
                                formatMoney(nisab ?: 0.0, currency),
                                emphasize = true,
                            )
                            Text(
                                strings.zakatPriceDateTemplate.replace("{date}", quote!!.fetchedOn),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        }
                        else -> {
                            Text(
                                strings.zakatLivePriceUnavailable,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            )
                            Spacer(Modifier.height(8.dp))
                            AmountField(strings.zakatGoldPriceInputLabel, manualGramPrice, currency) { manualGramPrice = it }
                            if (nisab != null) {
                                ValueRow(
                                    strings.zakatNisabTemplate.replace("{grams}", formatNumber(values.nisabGoldGrams)),
                                    formatMoney(nisab, currency),
                                    emphasize = true,
                                )
                            }
                        }
                    }
                }

                // 2. Zakat.
                SectionHeader(strings.zakatCalculatorTitle)
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AmountField(strings.zakatCashLabel, cash, currency) { cash = it }
                        AmountField(strings.zakatGoldGramsLabel, goldGrams, unit = "g") { goldGrams = it }
                        AmountField(strings.zakatOtherAssetsLabel, otherAssets, currency) { otherAssets = it }
                        AmountField(strings.zakatTradeGoodsLabel, tradeGoods, currency) { tradeGoods = it }
                        AmountField(strings.zakatDebtsLabel, debts, currency) { debts = it }
                    }
                    if (hasInput) {
                        OrnamentRule(modifier = Modifier.padding(vertical = 12.dp), centered = true)
                        Text(
                            strings.zakatNetWealthTemplate.replace("{amount}", formatMoney(netWealth, currency)),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(6.dp))
                        val due = nisab?.let { zakatDue(netWealth, it, values.zakatRate) }
                        if (due != null && due > 0) {
                            Text(
                                strings.zakatResultTemplate.replace("{amount}", formatMoney(due, currency)),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else if (nisab != null) {
                            Text(
                                strings.zakatBelowNisab,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                if (showAds) AdBannerCard()

                // 3. Fitra and fidya (Diyanet's yearly amount, in lira).
                SectionHeader(strings.fitreTitle)
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    if (currency != ZakatCurrency.TRY) {
                        Text(
                            strings.fitreOtherCurrencyNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    if (fitre != null) {
                        ValueRow(
                            strings.fitreAmountTemplate.replace("{period}", fitre.period),
                            formatMoney(fitre.amountTry, ZakatCurrency.TRY),
                            emphasize = true,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.fitrePersonsLabel, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            FilledTonalIconButton(onClick = { if (persons > 1) persons-- }) {
                                Icon(Icons.Filled.Remove, contentDescription = null)
                            }
                            Text(
                                persons.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 14.dp),
                            )
                            FilledTonalIconButton(onClick = { if (persons < 50) persons++ }) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                            }
                        }
                        Text(
                            strings.fitreTotalTemplate.replace("{amount}", formatMoney(fitre.amountTry * persons, ZakatCurrency.TRY)),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        OrnamentRule(modifier = Modifier.padding(vertical = 12.dp), centered = true)
                        AmountField(strings.fidyeDaysLabel, fastingDays, unit = null, decimal = false) { fastingDays = it }
                        val days = fastingDays.toAmount()?.toInt() ?: 0
                        if (days > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                strings.fidyeTotalTemplate.replace("{amount}", formatMoney(fitre.amountTry * days, ZakatCurrency.TRY)),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            strings.fitreSourceNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }

                Text(
                    strings.zakatNisabInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )

                if (showAds) AdBannerCard()
                SiteFooter()
            }
        }
    }
}

@Composable
private fun ValueRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AmountField(
    label: String,
    value: String,
    currency: ZakatCurrency? = null,
    unit: String? = currency?.symbol,
    decimal: Boolean = true,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> onChange(new.filter { it.isDigit() || (decimal && (it == '.' || it == ',')) }) },
        label = { Text(label) },
        suffix = unit?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun defaultCurrencyFor(language: AppLanguage): ZakatCurrency = when (language) {
    AppLanguage.TURKISH -> ZakatCurrency.TRY
    AppLanguage.GERMAN, AppLanguage.FRENCH -> ZakatCurrency.EUR
    else -> ZakatCurrency.USD
}

/** Accepts both "1.234,50" (Turkish) and "1234.50"; the last separator is the decimal one. */
private fun String.toAmount(): Double? {
    val s = trim()
    if (s.isEmpty()) return null
    val lastSep = maxOf(s.lastIndexOf(','), s.lastIndexOf('.'))
    val normalized = if (lastSep >= 0 && s.length - lastSep - 1 in 1..2) {
        s.substring(0, lastSep).filter { it.isDigit() } + "." + s.substring(lastSep + 1)
    } else {
        s.filter { it.isDigit() }
    }
    return normalized.toDoubleOrNull()
}

private fun formatNumber(value: Double): String {
    val cents = kotlin.math.round(value * 100).toLong()
    val whole = cents / 100
    val fraction = cents % 100
    return if (fraction == 0L) whole.toString() else "$whole,${fraction.toString().padStart(2, '0')}"
}

/** "12.345,67 ₺" / "12.345,67 €" / "$12,345.67" — grouped, two decimals, in the currency's own style. */
private fun formatMoney(value: Double, currency: ZakatCurrency): String {
    val cents = kotlin.math.round(kotlin.math.abs(value) * 100).toLong()
    val whole = (cents / 100).toString()
    val fraction = (cents % 100).toString().padStart(2, '0')
    val usStyle = currency == ZakatCurrency.USD
    val grouped = whole.reversed().chunked(3).joinToString(if (usStyle) "," else ".").reversed()
    val sign = if (value < 0) "-" else ""
    return if (usStyle) "$sign${currency.symbol}$grouped.$fraction" else "$sign$grouped,$fraction ${currency.symbol}"
}
