package br.com.paivalab.controlapeso.data.export

import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportTextSummaryFormatterTest {
    @Test
    fun `summary contains neutral statistics and disclaimer`() {
        val time = Instant.parse("2026-07-27T12:00:00Z")
        val measurements = listOf(
            measurement("a", 75.0, time),
            measurement("b", 74.0, time.plusSeconds(86_400))
        )
        val data = ReportData(
            profile = profile(time),
            measurements = measurements,
            statistics = MeasurementStatistics.calculate(measurements),
            generatedAt = time,
            startInclusive = null,
            endExclusive = null
        )

        val text = ReportTextSummaryFormatter.format(data, WeightUnit.KILOGRAM)

        assertTrue(text.contains("Inicial: 75,00 kg"))
        assertTrue(text.contains("Final: 74,00 kg"))
        assertTrue(text.contains("Variação: -1,00 kg"))
        assertTrue(text.contains("não é diagnóstico médico"))
    }

    private fun profile(time: Instant) = Profile(
        id = "p",
        name = "Pessoa",
        avatarKey = null,
        heightCm = null,
        birthDate = null,
        preferredWeightUnit = WeightUnit.KILOGRAM,
        healthConnectEnabled = false,
        isActive = true,
        createdAt = time,
        updatedAt = time
    )

    private fun measurement(id: String, kg: Double, time: Instant) = WeightMeasurement(
        id = id,
        profileId = "p",
        weightKg = kg,
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
