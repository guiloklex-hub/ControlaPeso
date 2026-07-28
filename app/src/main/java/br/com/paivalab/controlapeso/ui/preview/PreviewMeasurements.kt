package br.com.paivalab.controlapeso.ui.preview

import br.com.paivalab.controlapeso.bluetooth.BleWeightReading
import br.com.paivalab.controlapeso.bluetooth.StabilityProgress
import br.com.paivalab.controlapeso.bluetooth.StableWeightEvent
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import java.time.temporal.ChronoUnit

object PreviewMeasurements {
    private val anchor = Instant.parse("2026-07-28T10:30:00Z")

    val history: List<WeightMeasurement> = listOf(
        80.1, 79.8, 79.7, 79.3, 79.5, 79.0, 78.8, 78.6, 78.7, 78.4
    ).mapIndexed { index, weight ->
        val measuredAt = anchor.minus((18L - index * 2L), ChronoUnit.DAYS)
        WeightMeasurement(
            id = "preview-measurement-$index",
            profileId = PreviewProfiles.primary.id,
            weightKg = weight,
            measuredAt = measuredAt,
            zoneOffsetSeconds = -10_800,
            source = if (index % 3 == 0) {
                MeasurementSource.MANUAL
            } else {
                MeasurementSource.BLE
            },
            isStable = index % 3 != 0,
            deviceId = "preview-scale",
            deviceName = "Balança de teste",
            deviceAddress = null,
            note = if (index == 9) "Medição após acordar" else null,
            rawPayloadHex = null,
            impedanceOne = null,
            impedanceTwo = null,
            bodyFatPercent = null,
            muscleMassKg = null,
            bodyWaterPercent = null,
            boneMassKg = null,
            visceralFatLevel = null,
            metabolicAge = null,
            createdAt = measuredAt,
            updatedAt = measuredAt
        )
    }

    val reading = BleWeightReading(
        deviceAddress = "00:00:00:00:00:00",
        deviceName = "Balança de teste",
        advertisedValue = 78.4,
        rawWeight = 7_840,
        property = BleWeightReading.PROPERTY_MEASUREMENT,
        sequenceNumber = 42,
        rssi = -58,
        observedAtEpochMillis = 1_785_236_400_000,
        rawPayloadHex = "00 00 00 00"
    )

    val varyingProgress = StabilityProgress(
        candidateValue = 78.4,
        readingsCollected = 4,
        requiredReadings = 8,
        elapsedMillis = 1_000,
        requiredElapsedMillis = 2_000,
        maximumRange = 0.08,
        stableEvent = null
    )

    val stableEvent = StableWeightEvent(
        reading = reading,
        sampleCount = 8,
        windowDurationMillis = 2_200,
        valueRange = 0.04
    )

    val stableProgress = varyingProgress.copy(
        readingsCollected = 8,
        elapsedMillis = 2_200,
        maximumRange = 0.04,
        stableEvent = stableEvent
    )
}

