package br.com.paivalab.controlapeso.core.time

import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementTimeFormatterTest {
    @Test
    fun recordedOffsetWinsAfterDeviceTimezoneChanges() {
        val measurement = measurement(
            instant = Instant.parse("2026-07-28T01:30:00Z"),
            zoneOffsetSeconds = -10_800
        )

        assertEquals(
            "7/27/26, 10:30 PM",
            MeasurementTimeFormatter.dateTime(
                measurement = measurement,
                dateStyle = FormatStyle.SHORT,
                timeStyle = FormatStyle.SHORT,
                locale = Locale.US,
                fallbackZone = ZoneId.of("Asia/Tokyo")
            ).replace('\u202F', ' ')
        )
        assertEquals(
            java.time.LocalDate.of(2026, 7, 27),
            MeasurementTimeFormatter.localDate(
                measurement,
                fallbackZone = ZoneId.of("Asia/Tokyo")
            )
        )
    }

    @Test
    fun missingOrInvalidOffsetFallsBackWithoutThrowing() {
        val measurement = measurement(
            instant = Instant.parse("2026-07-28T01:30:00Z"),
            zoneOffsetSeconds = 70_000
        )

        assertEquals(
            java.time.LocalDate.of(2026, 7, 28),
            MeasurementTimeFormatter.localDate(
                measurement,
                fallbackZone = ZoneId.of("UTC")
            )
        )
    }

    private fun measurement(
        instant: Instant,
        zoneOffsetSeconds: Int?
    ) = WeightMeasurement(
        id = "measurement",
        profileId = "profile",
        weightKg = 75.4,
        measuredAt = instant,
        zoneOffsetSeconds = zoneOffsetSeconds,
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
