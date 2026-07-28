package br.com.paivalab.controlapeso.domain.usecase.measurement

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProbableDuplicatePolicyTest {
    private val time = Instant.parse("2026-07-27T12:00:00Z")

    @Test
    fun `matches only nearby weight time and device`() {
        assertTrue(
            ProbableDuplicatePolicy.matches(
                75.0, time, "AA",
                75.04, time.plusSeconds(90), "AA",
                Duration.ofMinutes(2), 0.05
            )
        )
        assertFalse(
            ProbableDuplicatePolicy.matches(
                75.0, time, "AA",
                75.04, time.plusSeconds(121), "AA",
                Duration.ofMinutes(2), 0.05
            )
        )
        assertFalse(
            ProbableDuplicatePolicy.matches(
                75.0, time, "AA",
                75.04, time.plusSeconds(30), "BB",
                Duration.ofMinutes(2), 0.05
            )
        )
    }

    @Test
    fun `manual expected address accepts any candidate device`() {
        assertTrue(
            ProbableDuplicatePolicy.matches(
                75.0, time, null,
                75.0, time, "AA",
                Duration.ofMinutes(2), 0.05
            )
        )
    }
}
