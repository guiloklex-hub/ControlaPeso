package br.com.paivalab.controlapeso.ui.history

import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object HistoryDateRangeCalculator {
    fun bounds(
        filters: HistoryFilters,
        today: LocalDate,
        zoneId: ZoneId
    ): Pair<Instant, Instant>? {
        if (filters.range == HistoryRange.ALL) return null
        val (start, endExclusive) = when (filters.range) {
            HistoryRange.DAYS_7 -> today.minusDays(6) to today.plusDays(1)
            HistoryRange.DAYS_30 -> today.minusDays(29) to today.plusDays(1)
            HistoryRange.MONTHS_3 -> today.minusMonths(3) to today.plusDays(1)
            HistoryRange.MONTHS_6 -> today.minusMonths(6) to today.plusDays(1)
            HistoryRange.YEAR_1 -> today.minusYears(1) to today.plusDays(1)
            HistoryRange.CUSTOM -> {
                try {
                    val start = BrazilianDateFormatter.parse(filters.customStartText)
                    val end = BrazilianDateFormatter.parse(filters.customEndText)
                    if (end < start) return null
                    start to end.plusDays(1)
                } catch (_: DateTimeException) {
                    return null
                }
            }
            HistoryRange.ALL -> return null
        }
        return start.atStartOfDay(zoneId).toInstant() to
            endExclusive.atStartOfDay(zoneId).toInstant()
    }
}
