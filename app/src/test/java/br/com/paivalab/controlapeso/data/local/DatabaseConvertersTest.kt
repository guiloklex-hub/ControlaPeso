package br.com.paivalab.controlapeso.data.local

import br.com.paivalab.controlapeso.data.local.converter.DatabaseConverters
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatabaseConvertersTest {
    private val converters = DatabaseConverters()

    @Test
    fun values_roundTripExactly() {
        val instant = Instant.parse("2026-07-27T21:30:45.123Z")
        val date = LocalDate.of(2026, 7, 27)
        assertEquals(
            instant,
            converters.epochMillisToInstant(converters.instantToEpochMillis(instant))
        )
        assertEquals(date, converters.epochDayToLocalDate(converters.localDateToEpochDay(date)))
        assertNull(converters.instantToEpochMillis(null))
        assertNull(converters.epochDayToLocalDate(null))
    }
}
