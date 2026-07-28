package br.com.paivalab.controlapeso.core.time

import java.time.Instant

fun interface AppClock {
    fun now(): Instant
}
