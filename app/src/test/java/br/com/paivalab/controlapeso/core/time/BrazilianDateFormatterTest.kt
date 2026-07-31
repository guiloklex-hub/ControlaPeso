package br.com.paivalab.controlapeso.core.time

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrazilianDateFormatterTest {
    @Test
    fun formatAndParse_useTheBrazilianDayMonthYearOrder() {
        val date = LocalDate.of(2026, 7, 28)

        assertEquals("28/07/2026", BrazilianDateFormatter.format(date))
        assertEquals(date, BrazilianDateFormatter.parse("28/07/2026"))
        assertEquals(date, BrazilianDateFormatter.parse("28072026"))
        assertEquals(date, BrazilianDateFormatter.parse("28-07-2026"))
    }

    @Test
    fun mask_insertsSeparatorsAndConvertsAnIsoPaste() {
        assertEquals("28/07/2026", BrazilianDateFormatter.mask("28072026"))
        assertEquals("28/07/2026", BrazilianDateFormatter.mask("28/07/2026"))
        assertEquals("28/07/2026", BrazilianDateFormatter.mask("2026-07-28"))
        assertEquals("28072026", BrazilianDateFormatter.inputDigits("2026-07-28"))
    }

    @Test
    fun invalidDate_returnsNullWithoutThrowing() {
        assertNull(BrazilianDateFormatter.parseOrNull("31/02/2026"))
    }
}
