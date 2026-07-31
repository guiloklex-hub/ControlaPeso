package br.com.paivalab.controlapeso.buildvariant

import android.content.Context
import br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource

/**
 * A variante usada por testes instrumentados não disponibiliza uma fonte
 * executável de teste nem rota de demonstração.
 */
object BleSourceFactory {
    const val isAvailable: Boolean = false

    fun create(context: Context): BleMeasurementSource =
        error("A fonte de teste não existe na variante instrumentada.")
}
