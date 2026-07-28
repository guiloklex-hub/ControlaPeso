package br.com.paivalab.controlapeso.domain.usecase.measurement

import java.time.Duration
import java.time.Instant
import kotlin.math.abs

object ProbableDuplicatePolicy {
    fun window(center: Instant, duration: Duration): Pair<Instant, Instant> {
        require(!duration.isNegative)
        return center.minus(duration) to center.plus(duration)
    }

    fun matches(
        expectedWeightKg: Double,
        expectedAt: Instant,
        expectedDeviceAddress: String?,
        candidateWeightKg: Double,
        candidateAt: Instant,
        candidateDeviceAddress: String?,
        window: Duration,
        toleranceKg: Double
    ): Boolean {
        if (toleranceKg < 0.0 || window.isNegative) return false
        if (
            expectedDeviceAddress != null &&
            expectedDeviceAddress != candidateDeviceAddress
        ) {
            return false
        }
        return abs(expectedWeightKg - candidateWeightKg) <= toleranceKg &&
            Duration.between(expectedAt, candidateAt).abs() <= window
    }
}
