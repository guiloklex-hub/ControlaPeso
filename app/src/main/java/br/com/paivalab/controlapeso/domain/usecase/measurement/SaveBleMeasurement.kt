package br.com.paivalab.controlapeso.domain.usecase.measurement

import br.com.paivalab.controlapeso.bluetooth.StableWeightEvent
import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.repository.GoalRepository
import br.com.paivalab.controlapeso.domain.repository.MeasurementRepository
import br.com.paivalab.controlapeso.domain.repository.ScaleDeviceRepository
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.first

sealed interface SaveBleResult {
    data class Saved(val measurement: WeightMeasurement) : SaveBleResult
    data class ProbableDuplicate(
        val proposed: WeightMeasurement,
        val existing: WeightMeasurement
    ) : SaveBleResult
    data object UnitNotConfirmed : SaveBleResult
    data object InvalidWeight : SaveBleResult
}

class SaveBleMeasurement(
    private val measurementRepository: MeasurementRepository,
    private val deviceRepository: ScaleDeviceRepository,
    private val goalRepository: GoalRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val clock: AppClock,
    private val idGenerator: IdGenerator,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    suspend operator fun invoke(
        event: StableWeightEvent,
        profileId: String?,
        confirmedUnit: WeightUnit?,
        note: String?,
        source: MeasurementSource = MeasurementSource.BLE,
        acceptProbableDuplicate: Boolean = false
    ): SaveBleResult {
        if (confirmedUnit == null) return SaveBleResult.UnitNotConfirmed
        val weightKg = confirmedUnit.toKilograms(event.reading.advertisedValue)
        if (!weightKg.isFinite() || weightKg !in 2.0..500.0) {
            return SaveBleResult.InvalidWeight
        }
        val now = clock.now()
        val measuredAt = Instant.ofEpochMilli(event.reading.observedAtEpochMillis)
        val device = if (source == MeasurementSource.BLE) {
            prepareDevice(event, now)
        } else null
        val zoneOffset = zoneId.rules.getOffset(measuredAt)
        val proposed = WeightMeasurement(
            id = idGenerator.newId(),
            profileId = profileId,
            weightKg = weightKg,
            measuredAt = measuredAt,
            zoneOffsetSeconds = zoneOffset.totalSeconds,
            source = source,
            isStable = true,
            deviceId = device?.id,
            deviceName = if (source == MeasurementSource.DEMO) {
                event.reading.deviceName
            } else {
                device?.displayName
            },
            deviceAddress = device?.bluetoothAddress,
            note = note?.trim()?.takeIf(String::isNotEmpty)?.take(500),
            rawPayloadHex = event.reading.rawPayloadHex,
            impedanceOne = null,
            impedanceTwo = null,
            bodyFatPercent = null,
            muscleMassKg = null,
            bodyWaterPercent = null,
            boneMassKg = null,
            visceralFatLevel = null,
            metabolicAge = null,
            createdAt = now,
            updatedAt = now
        )
        if (!acceptProbableDuplicate) {
            val duplicate = measurementRepository.findProbableDuplicate(
                profileId = profileId,
                weightKg = weightKg,
                measuredAt = measuredAt,
                deviceAddress = device?.bluetoothAddress
            )
            if (duplicate != null) {
                return SaveBleResult.ProbableDuplicate(proposed, duplicate)
            }
        }
        if (source == MeasurementSource.BLE && device != null) {
            persistDevice(device)
        }
        measurementRepository.insert(proposed)
        createPendingGoalIfNeeded(proposed)
        return SaveBleResult.Saved(proposed)
    }

    private suspend fun prepareDevice(
        event: StableWeightEvent,
        now: Instant
    ): ScaleDevice {
        val existing = deviceRepository.findByAddress(event.reading.deviceAddress)
        return ScaleDevice(
            id = existing?.id ?: idGenerator.newId(),
            displayName = event.reading.deviceName ?: existing?.displayName,
            bluetoothAddress = event.reading.deviceAddress,
            protocolName = "OKOK Manufacturer Advertising",
            lastConnectedAt = existing?.lastConnectedAt,
            lastSeenAt = now,
            isPreferred = existing?.isPreferred ?: false,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
    }

    private suspend fun persistDevice(device: ScaleDevice) {
        if (deviceRepository.findByAddress(device.bluetoothAddress)?.id == device.id) {
            deviceRepository.update(device)
        } else {
            deviceRepository.insert(device)
        }
    }

    private suspend fun createPendingGoalIfNeeded(measurement: WeightMeasurement) {
        val targetKg = preferencesRepository.preferences.first().pendingGoalTargetKg ?: return
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
