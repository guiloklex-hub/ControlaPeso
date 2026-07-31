package br.com.paivalab.controlapeso.domain.usecase.measurement

import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.repository.GoalRepository
import br.com.paivalab.controlapeso.domain.repository.MeasurementRepository
import java.time.ZoneId
import kotlinx.coroutines.flow.first

sealed interface CreateMeasurementResult {
    data class Saved(val measurement: WeightMeasurement) : CreateMeasurementResult
    data class Invalid(val errors: List<ManualValidationError>) : CreateMeasurementResult
    data class ProbableDuplicate(
        val proposed: WeightMeasurement,
        val existing: WeightMeasurement
    ) : CreateMeasurementResult
}

class CreateManualMeasurement(
    private val validator: ManualMeasurementValidator,
    private val measurementRepository: MeasurementRepository,
    private val goalRepository: GoalRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val clock: AppClock,
    private val idGenerator: IdGenerator,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    suspend operator fun invoke(
        input: ManualMeasurementInput,
        existingId: String? = null,
        acceptProbableDuplicate: Boolean = false
    ): CreateMeasurementResult {
        val validated = when (val result = validator.validate(input)) {
            is ManualValidationResult.Invalid ->
                return CreateMeasurementResult.Invalid(result.errors)
            is ManualValidationResult.Valid -> result.value
        }
        val now = clock.now()
        val existing = existingId?.let { measurementRepository.findById(it) }
        val proposed = WeightMeasurement(
            id = existing?.id ?: idGenerator.newId(),
            profileId = validated.profileId,
            weightKg = validated.weightKg,
            measuredAt = validated.measuredAt,
            zoneOffsetSeconds = validated.zoneOffsetSeconds,
            source = existing?.source ?: MeasurementSource.MANUAL,
            isStable = existing?.isStable ?: true,
            deviceId = existing?.deviceId,
            deviceName = existing?.deviceName,
            deviceAddress = existing?.deviceAddress,
            note = validated.note,
            rawPayloadHex = existing?.rawPayloadHex,
            impedanceOne = existing?.impedanceOne,
            impedanceTwo = existing?.impedanceTwo,
            bodyFatPercent = existing?.bodyFatPercent,
            muscleMassKg = existing?.muscleMassKg,
            bodyWaterPercent = existing?.bodyWaterPercent,
            boneMassKg = existing?.boneMassKg,
            visceralFatLevel = existing?.visceralFatLevel,
            metabolicAge = existing?.metabolicAge,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )

        if (existing == null && !acceptProbableDuplicate) {
            val duplicate = measurementRepository.findProbableDuplicate(
                profileId = proposed.profileId,
                weightKg = proposed.weightKg,
                measuredAt = proposed.measuredAt,
                deviceAddress = null
            )
            if (duplicate != null) {
                return CreateMeasurementResult.ProbableDuplicate(proposed, duplicate)
            }
        }

        if (existing == null) measurementRepository.insert(proposed)
        else measurementRepository.update(proposed)
        createPendingGoalIfNeeded(proposed)
        return CreateMeasurementResult.Saved(proposed)
    }

    private suspend fun createPendingGoalIfNeeded(measurement: WeightMeasurement) {
        val preferences = preferencesRepository.preferences.first()
        val targetKg = preferences.pendingGoalTargetKg ?: return
        val profileId = measurement.profileId ?: return
        if (goalRepository.observeForProfile(profileId).first().isEmpty()) {
            val now = clock.now()
            goalRepository.insert(
                WeightGoal(
                    id = idGenerator.newId(),
                    profileId = profileId,
                    startWeightKg = measurement.weightKg,
                    targetWeightKg = targetKg,
                    startDate = MeasurementTimeFormatter.localDate(
                        measurement,
                        fallbackZone = zoneId
                    ),
                    targetDate = null,
                    status = GoalStatus.ACTIVE,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        preferencesRepository.setPendingGoalTargetKg(null)
    }
}
