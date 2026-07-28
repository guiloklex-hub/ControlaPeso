package br.com.paivalab.controlapeso.data.export

import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.nio.charset.StandardCharsets
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class CsvExportService {
    fun generate(
        data: ReportData,
        unit: WeightUnit,
        includeNotes: Boolean,
        includeAdditionalMetrics: Boolean
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
            if (includeAdditionalMetrics) {
                addAll(
                    listOf(
                        "impedancia_1",
                        "impedancia_2",
                        "gordura_corporal",
                        "massa_muscular_${unit.symbol}",
                        "agua_corporal",
                        "massa_ossea_${unit.symbol}",
                        "gordura_visceral",
                        "idade_metabolica"
                    )
                )
            }
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
                        includeAdditionalMetrics = includeAdditionalMetrics
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
        includeAdditionalMetrics: Boolean
    ): List<String> = buildList {
        val offset = measurement.zoneOffsetSeconds
            ?.let(ZoneOffset::ofTotalSeconds)
            ?: ZoneId.systemDefault().rules.getOffset(measurement.measuredAt)
        val local = measurement.measuredAt.atOffset(offset)
        add(measurement.id)
        add(safeSpreadsheetText(profileName))
        add(decimal(unit.fromKilograms(measurement.weightKg)))
        add(unit.symbol)
        add(local.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
        add(local.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME))
        add(offset.id)
        add(measurement.source.name)
        add(safeSpreadsheetText(measurement.deviceName.orEmpty()))
        add(if (measurement.isStable) "sim" else "não")
        if (includeNotes) add(safeSpreadsheetText(measurement.note.orEmpty()))
        if (includeAdditionalMetrics) {
            add(nullableDecimal(measurement.impedanceOne))
            add(nullableDecimal(measurement.impedanceTwo))
            add(nullableDecimal(measurement.bodyFatPercent))
            add(nullableDecimal(measurement.muscleMassKg?.let(unit::fromKilograms)))
            add(nullableDecimal(measurement.bodyWaterPercent))
            add(nullableDecimal(measurement.boneMassKg?.let(unit::fromKilograms)))
            add(nullableDecimal(measurement.visceralFatLevel))
            add(measurement.metabolicAge?.toString().orEmpty())
        }
    }

    private fun decimal(value: Double): String =
        String.format(PORTUGUESE_BRAZIL, "%.2f", value)

    private fun nullableDecimal(value: Double?): String =
        value?.let(::decimal).orEmpty()

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
