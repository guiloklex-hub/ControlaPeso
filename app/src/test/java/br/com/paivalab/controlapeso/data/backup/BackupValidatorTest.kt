package br.com.paivalab.controlapeso.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupValidatorTest {
    @Test
    fun validDocument_roundTripsAndPreservesRawPayload() {
        val document = validDocument()
        val encoded = BackupCodec.encode(document)
        val decoded = BackupCodec.decodeAndValidate(encoded)

        require(decoded is BackupDecodeResult.Valid)
        assertEquals(
            "C0 68 28 8C",
            decoded.document.measurements.single().rawPayloadHex
        )
        assertTrue(BackupValidator.validate(decoded.document).isValid)
    }

    @Test
    fun unknownSchemaDuplicateAndBrokenReference_areRejected() {
        val base = validDocument()
        val invalid = base.copy(
            schemaVersion = 99,
            profiles = base.profiles + base.profiles.single(),
            measurements = base.measurements.map { it.copy(profileId = "missing") }
        )
        val result = BackupValidator.validate(invalid)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("esquema") })
        assertTrue(result.errors.any { it.contains("duplicados") })
        assertTrue(result.errors.any { it.contains("perfil inexistente") })
    }

    @Test
    fun truncatedJson_neverProducesValidPreview() {
        val result = BackupCodec.decodeAndValidate("""{"schemaVersion":1""")
        assertTrue(result is BackupDecodeResult.Invalid)
    }

    @Test
    fun danglingDeviceAndInvalidZoneOffset_areRejected() {
        val base = validDocument()
        val invalid = base.copy(
            measurements = base.measurements.map {
                it.copy(
                    deviceId = "33333333-3333-4333-8333-333333333333",
                    zoneOffsetSeconds = 70_000
                )
            }
        )

        val result = BackupValidator.validate(invalid)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("dispositivo inexistente") })
        assertTrue(result.errors.any { it.contains("fuso horário inválido") })
    }

    private fun validDocument(): BackupDocument {
        val profileId = "11111111-1111-4111-8111-111111111111"
        val measurementId = "22222222-2222-4222-8222-222222222222"
        return BackupDocument(
            exportedAt = "2026-07-27T20:00:00Z",
            profiles = listOf(
                BackupProfile(
                    id = profileId,
                    name = "José",
                    avatarKey = null,
                    heightCm = null,
                    birthDate = null,
                    preferredWeightUnit = "KILOGRAM",
                    healthConnectEnabled = false,
                    isActive = true,
                    createdAt = "2026-07-27T20:00:00Z",
                    updatedAt = "2026-07-27T20:00:00Z"
                )
            ),
            measurements = listOf(
                BackupMeasurement(
                    id = measurementId,
                    profileId = profileId,
                    weightKg = 75.4,
                    measuredAt = "2026-07-27T20:00:00Z",
                    zoneOffsetSeconds = -10_800,
                    source = "BLE",
                    isStable = true,
                    deviceId = null,
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
                    createdAt = "2026-07-27T20:00:00Z",
                    updatedAt = "2026-07-27T20:00:00Z"
                )
            ),
            goals = emptyList(),
            preferences = BackupPreferences(
                themeMode = "SYSTEM",
                dynamicColors = true,
                visualEffects = "FULL",
                defaultWeightUnit = "KILOGRAM",
                autoSaveStableMeasurement = false,
                confirmBeforeSaving = true,
                vibrationEnabled = true,
                soundEnabled = false,
                defaultHistoryPeriod = "DAYS_30",
                movingAverageEnabled = true,
                historyGrouping = "NONE",
                chartSize = "COMFORTABLE",
                highContrast = false,
                remindersEnabled = false,
                reminderDaysMask = 127,
                reminderHour = 9,
                reminderMinute = 0
            )
        )
    }
}
