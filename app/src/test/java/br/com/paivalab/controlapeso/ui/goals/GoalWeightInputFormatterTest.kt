package br.com.paivalab.controlapeso.ui.goals

import br.com.paivalab.controlapeso.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalWeightInputFormatterTest {
    @Test
    fun formatsStoredKilogramsInTheDisplayedUnit() {
        val formatted = formatGoalWeightInput(75.0, WeightUnit.POUND)

        assertEquals(
            WeightUnit.POUND.fromKilograms(75.0),
            formatted.replace(',', '.').toDouble(),
            0.1
        )
    }

    @Test
    fun kilogramsRemainKilograms() {
        assertEquals("75.0", formatGoalWeightInput(75.0, WeightUnit.KILOGRAM).replace(',', '.'))
    }
}
