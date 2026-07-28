package br.com.paivalab.controlapeso.data.export

import androidx.test.platform.app.InstrumentationRegistry
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import java.io.ByteArrayOutputStream
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportServicesInstrumentedTest {
    @Test
    fun pdfIsGeneratedAndSharedWithContentUri() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val instant = Instant.parse("2026-07-27T20:00:00Z")
        val profile = Profile(
            id = "p",
            name = "Teste",
            avatarKey = null,
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = instant,
            updatedAt = instant
        )
        val measurements = (0..80).map { index ->
            WeightMeasurement(
                id = "m$index",
                profileId = "p",
                weightKg = 75.0 + index / 100.0,
                measuredAt = instant.plusSeconds(index * 86_400L),
                zoneOffsetSeconds = 0,
                source = MeasurementSource.MANUAL,
                isStable = true,
                deviceId = null,
                deviceName = null,
                deviceAddress = null,
                note = "Medição $index",
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
        val data = ReportData(
            profile,
            measurements,
            MeasurementStatistics.calculate(measurements),
            instant,
            null,
            null
        )
        val options = ReportOptions("p", null, null, ReportFormat.PDF)
        val output = ByteArrayOutputStream()
        val result = PdfReportService().generate(data, options, output)

        assertTrue(result.pageCount >= 2)
        assertTrue(output.toByteArray().copyOfRange(0, 4).toString(Charsets.US_ASCII) == "%PDF")
        val shared = ShareFileService(context).create(
            "teste.pdf",
            "application/pdf",
            output.toByteArray()
        )
        assertEquals("content", shared.uri.scheme)
        assertTrue(shared.file.exists())
        shared.file.delete()
    }
}
