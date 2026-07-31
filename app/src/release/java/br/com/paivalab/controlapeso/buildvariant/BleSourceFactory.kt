package br.com.paivalab.controlapeso.buildvariant

import android.content.Context
import br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource

object BleSourceFactory {
    const val isAvailable: Boolean = false

    fun create(context: Context): BleMeasurementSource =
        error("A fonte de teste não existe em builds release.")
}
