package br.com.paivalab.controlapeso.data.export

import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.core.time.BrazilianDateTimeFormatter
import java.nio.charset.StandardCharsets
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

class CsvExportService {
    fun generate(
        data: ReportData,
        unit: WeightUnit,
        includeNotes: Boolean,
        @Suppress("UNUSED_PARAMETER") includeAdditionalMetrics: Boolean
    ): ByteArray {
        val columns = buildList {
            addAll(
                listOf(
                    "id",
                    "perfil",
                    "peso_${unit.symbol}",
                    "unidade",
                    "data",
                    "hora",
                    "offset_utc",
                    "origem",
                    "dispositivo",
                    "estavel"
                )
            )
            if (includeNotes) add("observacao")
        }
        val csv = buildString {
            append(columns.joinToString(SEPARATOR))
            append("\r\n")
            data.measurements.sortedBy(WeightMeasurement::measuredAt).forEach { measurement ->
                append(
                    row(
                        measurement = measurement,
                        profileName = data.profile.name,
                        unit = unit,
                        includeNotes = includeNotes,
                        includeAdditionalMetrics = false
                    ).joinToString(SEPARATOR, transform = ::escape)
                )
                append("\r\n")
            }
        }
        return csv.toByteArray(StandardCharsets.UTF_8)
    }

    private fun row(
        measurement: WeightMeasurement,
        profileName: String,
        unit: WeightUnit,
        includeNotes: Boolean,
        @Suppress("UNUSED_PARAMETER") includeAdditionalMetrics: Boolean
    ): List<String> = buildList {
        val offset = measurement.zoneOffsetSeconds
            ?.let(ZoneOffset::ofTotalSeconds)
            ?: ZoneId.systemDefault().rules.getOffset(measurement.measuredAt)
        val local = measurement.measuredAt.atOffset(offset)
        add(measurement.id)
        add(safeSpreadsheetText(profileName))
        add(decimal(unit.fromKilograms(measurement.weightKg)))
        add(unit.symbol)
        add(BrazilianDateTimeFormatter.date(local.toLocalDate()))
        add(BrazilianDateTimeFormatter.time(local.toLocalTime()))
        add(offset.id)
        add(measurement.source.name)
        add(safeSpreadsheetText(measurement.deviceName.orEmpty()))
        add(if (measurement.isStable) "sim" else "não")
        if (includeNotes) add(safeSpreadsheetText(measurement.note.orEmpty()))
    }

    private fun decimal(value: Double): String =
        String.format(PORTUGUESE_BRAZIL, "%.2f", value)

    private fun escape(value: String): String {
        if (
            SEPARATOR in value ||
            '"' in value ||
            '\n' in value ||
            '\r' in value
        ) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    private fun safeSpreadsheetText(value: String): String =
        if (value.firstOrNull() in setOf('=', '+', '-', '@', '\t', '\r')) {
            "'$value"
        } else {
            value
        }

    companion object {
        private const val SEPARATOR = ";"
        private val PORTUGUESE_BRAZIL = Locale.forLanguageTag("pt-BR")
    }
}
