package br.com.paivalab.controlapeso.core.time

import java.time.Instant

object SystemAppClock : AppClock {
    override fun now(): Instant = Instant.now()
}
