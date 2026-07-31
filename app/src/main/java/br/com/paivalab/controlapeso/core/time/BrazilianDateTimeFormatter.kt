package br.com.paivalab.controlapeso.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Presentation-only formats used by the app. Domain, Room and backup values
 * continue to use their existing technical representations.
 */
object BrazilianDateTimeFormatter {
    const val DATE_PATTERN: String = "dd/MM/uuuu"
    const val DATE_TIME_PATTERN: String = "dd/MM/uuuu · HH:mm"
    const val TIME_PATTERN: String = "HH:mm"

    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.ROOT)
    private val dateTimeFormatter =
        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, Locale.ROOT)
    private val timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN, Locale.ROOT)

    fun date(value: LocalDate): String = value.format(dateFormatter)

    fun date(value: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        date(value.atZone(zone).toLocalDate())

    fun dateTime(value: ZonedDateTime): String = value.format(dateTimeFormatter)

    fun dateTime(value: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        dateTime(value.atZone(zone))

    fun time(value: LocalTime): String = value.format(timeFormatter)
}
