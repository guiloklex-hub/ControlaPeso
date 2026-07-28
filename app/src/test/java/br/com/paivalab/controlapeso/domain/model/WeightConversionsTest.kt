package br.com.paivalab.controlapeso.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightConversionsTest {
    @Test
    fun kilogramsAndPounds_roundTripWithoutChangingStorageValue() {
        val pounds = WeightUnit.POUND.fromKilograms(75.0)
        assertEquals(165.346696635, pounds, 0.000000001)
        assertEquals(75.0, WeightUnit.POUND.toKilograms(pounds), 0.000000001)
        assertEquals(75.0, WeightUnit.KILOGRAM.toKilograms(75.0), 0.0)
    }
}
