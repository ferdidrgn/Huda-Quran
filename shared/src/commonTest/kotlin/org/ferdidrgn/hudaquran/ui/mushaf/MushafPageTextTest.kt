package org.ferdidrgn.hudaquran.ui.mushaf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

class MushafPageTextTest {
    @Test
    fun normalProfileKeepsTheMeasuredFit() {
        assertEquals(28f, scaledMushafFontSize(28f, 1f, minSp = 17, maxSp = 34))
    }

    @Test
    fun elderlyScaleIsNotSilentlyReducedToTheFitSize() {
        assertClose(36.4f, scaledMushafFontSize(28f, 1.3f, minSp = 17, maxSp = 34))
    }

    @Test
    fun fittedSizeStillRespectsProfileBounds() {
        assertClose(22.1f, scaledMushafFontSize(10f, 1.3f, minSp = 17, maxSp = 34))
        assertClose(44.2f, scaledMushafFontSize(40f, 1.3f, minSp = 17, maxSp = 34))
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertTrue(abs(expected - actual) < 0.001f, "expected=$expected actual=$actual")
    }
}
