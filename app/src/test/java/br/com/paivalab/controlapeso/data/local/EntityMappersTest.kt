package br.com.paivalab.controlapeso.data.local

import br.com.paivalab.controlapeso.data.local.mapper.toDomain
import br.com.paivalab.controlapeso.data.local.mapper.toEntity
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntityMappersTest {
    @Test
    fun measurement_roundTripPreservesRawPayloadAndNullMetrics() {
        val now = Instant.parse("2026-07-27T21:30:00Z")
        val original = WeightMeasurement(
            id = "measurement-id",
            profileId = "profile-id",
            weightKg = 103.8,
            measuredAt = now,
            zoneOffsetSeconds = -10_800,
            source = MeasurementSource.BLE,
            isStable = true,
            deviceId = "device-id",
            deviceName = "Yoda1",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            note = null,
            rawPayloadHex = "C0 68 28 8C",
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

        val result = original.toEntity().toDomain()

        assertEquals(original, result)
        assertEquals("C0 68 28 8C", result.rawPayloadHex)
        assertNull(result.bodyFatPercent)
    }

    @Test
    fun measurementWithoutProfile_roundTrips() {
        val now = Instant.parse("2026-07-27T21:30:00Z")
        val original = WeightMeasurement(
            id = "unassigned-measurement",
            profileId = null,
            weightKg = 75.0,
            measuredAt = now,
            zoneOffsetSeconds = -10_800,
            source = MeasurementSource.MANUAL,
            isStable = true,
            deviceId = null,
            deviceName = null,
            deviceAddress = null,
            note = null,
            rawPayloadHex = null,
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

        assertEquals(original, original.toEntity().toDomain())
    }
}
