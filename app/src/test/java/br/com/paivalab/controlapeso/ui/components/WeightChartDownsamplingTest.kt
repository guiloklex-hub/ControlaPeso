package br.com.paivalab.controlapeso.ui.components

import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightChartDownsamplingTest {
    @Test
    fun `limits points and preserves first and last`() {
        val input = (0 until 1_000).map(::measurement)

        val result = downsampleMeasurements(input, maximumPoints = 100)

        assertEquals(100, result.size)
        assertEquals("m0", result.first().id)
        assertEquals("m999", result.last().id)
    }

    private fun measurement(index: Int): WeightMeasurement {
        val time = Instant.EPOCH.plusSeconds(index.toLong())
        return WeightMeasurement(
            id = "m$index",
            profileId = "p",
            weightKg = 70.0 + index / 100.0,
            measuredAt = time,
            zoneOffsetSeconds = 0,
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
            createdAt = time,
            updatedAt = time
        )
    }
}
