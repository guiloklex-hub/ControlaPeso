package br.com.paivalab.controlapeso.bluetooth

sealed interface BleScanState {
    data object Idle : BleScanState

    data class Scanning(
        val secondsRemaining: Int,
        val phase: BleScanPhase
    ) : BleScanState

    data class Stopped(
        val reason: BleScanStopReason
    ) : BleScanState

    data class Failed(
        val error: BleScanError
    ) : BleScanState
}

enum class BleScanPhase {
    DISCOVERY,
    SAMPLING
}

enum class BleScanStopReason {
    MANUAL,
    TIMEOUT,
    BLUETOOTH_OFF,
    PERMISSION_LOST,
    VIEW_MODEL_CLEARED
}

sealed interface BleScanError {
    data object MissingPermission : BleScanError
    data object BluetoothUnavailable : BleScanError
    data object BleUnsupported : BleScanError
    data object BluetoothDisabled : BleScanError
    data object ScannerUnavailable : BleScanError
    data object SecurityFailure : BleScanError

    data class AndroidScanFailure(
        val errorCode: Int
    ) : BleScanError

    data class UnexpectedFailure(
        val operation: String
    ) : BleScanError
}

enum class BleSupportStatus {
    UNKNOWN,
    SUPPORTED,
    BLUETOOTH_UNAVAILABLE,
    BLE_UNSUPPORTED
}

enum class BluetoothPowerStatus {
    UNKNOWN,
    ON,
    OFF,
    PERMISSION_REQUIRED
}

enum class BlePermissionStatus {
    REQUIRED,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}
