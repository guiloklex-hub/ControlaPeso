package br.com.paivalab.controlapeso.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class BrazilianDateTimeFormatterTest {
    @Test
    fun visibleFormats_areFixedAndLocaleIndependent() {
        assertEquals(
            "28/07/2026",
            BrazilianDateTimeFormatter.date(LocalDate.of(2026, 7, 28))
        )
        assertEquals(
            "07:05",
            BrazilianDateTimeFormatter.time(LocalTime.of(7, 5))
        )
        assertEquals(
            "27/07/2026 · 22:30",
            BrazilianDateTimeFormatter.dateTime(
                Instant.parse("2026-07-28T01:30:00Z"),
                ZoneId.of("America/Sao_Paulo")
            )
        )
    }
}
