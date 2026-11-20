package org.ferdidrgn.hudaquran.ui.travel

import kotlin.test.Test
import kotlin.test.assertEquals

class TravelModeLogicTest {
    @Test
    fun missingDepartureStaysInPlanning() {
        assertEquals(TravelPhase.PLANNING, travelPhase(10_000L, null, null))
    }

    @Test
    fun activeTripBecomesReturningOnTheLastDay() {
        val departure = 100_000L
        val returnAt = departure + 3 * 86_400_000L
        assertEquals(TravelPhase.ACTIVE, travelPhase(departure + 86_400_000L, departure, returnAt))
        assertEquals(TravelPhase.RETURNING, travelPhase(returnAt - 12 * 60 * 60 * 1_000L, departure, returnAt))
    }

    @Test
    fun tripAfterReturnIsComplete() {
        val returnAt = 100_000L
        assertEquals(TravelPhase.COMPLETE, travelPhase(returnAt + 1, 1L, returnAt))
    }
}
