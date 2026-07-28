package br.com.paivalab.controlapeso.worker

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPolicyTest {
    @Test
    fun `maps monday to first bit and sunday to last bit`() {
        assertTrue(ReminderPolicy.isSelected(0b0000001, DayOfWeek.MONDAY))
        assertTrue(ReminderPolicy.isSelected(0b1000000, DayOfWeek.SUNDAY))
        assertFalse(ReminderPolicy.isSelected(0b0000001, DayOfWeek.TUESDAY))
    }

    @Test
    fun `returns delay to next selected occurrence`() {
        val now = ZonedDateTime.of(
            2026, 7, 27, 8, 30, 0, 0,
            ZoneId.of("America/Sao_Paulo")
        )

        val delay = ReminderPolicy.delayUntilNext(
            now = now,
            daysMask = 0b0000001,
            hour = 9,
            minute = 0
        )

        assertEquals(30, delay.toMinutes())
    }
}
