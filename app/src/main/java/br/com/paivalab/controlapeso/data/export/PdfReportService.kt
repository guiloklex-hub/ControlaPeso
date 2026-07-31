package br.com.paivalab.controlapeso.data.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import br.com.paivalab.controlapeso.BuildConfig
import br.com.paivalab.controlapeso.core.time.BrazilianDateTimeFormatter
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import java.io.OutputStream
import java.util.Locale

data class PdfReportResult(val pageCount: Int)

class PdfReportService {
    @Synchronized
    fun generate(
        data: ReportData,
        options: ReportOptions,
        output: OutputStream
    ): PdfReportResult {
        val document = PdfDocument()
        try {
            var pageNumber = 0
            var page = startPage(document, ++pageNumber)
            var canvas = page.canvas
            var y = MARGIN

            y = drawHeader(canvas, data, options.unit, y)
            if (options.includeChart && data.measurements.size >= 2) {
                y = drawChart(canvas, data.measurements, options.unit, y)
            }
            if (options.includeTable) {
                y += SECTION_GAP
                y = drawText(canvas, "Medições", y, titlePaint)
                val rows = data.measurements.sortedByDescending(WeightMeasurement::measuredAt)
                rows.forEachIndexed { index, measurement ->
                    val rowLines = tableRow(measurement, options).flatMap {
                        wrapTextLines(it, PAGE_WIDTH - 2 * MARGIN, bodyPaint)
                    }
                    val requiredHeight = rowLines.size * BODY_LINE_HEIGHT + ROW_GAP
                    if (y + requiredHeight > PAGE_HEIGHT - MARGIN) {
                        document.finishPage(page)
                        page = startPage(document, ++pageNumber)
                        canvas = page.canvas
                        y = MARGIN
                        y = drawText(canvas, "Medições — continuação", y, titlePaint)
                    }
                    rowLines.forEach { line ->
                        y = drawText(canvas, line, y, bodyPaint)
                    }
                    if (index != rows.lastIndex) {
                        canvas.drawLine(
                            MARGIN,
                            y,
                            PAGE_WIDTH - MARGIN,
                            y,
                            dividerPaint
                        )
                        y += ROW_GAP
                    }
                }
            }
            if (y + DISCLAIMER_HEIGHT > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                page = startPage(document, ++pageNumber)
                canvas = page.canvas
                y = MARGIN
            }
            drawWrappedText(
                canvas,
                "Este relatório é informativo, não é diagnóstico médico e não substitui " +
                    "avaliação de profissional qualificado.",
                y + SECTION_GAP,
                PAGE_WIDTH - 2 * MARGIN,
                captionPaint
            )
            document.finishPage(page)
            document.writeTo(output)
            return PdfReportResult(pageCount = pageNumber)
        } finally {
            document.close()
        }
    }

    private fun drawHeader(
        canvas: android.graphics.Canvas,
        data: ReportData,
        unit: WeightUnit,
        initialY: Float
    ): Float {
        var y = drawText(canvas, "Controla Peso", initialY, headerPaint)
        y = drawText(canvas, "Relatório de evolução de peso", y, titlePaint)
        y += 6f
        y = drawWrappedText(
            canvas,
            "Perfil: ${data.profile.name}",
            y,
            PAGE_WIDTH - 2 * MARGIN,
            bodyPaint
        )
        if (BuildConfig.DEBUG && data.measurements.any {
                it.source == MeasurementSource.DEMO
            }) {
            y = drawWrappedText(
                canvas,
                "ATENÇÃO: este relatório inclui dados falsos de demonstração.",
                y,
                PAGE_WIDTH - 2 * MARGIN,
                titlePaint
            )
        }
        y = drawText(
            canvas,
            "Gerado em: ${BrazilianDateTimeFormatter.dateTime(data.generatedAt)}",
            y,
            bodyPaint
        )
        val start = data.startInclusive?.let(BrazilianDateTimeFormatter::date)
            ?: "primeiro registro"
        val end = data.endExclusive
            ?.minusNanos(1)
            ?.let(BrazilianDateTimeFormatter::date)
            ?: "último registro"
        y = drawText(canvas, "Período: $start a $end", y, bodyPaint)
        data.statistics?.let { stats ->
            y += SECTION_GAP
            y = drawText(canvas, "Resumo", y, titlePaint)
            y = drawWrappedText(
                canvas,
                "Inicial ${number(unit.fromKilograms(stats.firstWeightKg))} ${unit.symbol} · " +
                    "final ${number(unit.fromKilograms(stats.lastWeightKg))} ${unit.symbol}",
                y,
                PAGE_WIDTH - 2 * MARGIN,
                bodyPaint
            )
            y = drawWrappedText(
                canvas,
                "Variação ${signedNumber(unit.fromKilograms(stats.absoluteVariationKg))} " +
                    "${unit.symbol} · média ${number(unit.fromKilograms(stats.averageWeightKg))} " +
                    unit.symbol,
                y,
                PAGE_WIDTH - 2 * MARGIN,
                bodyPaint
            )
            y = drawWrappedText(
                canvas,
                "Mínimo ${number(unit.fromKilograms(stats.minimumWeightKg))} · máximo " +
                    "${number(unit.fromKilograms(stats.maximumWeightKg))} ${unit.symbol} · " +
                    "${stats.measurementCount} medições",
                y,
                PAGE_WIDTH - 2 * MARGIN,
                bodyPaint
            )
        }
        return y
    }

    private fun drawChart(
        canvas: android.graphics.Canvas,
        measurements: List<WeightMeasurement>,
        unit: WeightUnit,
        initialY: Float
    ): Float {
        val values = measurements.sortedBy(WeightMeasurement::measuredAt)
            .let(::downsampleForPdf)
        var y = initialY + SECTION_GAP
        y = drawText(canvas, "Evolução", y, titlePaint)
        val chartTop = y
        val chartBottom = chartTop + CHART_HEIGHT
        val chartLeft = MARGIN
        val chartRight = PAGE_WIDTH - MARGIN
        repeat(5) { index ->
            val lineY = chartTop + CHART_HEIGHT * index / 4f
            canvas.drawLine(chartLeft, lineY, chartRight, lineY, gridPaint)
        }
        val displayed = values.map { unit.fromKilograms(it.weightKg) }
        val actualMin = displayed.min()
        val actualMax = displayed.max()
        val axisRange = maxOf(actualMax - actualMin, 2.0)
        val center = (actualMin + actualMax) / 2.0
        val axisMin = center - axisRange / 2.0
        val path = Path()
        values.forEachIndexed { index, measurement ->
            val x = if (values.size == 1) {
                (chartLeft + chartRight) / 2f
            } else {
                chartLeft + (chartRight - chartLeft) * index / (values.size - 1f)
            }
            val normalized =
                (unit.fromKilograms(measurement.weightKg) - axisMin) / axisRange
            val pointY = chartBottom - CHART_HEIGHT * normalized.toFloat()
            if (index == 0) path.moveTo(x, pointY) else path.lineTo(x, pointY)
        }
        canvas.drawPath(path, chartPaint)
        canvas.drawText(
            "${number(axisMin + axisRange)} ${unit.symbol}",
            chartLeft,
            chartTop - 4f,
            captionPaint
        )
        canvas.drawText(
            "${number(axisMin)} ${unit.symbol}",
            chartLeft,
            chartBottom + captionPaint.textSize,
            captionPaint
        )
        return chartBottom + captionPaint.textSize + 8f
    }

    private fun tableRow(
        measurement: WeightMeasurement,
        options: ReportOptions
    ): List<String> = buildList {
        add(
            "${MeasurementTimeFormatter.dateTime(measurement)} · " +
                "${number(options.unit.fromKilograms(measurement.weightKg))} " +
                "${options.unit.symbol} · ${displaySourceName(measurement.source)}"
        )
        if (options.includeNotes && !measurement.note.isNullOrBlank()) {
            add("Observação: ${measurement.note.take(140)}")
        }
    }

    private fun displaySourceName(source: MeasurementSource): String =
        if (source == MeasurementSource.DEMO && !BuildConfig.DEBUG) {
            "IMPORT"
        } else {
            source.name
        }

    private fun startPage(document: PdfDocument, number: Int): PdfDocument.Page =
        document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), number)
                .create()
        )

    private fun drawText(
        canvas: android.graphics.Canvas,
        text: String,
        y: Float,
        paint: Paint
    ): Float {
        canvas.drawText(text, MARGIN, y, paint)
        return y + paint.textSize + 7f
    }

    private fun drawWrappedText(
        canvas: android.graphics.Canvas,
        text: String,
        y: Float,
        width: Float,
        paint: Paint
    ): Float {
        var currentY = y
        wrapTextLines(text, width, paint).forEach { line ->
            canvas.drawText(line, MARGIN, currentY, paint)
            currentY += paint.textSize + 4f
        }
        return currentY
    }

    private fun wrapTextLines(text: String, width: Float, paint: Paint): List<String> {
        if (text.isEmpty()) return listOf("")
        val lines = mutableListOf<String>()
        var line = ""
        text.split(' ').forEach { word ->
            var remaining = word
            while (paint.measureText(remaining) > width) {
                var split = remaining.length.coerceAtLeast(1)
                while (split > 1 && paint.measureText(remaining.take(split)) > width) {
                    split--
                }
                if (line.isNotEmpty()) {
                    lines += line
                    line = ""
                }
                lines += remaining.take(split)
                remaining = remaining.drop(split)
            }
            val candidate = when {
                line.isEmpty() -> remaining
                remaining.isEmpty() -> line
                else -> "$line $remaining"
            }
            if (paint.measureText(candidate) > width && line.isNotEmpty()) {
                lines += line
                line = remaining
            } else {
                line = candidate
            }
        }
        if (line.isNotEmpty()) lines += line
        return lines.ifEmpty { listOf("") }
    }

    private fun number(value: Double): String =
        String.format(PORTUGUESE_BRAZIL, "%.2f", value)

    private fun signedNumber(value: Double): String =
        String.format(PORTUGUESE_BRAZIL, "%+.2f", value)

    companion object {
        private const val PAGE_WIDTH = 595f
        private const val PAGE_HEIGHT = 842f
        private const val MARGIN = 42f
        private const val SECTION_GAP = 18f
        private const val CHART_HEIGHT = 170f
        private const val BODY_LINE_HEIGHT = 19f
        private const val ROW_GAP = 8f
        private const val DISCLAIMER_HEIGHT = 52f
        private val PORTUGUESE_BRAZIL = Locale.forLanguageTag("pt-BR")
        private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 106, 101)
            textSize = 28f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(7, 27, 26)
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(23, 48, 46)
            textSize = 11f
        }
        private val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 9f
        }
        private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        private val chartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 106, 101)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
    }
}

internal fun downsampleForPdf(
    measurements: List<WeightMeasurement>,
    maximumPoints: Int = 300
): List<WeightMeasurement> {
    if (measurements.size <= maximumPoints || maximumPoints < 2) return measurements
    return List(maximumPoints) { index ->
        measurements[index.toLong().times(measurements.lastIndex)
            .div(maximumPoints - 1).toInt()]
    }
}
