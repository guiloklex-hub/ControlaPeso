package br.com.paivalab.controlapeso.ui.settings

import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HealthSyncPreviewCalculatorTest {
    @Test
    fun previewIncludesOnlyLocalMeasurementsForTheActiveProfile() {
        val first = measurement(
            id = "first",
            profileId = "active",
            measuredAt = Instant.parse("2026-07-01T10:00:00Z")
        )
        val last = measurement(
            id = "last",
            profileId = "active",
            measuredAt = Instant.parse("2026-07-03T10:00:00Z")
        )
        val otherProfile = measurement("other", "other", first.measuredAt)
        val healthConnect = measurement(
            id = "health",
            profileId = "active",
            measuredAt = Instant.parse("2026-07-04T10:00:00Z"),
            source = MeasurementSource.HEALTH_CONNECT
        )
        val demo = measurement(
            id = "demo",
            profileId = "active",
            measuredAt = Instant.parse("2026-07-05T10:00:00Z"),
            source = MeasurementSource.DEMO
        )

        val preview = HealthSyncPreviewCalculator.preview(
            listOf(last, demo, otherProfile, healthConnect, first),
            profileId = "active"
        )

        assertEquals(2, preview.count)
        assertEquals(first.measuredAt, preview.firstMeasuredAt)
        assertEquals(last.measuredAt, preview.lastMeasuredAt)
    }

    @Test
    fun emptyPreviewHasNoPeriod() {
        val preview = HealthSyncPreviewCalculator.preview(emptyList(), "active")

        assertEquals(0, preview.count)
        assertNull(preview.firstMeasuredAt)
        assertNull(preview.lastMeasuredAt)
    }

    private fun measurement(
        id: String,
        profileId: String?,
        measuredAt: Instant,
        source: MeasurementSource = MeasurementSource.MANUAL
    ) = WeightMeasurement(
        id = id,
        profileId = profileId,
        weightKg = 72.0,
        measuredAt = measuredAt,
        zoneOffsetSeconds = 0,
        source = source,
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
        createdAt = measuredAt,
        updatedAt = measuredAt
    )
}
