package br.com.paivalab.controlapeso.demo

import android.content.Context
import br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource

/**
 * A variante descartável usada por testes instrumentados não disponibiliza
 * dados simulados nem rota de demonstração.
 */
object DemoBleSourceFactory {
    const val isAvailable: Boolean = false

    fun create(context: Context): BleMeasurementSource =
        error("O modo de demonstração não existe na variante de testes.")
}
