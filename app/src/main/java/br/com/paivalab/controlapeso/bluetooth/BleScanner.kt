package br.com.paivalab.controlapeso.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.util.size
import java.util.Locale
import java.util.UUID

class BleScanner(
    context: Context,
    private val onDeviceFound: (BleDeviceResult) -> Unit,
    private val onStateChanged: (BleScanState) -> Unit
) {
    private val applicationContext = context.applicationContext
    private val bluetoothManager =
        applicationContext.getSystemService(BluetoothManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var activeScanner: BluetoothLeScanner? = null
    private var scanDeadlineElapsedMillis: Long = 0L
    private var isScanning = false
    private var currentPhase: BleScanPhase? = null

    private val phaseSwitchRunnable = Runnable {
        switchToSamplingPhase("fim da fase inicial")
    }

    private val timeoutRunnable = object : Runnable {
        override fun run() {
            if (!isScanning) return

            val remainingMillis = scanDeadlineElapsedMillis - SystemClock.elapsedRealtime()
            if (remainingMillis <= 0L) {
                stop(BleScanStopReason.TIMEOUT)
                return
            }

            val secondsRemaining = ((remainingMillis + 999L) / 1_000L).toInt()
            onStateChanged(
                BleScanState.Scanning(
                    secondsRemaining = secondsRemaining,
                    phase = currentPhase ?: BleScanPhase.DISCOVERY
                )
            )
            mainHandler.postDelayed(this, minOf(1_000L, remainingMillis))
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleScanResult)
        }

        override fun onScanFailed(errorCode: Int) {
            if (!isScanning) return
            Log.e(LOG_TAG, "Falha do Android ao iniciar/manter scan BLE: código=$errorCode")
            fail(BleScanError.AndroidScanFailure(errorCode))
        }
    }

    fun supportStatus(): BleSupportStatus {
        val adapter = bluetoothManager?.adapter
            ?: return BleSupportStatus.BLUETOOTH_UNAVAILABLE
        val supportsBle = applicationContext.packageManager.hasSystemFeature(
            PackageManager.FEATURE_BLUETOOTH_LE
        )
        return if (supportsBle) {
            BleSupportStatus.SUPPORTED
        } else {
            BleSupportStatus.BLE_UNSUPPORTED
        }
    }

    @SuppressLint("MissingPermission")
    fun bluetoothPowerStatus(): BluetoothPowerStatus {
        val adapter = bluetoothManager?.adapter ?: return BluetoothPowerStatus.UNKNOWN
        if (!BlePermissionHelper.hasConnectPermission(applicationContext)) {
            return BluetoothPowerStatus.PERMISSION_REQUIRED
        }
        return try {
            if (adapter.isEnabled) BluetoothPowerStatus.ON else BluetoothPowerStatus.OFF
        } catch (exception: SecurityException) {
            Log.w(LOG_TAG, "Sem permissão para consultar o estado do Bluetooth", exception)
            BluetoothPowerStatus.PERMISSION_REQUIRED
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (isScanning) {
            Log.d(LOG_TAG, "Solicitação de scan ignorada: já existe um scan em andamento")
            return
        }

        when (supportStatus()) {
            BleSupportStatus.BLUETOOTH_UNAVAILABLE -> {
                onStateChanged(BleScanState.Failed(BleScanError.BluetoothUnavailable))
                return
            }

            BleSupportStatus.BLE_UNSUPPORTED -> {
                onStateChanged(BleScanState.Failed(BleScanError.BleUnsupported))
                return
            }

            BleSupportStatus.UNKNOWN,
            BleSupportStatus.SUPPORTED -> Unit
        }

        if (!BlePermissionHelper.hasRequiredPermissions(applicationContext)) {
            onStateChanged(BleScanState.Failed(BleScanError.MissingPermission))
            return
        }

        val adapter = bluetoothManager?.adapter
        try {
            if (adapter?.isEnabled != true) {
                onStateChanged(BleScanState.Failed(BleScanError.BluetoothDisabled))
                return
            }

            val scanner = adapter.bluetoothLeScanner
            if (scanner == null) {
                onStateChanged(BleScanState.Failed(BleScanError.ScannerUnavailable))
                return
            }

            scanner.startScan(
                null,
                buildSettings(ScanSettings.SCAN_MODE_LOW_POWER),
                scanCallback
            )
            activeScanner = scanner
            isScanning = true
            currentPhase = BleScanPhase.DISCOVERY
            scanDeadlineElapsedMillis = SystemClock.elapsedRealtime() + SCAN_DURATION_MILLIS
            onStateChanged(
                BleScanState.Scanning(
                    secondsRemaining = SCAN_DURATION_SECONDS,
                    phase = BleScanPhase.DISCOVERY
                )
            )
            mainHandler.postDelayed(timeoutRunnable, 1_000L)
            mainHandler.postDelayed(phaseSwitchRunnable, DISCOVERY_PHASE_MILLIS)
            Log.d(
                LOG_TAG,
                "Scan BLE iniciado sem filtros por $SCAN_DURATION_SECONDS segundos; " +
                    "fase=DISCOVERY, modo=LOW_POWER"
            )
        } catch (exception: SecurityException) {
            Log.e(LOG_TAG, "SecurityException ao iniciar scan BLE", exception)
            fail(BleScanError.SecurityFailure)
        } catch (exception: RuntimeException) {
            Log.e(LOG_TAG, "Falha inesperada ao iniciar scan BLE", exception)
            fail(BleScanError.UnexpectedFailure("iniciar scan"))
        }
    }

    fun stop(reason: BleScanStopReason = BleScanStopReason.MANUAL) {
        if (!isScanning) return

        val stopError = releaseActiveScan()
        if (stopError == null) {
            onStateChanged(BleScanState.Stopped(reason))
            Log.d(LOG_TAG, "Scan BLE finalizado: motivo=$reason")
        } else {
            onStateChanged(BleScanState.Failed(stopError))
        }
    }

    fun close() {
        if (isScanning) {
            stop(BleScanStopReason.VIEW_MODEL_CLEARED)
        }
        mainHandler.removeCallbacksAndMessages(null)
        activeScanner = null
    }

    @SuppressLint("MissingPermission")
    private fun handleScanResult(result: ScanResult) {
        if (!isScanning) return

        try {
            val record = result.scanRecord
            val advertisedName = record?.deviceName
            val address = result.device.address
            val serviceUuids = record?.serviceUuids
                ?.map { parcelUuid -> parcelUuid.uuid }
                .orEmpty()
            val manufacturerData = record?.manufacturerSpecificData
                ?.let { data ->
                    buildMap {
                        for (index in 0 until data.size) {
                            put(data.keyAt(index), data.valueAt(index).copyOf())
                        }
                    }
                }
                .orEmpty()
            val serviceData = record?.serviceData
                ?.map { (parcelUuid, bytes) -> parcelUuid.uuid to bytes.copyOf() }
                ?.toMap()
                .orEmpty()
            val rawScanRecord = record?.bytes?.copyOf()
            val txPower = record?.txPowerLevel?.takeUnless { it == Int.MIN_VALUE }
            val okOkAdvertisement = OkOkAdvertisementParser.parse(manufacturerData)
            val chipseaSignals = buildList {
                addAll(
                    ChipseaUuids.detectionSignals(
                        advertisedName = advertisedName,
                        serviceUuids = serviceUuids,
                        manufacturerData = manufacturerData,
                        serviceData = serviceData
                    )
                )
                if (okOkAdvertisement != null) {
                    add("Manufacturer Advertising compatível com a captura real do OKOK")
                }
            }
            val device = BleDeviceResult(
                advertisedName = advertisedName,
                address = address,
                rssi = result.rssi,
                lastSeenEpochMillis = System.currentTimeMillis(),
                serviceUuids = serviceUuids,
                txPower = txPower,
                manufacturerData = manufacturerData,
                serviceData = serviceData,
                rawScanRecord = rawScanRecord,
                chipseaDetectionSignals = chipseaSignals,
                okOkAdvertisement = okOkAdvertisement
            )

            if (BleDiagnosticLogging.isDetailedLoggingEnabled) {
                logDevice(device)
            }
            onDeviceFound(device)
            if (
                okOkAdvertisement != null &&
                currentPhase == BleScanPhase.DISCOVERY
            ) {
                mainHandler.post {
                    switchToSamplingPhase("anúncio OKOK detectado")
                }
            }
        } catch (exception: SecurityException) {
            Log.e(LOG_TAG, "SecurityException ao processar resultado BLE", exception)
            fail(BleScanError.SecurityFailure)
        } catch (exception: RuntimeException) {
            Log.e(LOG_TAG, "Falha inesperada ao processar resultado BLE", exception)
            fail(BleScanError.UnexpectedFailure("processar resultado"))
        }
    }

    private fun fail(error: BleScanError) {
        if (isScanning) {
            releaseActiveScan()
        } else {
            mainHandler.removeCallbacks(timeoutRunnable)
            mainHandler.removeCallbacks(phaseSwitchRunnable)
            activeScanner = null
            currentPhase = null
        }
        onStateChanged(BleScanState.Failed(error))
    }

    @SuppressLint("MissingPermission")
    private fun switchToSamplingPhase(reason: String) {
        if (!isScanning || currentPhase == BleScanPhase.SAMPLING) return

        val scanner = activeScanner ?: run {
            fail(BleScanError.ScannerUnavailable)
            return
        }
        mainHandler.removeCallbacks(phaseSwitchRunnable)

        try {
            scanner.stopScan(scanCallback)
            scanner.startScan(
                null,
                buildSettings(ScanSettings.SCAN_MODE_LOW_LATENCY),
                scanCallback
            )
            currentPhase = BleScanPhase.SAMPLING
            val remainingMillis = scanDeadlineElapsedMillis - SystemClock.elapsedRealtime()
            val secondsRemaining = ((remainingMillis.coerceAtLeast(0L) + 999L) / 1_000L)
                .toInt()
            onStateChanged(
                BleScanState.Scanning(
                    secondsRemaining = secondsRemaining,
                    phase = BleScanPhase.SAMPLING
                )
            )
            Log.d(
                LOG_TAG,
                "Scan BLE mudou para fase=SAMPLING, modo=LOW_LATENCY; motivo=$reason"
            )
        } catch (exception: SecurityException) {
            Log.e(LOG_TAG, "SecurityException ao alternar modo do scan BLE", exception)
            fail(BleScanError.SecurityFailure)
        } catch (exception: RuntimeException) {
            Log.e(LOG_TAG, "Falha inesperada ao alternar modo do scan BLE", exception)
            fail(BleScanError.UnexpectedFailure("alternar modo do scan"))
        }
    }

    @SuppressLint("MissingPermission")
    private fun releaseActiveScan(): BleScanError? {
        isScanning = false
        mainHandler.removeCallbacks(timeoutRunnable)
        mainHandler.removeCallbacks(phaseSwitchRunnable)
        val scanner = activeScanner
        activeScanner = null
        currentPhase = null

        return try {
            scanner?.stopScan(scanCallback)
            null
        } catch (exception: SecurityException) {
            Log.e(LOG_TAG, "SecurityException ao parar scan BLE", exception)
            BleScanError.SecurityFailure
        } catch (exception: RuntimeException) {
            Log.e(LOG_TAG, "Falha inesperada ao parar scan BLE", exception)
            BleScanError.UnexpectedFailure("parar scan")
        }
    }

    private fun logDevice(device: BleDeviceResult) {
        val services = device.serviceUuids.joinToString().ifEmpty { "-" }
        val manufacturerIds = device.manufacturerData.keys
            .joinToString { id -> String.format(Locale.ROOT, "0x%04X", id) }
            .ifEmpty { "-" }
        val manufacturerPayloads = device.manufacturerData.entries
            .joinToString { (id, bytes) ->
                "${String.format(Locale.ROOT, "0x%04X", id)}=${BleHexFormatter.format(bytes)}"
            }
            .ifEmpty { "-" }
        val servicePayloads = device.serviceData.entries
            .joinToString { (uuid, bytes) -> "$uuid=${BleHexFormatter.format(bytes)}" }
            .ifEmpty { "-" }
        val rawRecord = device.rawScanRecord?.let(BleHexFormatter::format) ?: "-"
        val okOkData = device.okOkAdvertisement?.let { advertisement ->
            "sequência=${advertisement.sequenceNumber}, " +
                "pesoBruto=${advertisement.rawWeight}, " +
                "valorPeso=${advertisement.weightValue}, " +
                "valorSecundárioBruto=${advertisement.rawSecondaryValue}, " +
                "propriedade=${String.format(Locale.ROOT, "0x%02X", advertisement.property)}"
        } ?: "-"

        Log.d(
            LOG_TAG,
            "Dispositivo BLE address=${device.address}, nome=${device.advertisedName ?: "-"}, " +
                "RSSI=${device.rssi}, txPower=${device.txPower ?: "-"}, serviços=$services, " +
                "manufacturerIds=$manufacturerIds, manufacturerData=$manufacturerPayloads, " +
                "serviceData=$servicePayloads, raw=$rawRecord, " +
                "possívelChipsea=${device.isPossibleChipseaOrOkok}, okok=$okOkData"
        )
    }

    private fun buildSettings(scanMode: Int): ScanSettings = ScanSettings.Builder()
        .setScanMode(scanMode)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(0L)
        .build()

    companion object {
        const val LOG_TAG = "ControlaPesoBLE"
        const val SCAN_DURATION_SECONDS = 15
        private const val SCAN_DURATION_MILLIS = SCAN_DURATION_SECONDS * 1_000L
        private const val DISCOVERY_PHASE_MILLIS = 10_000L
    }
}
