package br.com.paivalab.controlapeso.domain.usecase.statistics

import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeasurementStatisticsTest {
    @Test
    fun emptyAndSingleDatasets_areHandled() {
        assertNull(MeasurementStatistics.calculate(emptyList()))
        val stats = requireNotNull(MeasurementStatistics.calculate(listOf(measurement(75.0, 0))))
        assertEquals(0.0, stats.absoluteVariationKg, 0.0)
        assertNull(stats.averageFrequencyDays)
        assertEquals(1, stats.measurementCount)
    }

    @Test
    fun fixedDataset_calculatesSummaryAndMovingAverage() {
        val values = listOf(80.0, 79.0, 78.0, 77.0)
        val stats = requireNotNull(
            MeasurementStatistics.calculate(
                values.mapIndexed { index, value -> measurement(value, index) },
                movingAverageWindow = 3
            )
        )
        assertEquals(-3.0, stats.absoluteVariationKg, 0.0)
        assertEquals(-3.75, stats.percentageVariation ?: 0.0, 0.0001)
        assertEquals(78.5, stats.averageWeightKg, 0.0)
        assertEquals(77.0, stats.minimumWeightKg, 0.0)
        assertEquals(80.0, stats.maximumWeightKg, 0.0)
        assertEquals(78.0, stats.movingAverage.last().averageWeightKg, 0.0)
        assertEquals(1.0, stats.averageFrequencyDays ?: -1.0, 0.0)
    }

    private fun measurement(weight: Double, day: Int): WeightMeasurement {
        val instant = Instant.parse("2026-07-01T12:00:00Z").plusSeconds(day * 86_400L)
        return WeightMeasurement(
            id = "m$day",
            profileId = "p",
            weightKg = weight,
            measuredAt = instant,
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
            createdAt = instant,
            updatedAt = instant
        )
    }
}
