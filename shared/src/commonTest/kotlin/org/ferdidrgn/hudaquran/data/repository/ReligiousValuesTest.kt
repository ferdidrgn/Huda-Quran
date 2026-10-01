package org.ferdidrgn.hudaquran.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class ReligiousValuesTest {

    private val values = ReligiousValues(
        fitre = listOf(
            FitreEntry("2025", 180.0, validFrom = "2025-02-20"),
            FitreEntry("2026", 240.0, validFrom = "2026-02-18"),
        ),
    )

    @Test
    fun currentFitreIsTheLatestAlreadyInForce() {
        assertEquals("2025", values.currentFitre("2026-01-10")?.period)
        assertEquals("2026", values.currentFitre("2026-02-18")?.period)
        assertEquals("2026", values.currentFitre("2026-09-30")?.period)
    }

    @Test
    fun bundledValuesCarryTheDiyanetNisabAndFitre() {
        assertEquals(80.18, BundledReligiousValues.nisabGoldGrams)
        assertEquals(240.0, BundledReligiousValues.currentFitre("2026-09-30")?.amountTry)
    }

    @Test
    fun zakatIsOnlyDueAtOrAboveNisab() {
        assertEquals(0.0, zakatDue(netWealth = 99_999.0, nisab = 100_000.0))
        assertEquals(2_500.0, zakatDue(netWealth = 100_000.0, nisab = 100_000.0))
        assertEquals(0.0, zakatDue(netWealth = -5.0, nisab = 0.0))
    }

    @Test
    fun goldQuoteConvertsPerCurrency() {
        val quote = GoldQuote(gramUsd = 100.0, usdToTry = 40.0, usdToEur = 0.9, fetchedOn = "2026-09-30")
        assertEquals(4_000.0, quote.gramPrice(ZakatCurrency.TRY))
        assertEquals(90.0, quote.gramPrice(ZakatCurrency.EUR))
        assertEquals(100.0, quote.gramPrice(ZakatCurrency.USD))
    }
}
