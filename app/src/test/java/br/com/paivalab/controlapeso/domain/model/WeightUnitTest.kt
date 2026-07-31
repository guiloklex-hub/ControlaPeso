package br.com.paivalab.controlapeso.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightUnitTest {
    @Test
    fun formatKeepsTwoDecimalPlaces() {
        val formatted = WeightUnit.KILOGRAM.formatFromKilograms(101.45)

        assertTrue(formatted == "101,45 kg" || formatted == "101.45 kg")
        assertTrue(
            WeightUnit.KILOGRAM.formatInputFromKilograms(101.45) == "101,45" ||
                WeightUnit.KILOGRAM.formatInputFromKilograms(101.45) == "101.45"
        )
    }

    @Test
    fun convertInputKeepsThePhysicalWeightWhenUnitChanges() {
        val pounds = WeightUnit.POUND.convertInput("75,4", WeightUnit.KILOGRAM)
        val kilograms = pounds.replace(',', '.').toDouble()

        assertEquals(75.4, WeightUnit.POUND.toKilograms(kilograms), 0.1)
    }

    @Test
    fun convertInputKeepsBlankAndInvalidTextUntouched() {
        assertEquals(
            "",
            WeightUnit.POUND.convertInput("", WeightUnit.KILOGRAM)
        )
        assertEquals(
            "abc",
            WeightUnit.POUND.convertInput("abc", WeightUnit.KILOGRAM)
        )
        assertTrue(
            WeightUnit.KILOGRAM.convertInput("75,4", WeightUnit.KILOGRAM) == "75,4"
        )
    }
}
