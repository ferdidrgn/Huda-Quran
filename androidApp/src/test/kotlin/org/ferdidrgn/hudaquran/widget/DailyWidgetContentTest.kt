package org.ferdidrgn.hudaquran.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyWidgetContentTest {
    @Test
    fun bundledFallbackIsLongerThanTheOriginalTinyPool() {
        assertTrue(DailyWidgetContent.fallbackSize >= 10)
    }

    @Test
    fun fallbackIsStableForTheSameDayAndRotatesAcrossDays() {
        assertEquals(DailyWidgetContent.fallbackForDay(42), DailyWidgetContent.fallbackForDay(42))
        assertNotEquals(DailyWidgetContent.fallbackForDay(1), DailyWidgetContent.fallbackForDay(2))
    }

    @Test
    fun fallbackNeverReturnsAnEmptyCard() {
        (1..DailyWidgetContent.fallbackSize).forEach { day ->
            val item = DailyWidgetContent.fallbackForDay(day)
            assertTrue(item.reference.isNotBlank())
            assertTrue(item.text.isNotBlank())
        }
    }
}
