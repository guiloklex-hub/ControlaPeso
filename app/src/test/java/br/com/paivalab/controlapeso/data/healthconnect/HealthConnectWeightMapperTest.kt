package br.com.paivalab.controlapeso.data.healthconnect

import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class HealthConnectWeightMapperTest {
    @Test
    fun `ble record maps weight time offset and id without private fields`() {
        val measurement = measurement(MeasurementSource.BLE)

        val record = HealthConnectWeightMapper.toRecord(measurement)

        assertEquals(75.4, record.weight.inKilograms, 0.0)
        assertEquals(measurement.measuredAt, record.time)
        assertEquals(-10_800, record.zoneOffset?.totalSeconds)
        assertEquals(measurement.id, record.metadata.clientRecordId)
        assertEquals(
            Metadata.RECORDING_METHOD_AUTOMATICALLY_RECORDED,
            record.metadata.recordingMethod
        )
        assertEquals(Device.TYPE_SCALE, requireNotNull(record.metadata.device).type)
        assertFalse(record.toString().contains("C0 68"))
        assertFalse(record.toString().contains("AA:BB"))
        assertFalse(record.toString().contains("privada"))
    }

    @Test
    fun `manual record has no device and uses manual recording method`() {
        val record = HealthConnectWeightMapper.toRecord(
            measurement(MeasurementSource.MANUAL)
        )

        assertEquals(Metadata.RECORDING_METHOD_MANUAL_ENTRY, record.metadata.recordingMethod)
        assertNull(record.metadata.device)
    }

    private fun measurement(source: MeasurementSource): WeightMeasurement {
        val measuredAt = Instant.parse("2026-07-27T15:00:00Z")
        return WeightMeasurement(
            id = "measurement-id",
            profileId = "profile-id",
            weightKg = 75.4,
            measuredAt = measuredAt,
            zoneOffsetSeconds = -10_800,
            source = source,
            isStable = true,
            deviceId = "device-id",
            deviceName = "Yoda1",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            note = "privada",
            rawPayloadHex = "C0 68",
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
}
