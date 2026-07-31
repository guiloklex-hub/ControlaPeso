package br.com.paivalab.controlapeso.domain.usecase.measurement

import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class ManualMeasurementInput(
    val weightText: String,
    val dateText: String,
    val timeText: String,
    val unit: WeightUnit,
    val profileId: String?,
    val note: String
)

data class ValidatedManualMeasurement(
    val weightKg: Double,
    val measuredAt: Instant,
    val zoneOffsetSeconds: Int,
    val profileId: String?,
    val note: String?
)

sealed interface ManualValidationResult {
    data class Valid(val value: ValidatedManualMeasurement) : ManualValidationResult
    data class Invalid(val errors: List<ManualValidationError>) : ManualValidationResult
}

enum class ManualValidationError {
    PROFILE_REQUIRED,
    WEIGHT_REQUIRED,
    INVALID_WEIGHT,
    WEIGHT_OUT_OF_RANGE,
    INVALID_DATE,
    INVALID_TIME,
    FUTURE_DATE,
    NOTE_TOO_LONG
}

class ManualMeasurementValidator(
    private val clock: AppClock,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    fun validate(input: ManualMeasurementInput): ManualValidationResult {
        val errors = mutableListOf<ManualValidationError>()
        val normalizedWeight = input.weightText.trim().replace(',', '.')
        val weightValue = normalizedWeight.toDoubleOrNull()
        when {
            normalizedWeight.isEmpty() -> errors += ManualValidationError.WEIGHT_REQUIRED
            weightValue == null || !weightValue.isFinite() ->
                errors += ManualValidationError.INVALID_WEIGHT
            input.unit.toKilograms(weightValue) !in MINIMUM_WEIGHT_KG..MAXIMUM_WEIGHT_KG ->
                errors += ManualValidationError.WEIGHT_OUT_OF_RANGE
        }

        val date = try {
            BrazilianDateFormatter.parse(input.dateText)
        } catch (_: DateTimeException) {
            errors += ManualValidationError.INVALID_DATE
            null
        }
        val time = try {
            LocalTime.parse(input.timeText.trim())
        } catch (_: DateTimeException) {
            errors += ManualValidationError.INVALID_TIME
            null
        }
        val measuredDateTime = if (date != null && time != null) {
            LocalDateTime.of(date, time).atZone(zoneId)
        } else {
            null
        }
        if (measuredDateTime?.toInstant()?.isAfter(clock.now()) == true) {
            errors += ManualValidationError.FUTURE_DATE
        }
        val note = input.note.trim().takeIf(String::isNotEmpty)
        if ((note?.length ?: 0) > MAXIMUM_NOTE_LENGTH) {
            errors += ManualValidationError.NOTE_TOO_LONG
        }

        if (errors.isNotEmpty()) return ManualValidationResult.Invalid(errors.distinct())
        requireNotNull(weightValue)
        requireNotNull(measuredDateTime)
        return ManualValidationResult.Valid(
            ValidatedManualMeasurement(
                weightKg = input.unit.toKilograms(weightValue),
                measuredAt = measuredDateTime.toInstant(),
                zoneOffsetSeconds = measuredDateTime.offset.totalSeconds,
                profileId = input.profileId,
                note = note
            )
        )
    }

    companion object {
        const val MINIMUM_WEIGHT_KG = 2.0
        const val MAXIMUM_WEIGHT_KG = 500.0
        const val MAXIMUM_NOTE_LENGTH = 500
    }
}
