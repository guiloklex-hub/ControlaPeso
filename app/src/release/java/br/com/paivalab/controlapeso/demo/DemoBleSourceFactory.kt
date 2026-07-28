package br.com.paivalab.controlapeso.demo

import android.content.Context
import br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource

object DemoBleSourceFactory {
    const val isAvailable: Boolean = false

    fun create(context: Context): BleMeasurementSource =
        error("O modo de demonstração não existe em builds release.")
}
