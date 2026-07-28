package br.com.paivalab.controlapeso.data.export

import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import java.text.DateFormat
import java.util.Date
import java.util.Locale

object ReportTextSummaryFormatter {
    fun format(data: ReportData, unit: WeightUnit): String {
        val statistics = data.statistics
        return buildString {
            appendLine("Controla Peso")
            appendLine("Resumo de ${data.profile.name}")
            appendLine("${data.measurements.size} medições")
            if (data.measurements.any { it.source == MeasurementSource.DEMO }) {
                appendLine("ATENÇÃO: inclui dados falsos de demonstração.")
            }
            if (statistics != null) {
                appendLine(
                    "Inicial: ${number(unit.fromKilograms(statistics.firstWeightKg))} " +
                        unit.symbol
                )
                appendLine(
                    "Final: ${number(unit.fromKilograms(statistics.lastWeightKg))} " +
                        unit.symbol
                )
                appendLine(
                    "Variação: ${signedNumber(
                        unit.fromKilograms(statistics.absoluteVariationKg)
                    )} ${unit.symbol}"
                )
                appendLine(
                    "Média: ${number(unit.fromKilograms(statistics.averageWeightKg))} " +
                        unit.symbol
                )
            }
            append(
                "Gerado em " +
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date.from(data.generatedAt))
            )
            appendLine()
            append("Relatório informativo; não é diagnóstico médico.")
        }
    }

    private fun number(value: Double) =
        String.format(Locale.forLanguageTag("pt-BR"), "%.1f", value)

    private fun signedNumber(value: Double) =
        String.format(Locale.forLanguageTag("pt-BR"), "%+.1f", value)
}
