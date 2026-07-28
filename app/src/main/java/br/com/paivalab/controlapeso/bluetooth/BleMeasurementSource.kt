package br.com.paivalab.controlapeso.bluetooth

import kotlinx.coroutines.flow.StateFlow

/**
 * Fronteira testável sobre a captura BLE existente.
 *
 * Implementações não podem assumir unidade nem estabilidade apenas porque um
 * anúncio foi interpretado.
 */
interface BleMeasurementSource : AutoCloseable {
    val scanState: StateFlow<BleScanState>
    val latestReading: StateFlow<BleWeightReading?>
    val devices: StateFlow<List<BleDeviceResult>>

    fun supportStatus(): BleSupportStatus

    fun bluetoothPowerStatus(): BluetoothPowerStatus

    fun start()

    fun stop(reason: BleScanStopReason = BleScanStopReason.MANUAL)

    override fun close()
}
