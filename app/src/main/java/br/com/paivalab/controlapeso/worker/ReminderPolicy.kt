package br.com.paivalab.controlapeso.worker

import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime

object ReminderPolicy {
    fun isSelected(daysMask: Int, dayOfWeek: DayOfWeek): Boolean {
        val bit = 1 shl (dayOfWeek.value - 1)
        return daysMask and bit != 0
    }

    fun delayUntilNext(
        now: ZonedDateTime,
        daysMask: Int,
        hour: Int,
        minute: Int
    ): Duration {
        val safeMask = daysMask and 0b1111111
        if (safeMask == 0) return Duration.ofDays(1)
        for (offset in 0..7) {
            val candidate = now.toLocalDate()
                .plusDays(offset.toLong())
                .atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
                .atZone(now.zone)
            if (
                isSelected(safeMask, candidate.dayOfWeek) &&
                candidate.isAfter(now)
            ) {
                return Duration.between(now, candidate)
            }
        }
        return Duration.ofDays(1)
    }
}
