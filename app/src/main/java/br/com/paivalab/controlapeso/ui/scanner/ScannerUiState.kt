package br.com.paivalab.controlapeso.ui.scanner

import br.com.paivalab.controlapeso.bluetooth.BleDeviceResult
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleScanError
import br.com.paivalab.controlapeso.bluetooth.BleScanPhase
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus

data class ScannerUiState(
    val bluetoothSupport: BleSupportStatus = BleSupportStatus.UNKNOWN,
    val bluetoothPower: BluetoothPowerStatus = BluetoothPowerStatus.UNKNOWN,
    val permissionStatus: BlePermissionStatus = BlePermissionStatus.REQUIRED,
    val isScanning: Boolean = false,
    val secondsRemaining: Int = 0,
    val scanPhase: BleScanPhase? = null,
    val devices: List<BleDeviceResult> = emptyList(),
    val advertisementHistory: Map<String, List<BleDeviceResult>> = emptyMap(),
    val error: BleScanError? = null
)
