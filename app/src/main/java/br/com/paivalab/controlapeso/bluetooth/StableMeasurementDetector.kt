package br.com.paivalab.controlapeso.bluetooth

import kotlin.math.abs

data class StabilityProgress(
    val candidateValue: Double?,
    val readingsCollected: Int,
    val requiredReadings: Int,
    val elapsedMillis: Long,
    val requiredElapsedMillis: Long,
    val maximumRange: Double?,
    val stableEvent: StableWeightEvent?
) {
    val fraction: Float
        get() {
            val countFraction =
                readingsCollected.toFloat() / requiredReadings.coerceAtLeast(1)
            val timeFraction =
                elapsedMillis.toFloat() / requiredElapsedMillis.coerceAtLeast(1L)
            return minOf(countFraction, timeFraction).coerceIn(0f, 1f)
        }
}

data class StableWeightEvent(
    val reading: BleWeightReading,
    val sampleCount: Int,
    val windowDurationMillis: Long,
    val valueRange: Double
)

/**
 * Detector conservador para anúncios OKOK.
 *
 * Um evento exige várias leituras 0x25 semelhantes durante uma janela. A
 * propriedade 0x25 identifica medição, não estabilidade. O detector libera uma
 * nova sessão após 0x24, peso zero ou lacuna longa.
 */
class StableMeasurementDetector(
    private val minimumReadings: Int = DEFAULT_MINIMUM_READINGS,
    private val minimumDurationMillis: Long = DEFAULT_MINIMUM_DURATION_MILLIS,
    private val maximumRange: Double = DEFAULT_MAXIMUM_RANGE,
    private val maximumFinalDifference: Double = DEFAULT_MAXIMUM_FINAL_DIFFERENCE,
    private val sessionGapMillis: Long = DEFAULT_SESSION_GAP_MILLIS
) {
    private val samples = ArrayDeque<BleWeightReading>()
    private var emittedInSession = false
    private var lastObservationMillis: Long? = null

    fun accept(reading: BleWeightReading): StabilityProgress {
        val previousTime = lastObservationMillis
        if (
            previousTime != null &&
            reading.observedAtEpochMillis - previousTime > sessionGapMillis
        ) {
            reset()
        }
        lastObservationMillis = reading.observedAtEpochMillis

        if (reading.isIdlePacket || reading.advertisedValue <= 0.0) {
            reset()
            lastObservationMillis = reading.observedAtEpochMillis
            return emptyProgress()
        }
        if (!reading.isMeasurementPacket) return currentProgress()
        if (emittedInSession) return currentProgress()

        samples.addLast(reading)
        while (samples.size > MAXIMUM_RETAINED_SAMPLES) {
            samples.removeFirst()
        }

        while (samples.size > 1 && rangeOf(samples) > maximumRange) {
            samples.removeFirst()
        }

        val duration = durationOf(samples)
        val range = rangeOf(samples)
        val finalDifference = if (samples.size >= 2) {
            abs(samples[samples.lastIndex].advertisedValue -
                samples[samples.lastIndex - 1].advertisedValue)
        } else {
            Double.POSITIVE_INFINITY
        }
        val stable = samples.size >= minimumReadings &&
            duration >= minimumDurationMillis &&
            range <= maximumRange &&
            finalDifference <= maximumFinalDifference

        val event = if (stable) {
            emittedInSession = true
            StableWeightEvent(
                reading = reading,
                sampleCount = samples.size,
                windowDurationMillis = duration,
                valueRange = range
            )
        } else {
            null
        }
        return progress(event)
    }

    fun reset() {
        samples.clear()
        emittedInSession = false
        lastObservationMillis = null
    }

    private fun currentProgress(): StabilityProgress = progress(null)

    private fun emptyProgress(): StabilityProgress = StabilityProgress(
        candidateValue = null,
        readingsCollected = 0,
        requiredReadings = minimumReadings,
        elapsedMillis = 0,
        requiredElapsedMillis = minimumDurationMillis,
        maximumRange = null,
        stableEvent = null
    )

    private fun progress(event: StableWeightEvent?): StabilityProgress =
        StabilityProgress(
            candidateValue = samples.lastOrNull()?.advertisedValue,
            readingsCollected = samples.size,
            requiredReadings = minimumReadings,
            elapsedMillis = durationOf(samples),
            requiredElapsedMillis = minimumDurationMillis,
            maximumRange = samples.takeIf { it.isNotEmpty() }?.let(::rangeOf),
            stableEvent = event
        )

    private fun durationOf(readings: Collection<BleWeightReading>): Long {
        val first = readings.firstOrNull()?.observedAtEpochMillis ?: return 0L
        val last = readings.lastOrNull()?.observedAtEpochMillis ?: return 0L
        return (last - first).coerceAtLeast(0L)
    }

    private fun rangeOf(readings: Collection<BleWeightReading>): Double {
        if (readings.isEmpty()) return 0.0
        var minimum = Double.POSITIVE_INFINITY
        var maximum = Double.NEGATIVE_INFINITY
        readings.forEach { reading ->
            minimum = minOf(minimum, reading.advertisedValue)
            maximum = maxOf(maximum, reading.advertisedValue)
        }
        return maximum - minimum
    }

    companion object {
        const val DEFAULT_MINIMUM_READINGS = 8
        const val DEFAULT_MINIMUM_DURATION_MILLIS = 2_000L
        const val DEFAULT_MAXIMUM_RANGE = 0.10
        const val DEFAULT_MAXIMUM_FINAL_DIFFERENCE = 0.05
        const val DEFAULT_SESSION_GAP_MILLIS = 2_000L
        private const val MAXIMUM_RETAINED_SAMPLES = 64
    }
}
