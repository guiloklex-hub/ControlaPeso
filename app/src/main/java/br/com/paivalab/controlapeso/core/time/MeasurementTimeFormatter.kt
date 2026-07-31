package br.com.paivalab.controlapeso.core.time

import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Exibe a hora local que foi registrada com a medição. A troca posterior do
 * fuso do telefone não altera silenciosamente o dia ou a hora do histórico.
 */
object MeasurementTimeFormatter {
    fun dateTime(
        measurement: WeightMeasurement,
        fallbackZone: ZoneId = ZoneId.systemDefault()
    ): String = BrazilianDateTimeFormatter.dateTime(
        measurement.measuredAt,
        recordedZone(measurement, fallbackZone)
    )

    fun date(
        measurement: WeightMeasurement,
        fallbackZone: ZoneId = ZoneId.systemDefault()
    ): String = BrazilianDateTimeFormatter.date(
        measurement.measuredAt,
        recordedZone(measurement, fallbackZone)
    )

    fun localDate(
        measurement: WeightMeasurement,
        fallbackZone: ZoneId = ZoneId.systemDefault()
    ): LocalDate =
        measurement.measuredAt.atZone(recordedZone(measurement, fallbackZone)).toLocalDate()

    private fun recordedZone(
        measurement: WeightMeasurement,
        fallbackZone: ZoneId
    ): ZoneId {
        val seconds = measurement.zoneOffsetSeconds ?: return fallbackZone
        return try {
            ZoneOffset.ofTotalSeconds(seconds)
        } catch (_: DateTimeException) {
            fallbackZone
        }
    }
}
