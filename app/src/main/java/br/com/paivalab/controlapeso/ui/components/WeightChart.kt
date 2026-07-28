package br.com.paivalab.controlapeso.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.statistics.MovingAveragePoint
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem

@Composable
fun WeightChart(
    measurements: List<WeightMeasurement>,
    unit: WeightUnit,
    modifier: Modifier = Modifier,
    movingAverage: List<MovingAveragePoint> = emptyList(),
    selectedId: String? = null,
    onSelect: (WeightMeasurement) -> Unit = {},
    height: Dp = 240.dp
) {
    val points = remember(measurements) { downsampleMeasurements(measurements) }
    if (points.isEmpty()) {
        Text(stringResource(R.string.chart_empty))
        return
    }
    val displayValues = points.map { unit.fromKilograms(it.weightKg) }
    val actualMinimum = displayValues.min()
    val actualMaximum = displayValues.max()
    val actualRange = actualMaximum - actualMinimum
    val axisRange = maxOf(actualRange, MINIMUM_AXIS_SPAN)
    val center = (actualMinimum + actualMaximum) / 2.0
    val axisMinimum = center - axisRange / 2.0
    val axisMaximum = center + axisRange / 2.0
    val summary = pluralStringResource(
        R.plurals.chart_accessibility_summary,
        points.size,
        points.size,
        displayValues.first(),
        displayValues.last(),
        unit.symbol,
        axisMinimum,
        axisMaximum
    )
    val lineColor = ControlaPesoDesignSystem.colors.chartPrimary
    val movingColor = ControlaPesoDesignSystem.colors.chartAverage
    val gridColor = ControlaPesoDesignSystem.colors.chartGrid
    val selectedColor = MaterialTheme.colorScheme.secondary
    val movingById = remember(movingAverage) {
        movingAverage.associate { it.measurement.id to it.averageWeightKg }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = summary },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.chart_axis_max, axisMaximum, unit.symbol))
            Text(stringResource(R.string.chart_axis_span, axisRange, unit.symbol))
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        val ratio = (offset.x / width).coerceIn(0f, 1f)
                        val index = (ratio * (points.size - 1)).toInt()
                        onSelect(points[index])
                    }
                }
        ) {
            repeat(5) { index ->
                val y = size.height * index / 4f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            fun offsetFor(index: Int, valueKg: Double): Offset {
                val x = if (points.size == 1) {
                    size.width / 2f
                } else {
                    size.width * index / (points.size - 1f)
                }
                val displayed = unit.fromKilograms(valueKg)
                val normalized = ((displayed - axisMinimum) / axisRange).coerceIn(0.0, 1.0)
                return Offset(x, size.height * (1f - normalized.toFloat()))
            }

            val path = Path()
            points.forEachIndexed { index, measurement ->
                val offset = offsetFor(index, measurement.weightKg)
                if (index == 0) path.moveTo(offset.x, offset.y)
                else path.lineTo(offset.x, offset.y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            if (movingById.isNotEmpty()) {
                val movingPath = Path()
                points.forEachIndexed { index, measurement ->
                    val average = movingById[measurement.id] ?: return@forEachIndexed
                    val offset = offsetFor(index, average)
                    if (index == 0) movingPath.moveTo(offset.x, offset.y)
                    else movingPath.lineTo(offset.x, offset.y)
                }
                drawPath(
                    movingPath,
                    color = movingColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            points.forEachIndexed { index, measurement ->
                drawCircle(
                    color = if (measurement.id == selectedId) selectedColor else lineColor,
                    radius = if (measurement.id == selectedId) 6.dp.toPx() else 3.dp.toPx(),
                    center = offsetFor(index, measurement.weightKg)
                )
            }
        }
        Text(stringResource(R.string.chart_axis_min, axisMinimum, unit.symbol))
        val selected = points.firstOrNull { it.id == selectedId }
        selected?.let {
            Text(
                stringResource(
                    R.string.chart_selected,
                    unit.fromKilograms(it.weightKg),
                    unit.symbol,
                    MeasurementTimeFormatter.date(it)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

internal fun downsampleMeasurements(
    input: List<WeightMeasurement>,
    maximumPoints: Int = 400
): List<WeightMeasurement> {
    if (input.size <= maximumPoints || maximumPoints < 2) return input
    val lastIndex = input.lastIndex
    return List(maximumPoints) { outputIndex ->
        val sourceIndex = (outputIndex.toLong() * lastIndex / (maximumPoints - 1))
            .toInt()
        input[sourceIndex]
    }
}

private const val MINIMUM_AXIS_SPAN = 2.0
