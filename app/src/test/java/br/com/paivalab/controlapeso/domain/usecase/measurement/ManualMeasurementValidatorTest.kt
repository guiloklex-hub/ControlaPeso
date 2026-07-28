package br.com.paivalab.controlapeso.domain.usecase.measurement

import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualMeasurementValidatorTest {
    private val validator = ManualMeasurementValidator(
        clock = AppClock { Instant.parse("2026-07-27T20:00:00Z") },
        zoneId = ZoneId.of("America/Sao_Paulo")
    )

    @Test
    fun decimalComma_isAcceptedAndConvertedToKilograms() {
        val result = validator.validate(validInput(weight = "165,35", unit = WeightUnit.POUND))
        require(result is ManualValidationResult.Valid)
        assertEquals(75.0015, result.value.weightKg, 0.001)
        assertEquals(-10_800, result.value.zoneOffsetSeconds)
    }

    @Test
    fun emptyExtremeFutureAndLongNote_areRejected() {
        val empty = validator.validate(validInput(weight = ""))
        require(empty is ManualValidationResult.Invalid)
        assertTrue(ManualValidationError.WEIGHT_REQUIRED in empty.errors)

        val invalid = validator.validate(
            validInput(
                weight = "700",
                date = "2027-01-01",
                note = "x".repeat(501)
            )
        )
        require(invalid is ManualValidationResult.Invalid)
        assertTrue(ManualValidationError.WEIGHT_OUT_OF_RANGE in invalid.errors)
        assertTrue(ManualValidationError.FUTURE_DATE in invalid.errors)
        assertTrue(ManualValidationError.NOTE_TOO_LONG in invalid.errors)
    }

    @Test
    fun malformedDateAndTime_neverThrow() {
        val result = validator.validate(validInput(date = "27/07/2026", time = "99:99"))
        require(result is ManualValidationResult.Invalid)
        assertTrue(ManualValidationError.INVALID_DATE in result.errors)
        assertTrue(ManualValidationError.INVALID_TIME in result.errors)
    }

    private fun validInput(
        weight: String = "75.0",
        date: String = "2026-07-27",
        time: String = "16:30",
        unit: WeightUnit = WeightUnit.KILOGRAM,
        note: String = ""
    ) = ManualMeasurementInput(
        weightText = weight,
        dateText = date,
        timeText = time,
        unit = unit,
        profileId = "profile",
        note = note
    )
}
