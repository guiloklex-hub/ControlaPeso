package br.com.paivalab.controlapeso.data.export

import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportServiceTest {
    @Test
    fun utf8SemicolonDecimalCommaAndEmptyNullableFields_arePreserved() {
        val instant = Instant.parse("2026-07-27T21:30:00Z")
        val profile = Profile(
            id = "p",
            name = "José; Silva",
            avatarKey = null,
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = instant,
            updatedAt = instant
        )
        val measurement = WeightMeasurement(
            id = "m",
            profileId = "p",
            weightKg = 75.4,
            measuredAt = instant,
            zoneOffsetSeconds = -10_800,
            source = MeasurementSource.MANUAL,
            isStable = true,
            deviceId = null,
            deviceName = null,
            deviceAddress = null,
            note = "linha; \"acentuada\"",
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
        val output = CsvExportService().generate(
            ReportData(profile, listOf(measurement), null, instant, null, null),
            WeightUnit.KILOGRAM,
            includeNotes = true,
            includeAdditionalMetrics = true
        ).toString(Charsets.UTF_8)

        assertTrue(output.startsWith("id;perfil;peso_kg;"))
        assertTrue(output.contains("\"José; Silva\""))
        assertTrue(output.contains("75,40"))
        assertTrue(output.contains("\"linha; \"\"acentuada\"\"\""))
        assertFalse(output.contains("C0 68"))
        assertTrue(output.endsWith("\r\n"))
    }

    @Test
    fun spreadsheetFormulaInUserText_isNeutralized() {
        val instant = Instant.parse("2026-07-27T21:30:00Z")
        val profile = Profile(
            id = "p",
            name = "=CMD()",
            avatarKey = null,
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = instant,
            updatedAt = instant
        )
        val base = WeightMeasurement(
            id = "m",
            profileId = "p",
            weightKg = 75.4,
            measuredAt = instant,
            zoneOffsetSeconds = 0,
            source = MeasurementSource.MANUAL,
            isStable = true,
            deviceId = null,
            deviceName = "@device",
            deviceAddress = null,
            note = "+formula",
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

        val output = CsvExportService().generate(
            ReportData(profile, listOf(base), null, instant, null, null),
            WeightUnit.KILOGRAM,
            includeNotes = true,
            includeAdditionalMetrics = false
        ).toString(Charsets.UTF_8)

        assertTrue(output.contains("'=CMD()"))
        assertTrue(output.contains("'@device"))
        assertTrue(output.contains("'+formula"))
    }
}
