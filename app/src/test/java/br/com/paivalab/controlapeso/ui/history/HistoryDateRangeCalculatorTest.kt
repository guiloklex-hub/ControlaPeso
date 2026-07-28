package br.com.paivalab.controlapeso.ui.history

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryDateRangeCalculatorTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val today = LocalDate.of(2026, 7, 27)

    @Test
    fun `seven day period is inclusive of today`() {
        val bounds = requireNotNull(
            HistoryDateRangeCalculator.bounds(
                HistoryFilters(range = HistoryRange.DAYS_7),
                today,
                zone
            )
        )

        assertEquals(
            LocalDate.of(2026, 7, 21),
            bounds.first.atZone(zone).toLocalDate()
        )
        assertEquals(
            LocalDate.of(2026, 7, 28),
            bounds.second.atZone(zone).toLocalDate()
        )
    }

    @Test
    fun `custom invalid and all return no bounded query`() {
        assertNull(
            HistoryDateRangeCalculator.bounds(
                HistoryFilters(
                    range = HistoryRange.CUSTOM,
                    customStartText = "2026-07-30",
                    customEndText = "2026-07-20"
                ),
                today,
                zone
            )
        )
        assertNull(
            HistoryDateRangeCalculator.bounds(
                HistoryFilters(range = HistoryRange.ALL),
                today,
                zone
            )
        )
    }

    @Test
    fun `bounds use local midnights across daylight saving transition`() {
        val daylightSavingZone = ZoneId.of("America/New_York")
        val daylightSavingToday = LocalDate.of(2026, 3, 9)

        val bounds = requireNotNull(
            HistoryDateRangeCalculator.bounds(
                HistoryFilters(range = HistoryRange.DAYS_7),
                daylightSavingToday,
                daylightSavingZone
            )
        )

        assertEquals(
            LocalDate.of(2026, 3, 3),
            bounds.first.atZone(daylightSavingZone).toLocalDate()
        )
        assertEquals(
            LocalDate.of(2026, 3, 10),
            bounds.second.atZone(daylightSavingZone).toLocalDate()
        )
        assertEquals(167, java.time.Duration.between(bounds.first, bounds.second).toHours())
    }
}
