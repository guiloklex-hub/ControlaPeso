package br.com.paivalab.controlapeso.buildvariant

import android.content.Context
import br.com.paivalab.controlapeso.bluetooth.BleDeviceResult
import br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource
import br.com.paivalab.controlapeso.bluetooth.BleScanPhase
import br.com.paivalab.controlapeso.bluetooth.BleScanState
import br.com.paivalab.controlapeso.bluetooth.BleScanStopReason
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BleWeightReading
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object BleSourceFactory {
    const val isAvailable: Boolean = true

    fun create(context: Context): BleMeasurementSource = DemoBleMeasurementSource()
}

private class DemoBleMeasurementSource : BleMeasurementSource {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private val mutableScanState = MutableStateFlow<BleScanState>(BleScanState.Idle)
    override val scanState: StateFlow<BleScanState> = mutableScanState.asStateFlow()
    private val mutableReading = MutableStateFlow<BleWeightReading?>(null)
    override val latestReading: StateFlow<BleWeightReading?> = mutableReading.asStateFlow()
    private val mutableDevices = MutableStateFlow<List<BleDeviceResult>>(emptyList())
    override val devices: StateFlow<List<BleDeviceResult>> = mutableDevices.asStateFlow()

    override fun supportStatus() = BleSupportStatus.SUPPORTED
    override fun bluetoothPowerStatus() = BluetoothPowerStatus.ON

    override fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            val samples = listOf(
                72.8, 72.4, 72.2, 72.1,
                72.1, 72.1, 72.1, 72.1,
                72.1, 72.1, 72.1, 72.1
            )
            samples.forEachIndexed { index, value ->
                mutableScanState.value = BleScanState.Scanning(
                    secondsRemaining = 15 - index,
                    phase = if (index < 3) {
                        BleScanPhase.DISCOVERY
                    } else {
                        BleScanPhase.SAMPLING
                    }
                )
                mutableReading.value = BleWeightReading(
                    deviceAddress = "02:00:00:00:00:01",
                    deviceName = "DEMONSTRAÇÃO · balança simulada",
                    advertisedValue = value,
                    rawWeight = (value * 100).toInt(),
                    property = BleWeightReading.PROPERTY_MEASUREMENT,
                    sequenceNumber = index,
                    rssi = -48,
                    observedAtEpochMillis = System.currentTimeMillis(),
                    rawPayloadHex = "DE 00 00 00 ${index.toString(16).padStart(2, '0')}"
                        .uppercase()
                )
                delay(300)
            }
            mutableScanState.value = BleScanState.Stopped(BleScanStopReason.TIMEOUT)
        }
    }

    override fun stop(reason: BleScanStopReason) {
        job?.cancel()
        job = null
        mutableScanState.value = BleScanState.Stopped(reason)
    }

    override fun close() {
        stop(BleScanStopReason.VIEW_MODEL_CLEARED)
    }
}
