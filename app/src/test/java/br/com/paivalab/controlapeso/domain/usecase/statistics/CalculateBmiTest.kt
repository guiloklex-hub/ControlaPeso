package br.com.paivalab.controlapeso.domain.usecase.statistics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateBmiTest {
    @Test
    fun validHeight_calculatesWithoutClassification() {
        assertEquals(23.148, CalculateBmi(75.0, 180.0) ?: 0.0, 0.001)
    }

    @Test
    fun absentOrInvalidHeight_returnsNull() {
        assertNull(CalculateBmi(75.0, null))
        assertNull(CalculateBmi(75.0, 0.0))
        assertNull(CalculateBmi(-1.0, 180.0))
    }
}
