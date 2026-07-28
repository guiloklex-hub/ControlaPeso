package br.com.paivalab.controlapeso.core.id

import java.util.UUID

fun interface IdGenerator {
    fun newId(): String

    companion object {
        val UUID: IdGenerator = IdGenerator { java.util.UUID.randomUUID().toString() }
    }
}
