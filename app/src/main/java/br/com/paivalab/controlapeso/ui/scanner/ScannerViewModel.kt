package br.com.paivalab.controlapeso.ui.scanner

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import br.com.paivalab.controlapeso.bluetooth.BleDeviceResult
import br.com.paivalab.controlapeso.bluetooth.BlePermissionHelper
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleScanError
import br.com.paivalab.controlapeso.bluetooth.BleScanner
import br.com.paivalab.controlapeso.bluetooth.BleScanState
import br.com.paivalab.controlapeso.bluetooth.BleScanStopReason
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val devicesByAddress = linkedMapOf<String, BleDeviceResult>()
    private val historyByAddress = linkedMapOf<String, MutableList<BleDeviceResult>>()
    private val scanner = BleScanner(
        context = application,
        onDeviceFound = ::onDeviceFound,
        onStateChanged = ::onScanStateChanged
    )

    private var receiverRegistered = false
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

            when (
                intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
            ) {
                BluetoothAdapter.STATE_OFF,
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    scanner.stop(BleScanStopReason.BLUETOOTH_OFF)
                    _uiState.update {
                        it.copy(
                            bluetoothSupport = scanner.supportStatus(),
                            bluetoothPower = BluetoothPowerStatus.OFF
                        )
                    }
                }

                BluetoothAdapter.STATE_ON -> {
                    refreshEnvironment()
                }
            }
        }
    }

    init {
        registerBluetoothStateReceiver()
        refreshEnvironment()
    }

    fun startScan() {
        refreshEnvironment()
        _uiState.update { it.copy(error = null) }
        scanner.start()
    }

    fun stopScan() {
        scanner.stop(BleScanStopReason.MANUAL)
    }

    fun clearResults() {
        devicesByAddress.clear()
        historyByAddress.clear()
        _uiState.update {
            it.copy(
                devices = emptyList(),
                advertisementHistory = emptyMap()
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onPermissionsResult(permanentlyDenied: Boolean) {
        val hasPermissions = BlePermissionHelper.hasRequiredPermissions(getApplication())
        val permissionStatus = when {
            hasPermissions -> BlePermissionStatus.GRANTED
            permanentlyDenied -> BlePermissionStatus.PERMANENTLY_DENIED
            else -> BlePermissionStatus.DENIED
        }
        _uiState.update {
            it.copy(
                permissionStatus = permissionStatus,
                error = if (hasPermissions) null else BleScanError.MissingPermission
            )
        }
        refreshEnvironment()
    }

    fun refreshEnvironment() {
        val hasPermissions = BlePermissionHelper.hasRequiredPermissions(getApplication())
        val previousPermissionStatus = _uiState.value.permissionStatus
        val wasScanning = _uiState.value.isScanning
        val permissionStatus = when {
            hasPermissions -> BlePermissionStatus.GRANTED
            previousPermissionStatus == BlePermissionStatus.PERMANENTLY_DENIED ->
                BlePermissionStatus.PERMANENTLY_DENIED
            previousPermissionStatus == BlePermissionStatus.DENIED ->
                BlePermissionStatus.DENIED
            else -> BlePermissionStatus.REQUIRED
        }

        if (!hasPermissions && wasScanning) {
            scanner.stop(BleScanStopReason.PERMISSION_LOST)
        }

        _uiState.update {
            it.copy(
                bluetoothSupport = scanner.supportStatus(),
                bluetoothPower = scanner.bluetoothPowerStatus(),
                permissionStatus = permissionStatus,
                error = if (!hasPermissions && wasScanning) {
                    BleScanError.MissingPermission
                } else {
                    it.error
                }
            )
        }
    }

    override fun onCleared() {
        val application = getApplication<Application>()
        if (receiverRegistered) {
            try {
                application.unregisterReceiver(bluetoothStateReceiver)
            } catch (_: IllegalArgumentException) {
                // O registro já não existia; não há recurso adicional a liberar.
            }
            receiverRegistered = false
        }
        scanner.close()
        super.onCleared()
    }

    private fun onDeviceFound(device: BleDeviceResult) {
        devicesByAddress[device.address] = device
        val history = historyByAddress.getOrPut(device.address) { mutableListOf() }
        if (history.lastOrNull()?.hasSamePayloadAs(device) != true) {
            history += device
            if (history.size > MAX_HISTORY_PER_DEVICE) {
                history.removeAt(0)
            }
        }
        val sortedDevices = devicesByAddress.values.sortedWith(
            compareByDescending<BleDeviceResult> { it.isPossibleChipseaOrOkok }
                .thenByDescending { it.rssi }
        )
        val historySnapshot = historyByAddress.mapValues { (_, results) ->
            results.toList()
        }
        _uiState.update {
            it.copy(
                devices = sortedDevices,
                advertisementHistory = historySnapshot
            )
        }
    }

    private fun onScanStateChanged(scanState: BleScanState) {
        when (scanState) {
            BleScanState.Idle -> {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        secondsRemaining = 0,
                        scanPhase = null
                    )
                }
            }

            is BleScanState.Scanning -> {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        secondsRemaining = scanState.secondsRemaining,
                        scanPhase = scanState.phase,
                        error = null
                    )
                }
            }

            is BleScanState.Stopped -> {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        secondsRemaining = 0,
                        scanPhase = null
                    )
                }
            }

            is BleScanState.Failed -> {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        secondsRemaining = 0,
                        scanPhase = null,
                        error = scanState.error
                    )
                }
            }
        }
    }

    private fun registerBluetoothStateReceiver() {
        try {
            ContextCompat.registerReceiver(
                getApplication(),
                bluetoothStateReceiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        } catch (_: SecurityException) {
            _uiState.update { it.copy(error = BleScanError.SecurityFailure) }
        }
    }

    private fun BleDeviceResult.hasSamePayloadAs(other: BleDeviceResult): Boolean {
        val firstRawRecord = rawScanRecord
        val secondRawRecord = other.rawScanRecord
        if (firstRawRecord != null || secondRawRecord != null) {
            return firstRawRecord != null &&
                secondRawRecord != null &&
                firstRawRecord.contentEquals(secondRawRecord)
        }

        return manufacturerData.contentEquals(other.manufacturerData) &&
            serviceData.contentEquals(other.serviceData)
    }

    private fun <K> Map<K, ByteArray>.contentEquals(other: Map<K, ByteArray>): Boolean =
        size == other.size && all { (key, value) ->
            other[key]?.contentEquals(value) == true
        }

    companion object {
        private const val MAX_HISTORY_PER_DEVICE = 50
    }
}
