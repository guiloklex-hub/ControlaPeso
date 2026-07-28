package br.com.paivalab.controlapeso.bluetooth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Adaptador fino: o scanner e o parser existentes permanecem as fontes de
 * verdade. A classe mantém somente Application Context.
 */
class OkOkBleMeasurementSource(context: Context) : BleMeasurementSource {
    private val _scanState = MutableStateFlow<BleScanState>(BleScanState.Idle)
    override val scanState: StateFlow<BleScanState> = _scanState.asStateFlow()

    private val _latestReading = MutableStateFlow<BleWeightReading?>(null)
    override val latestReading: StateFlow<BleWeightReading?> =
        _latestReading.asStateFlow()

    private val _devices = MutableStateFlow<List<BleDeviceResult>>(emptyList())
    override val devices: StateFlow<List<BleDeviceResult>> = _devices.asStateFlow()

    private val devicesByAddress = linkedMapOf<String, BleDeviceResult>()
    private val scanner = BleScanner(
        context = context.applicationContext,
        onDeviceFound = ::onDeviceFound,
        onStateChanged = { state -> _scanState.value = state }
    )

    override fun supportStatus(): BleSupportStatus = scanner.supportStatus()

    override fun bluetoothPowerStatus(): BluetoothPowerStatus =
        scanner.bluetoothPowerStatus()

    override fun start() = scanner.start()

    override fun stop(reason: BleScanStopReason) = scanner.stop(reason)

    override fun close() = scanner.close()

    private fun onDeviceFound(device: BleDeviceResult) {
        devicesByAddress[device.address] = device
        _devices.update {
            devicesByAddress.values.sortedWith(
                compareByDescending<BleDeviceResult> { it.isPossibleChipseaOrOkok }
                    .thenByDescending { it.rssi }
            )
        }
        val advertisement = device.okOkAdvertisement ?: return
        _latestReading.value = BleWeightReading(
            deviceAddress = device.address,
            deviceName = device.advertisedName,
            advertisedValue = advertisement.weightValue,
            rawWeight = advertisement.rawWeight,
            property = advertisement.property,
            sequenceNumber = advertisement.sequenceNumber,
            rssi = device.rssi,
            observedAtEpochMillis = device.lastSeenEpochMillis,
            rawPayloadHex = advertisement.rawManufacturerDataHex
        )
    }
}
