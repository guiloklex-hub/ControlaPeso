package br.com.paivalab.controlapeso.domain.usecase.statistics

import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Duration

data class MovingAveragePoint(
    val measurement: WeightMeasurement,
    val averageWeightKg: Double
)

data class WeightStatistics(
    val firstWeightKg: Double,
    val lastWeightKg: Double,
    val absoluteVariationKg: Double,
    val percentageVariation: Double?,
    val averageWeightKg: Double,
    val minimumWeightKg: Double,
    val maximumWeightKg: Double,
    val measurementCount: Int,
    val averageFrequencyDays: Double?,
    val movingAverage: List<MovingAveragePoint>
)

object MeasurementStatistics {
    fun calculate(
        measurements: List<WeightMeasurement>,
        movingAverageWindow: Int = 7
    ): WeightStatistics? {
        if (measurements.isEmpty()) return null
        val sorted = measurements.sortedWith(compareBy(WeightMeasurement::measuredAt, WeightMeasurement::id))
        val first = sorted.first().weightKg
        val last = sorted.last().weightKg
        val variation = last - first
        val frequency = if (sorted.size >= 2) {
            val totalDays = Duration.between(
                sorted.first().measuredAt,
                sorted.last().measuredAt
            ).toMillis() / MILLIS_PER_DAY
            totalDays / (sorted.size - 1)
        } else {
            null
        }
        val moving = sorted.mapIndexed { index, measurement ->
            val start = (index - movingAverageWindow + 1).coerceAtLeast(0)
            val window = sorted.subList(start, index + 1)
            MovingAveragePoint(
                measurement = measurement,
                averageWeightKg = window.map(WeightMeasurement::weightKg).average()
            )
        }
        return WeightStatistics(
            firstWeightKg = first,
            lastWeightKg = last,
            absoluteVariationKg = variation,
            percentageVariation = first.takeUnless { it == 0.0 }?.let {
                variation / it * 100.0
            },
            averageWeightKg = sorted.map(WeightMeasurement::weightKg).average(),
            minimumWeightKg = sorted.minOf(WeightMeasurement::weightKg),
            maximumWeightKg = sorted.maxOf(WeightMeasurement::weightKg),
            measurementCount = sorted.size,
            averageFrequencyDays = frequency,
            movingAverage = moving
        )
    }

    private const val MILLIS_PER_DAY = 86_400_000.0
}
