package br.com.paivalab.controlapeso.ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.bluetooth.BleDeviceResult
import br.com.paivalab.controlapeso.bluetooth.BleHexFormatter
import br.com.paivalab.controlapeso.bluetooth.BleDiagnosticFormatter
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleScanError
import br.com.paivalab.controlapeso.bluetooth.BleScanPhase
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onRequestPermissions: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onClearResults: () -> Unit,
    onDismissError: () -> Unit,
    onCopyText: (String) -> Unit,
    onShareText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var maskAddresses by rememberSaveable { mutableStateOf(true) }
    val diagnosticText = BleDiagnosticFormatter.format(
        devices = uiState.devices,
        history = uiState.advertisementHistory,
        maskAddresses = maskAddresses
    )
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.scanner_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                EnvironmentStatusCard(uiState)
            }

            item {
                ScanStatus(uiState)
            }

            uiState.error?.let { error ->
                item {
                    ErrorCard(error = error, onDismiss = onDismissError)
                }
            }

            item {
                ActionButtons(
                    uiState = uiState,
                    onRequestPermissions = onRequestPermissions,
                    onStartScan = onStartScan,
                    onStopScan = onStopScan,
                    onClearResults = onClearResults
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(
                                R.string.diagnostic_parser_version,
                                BleDiagnosticFormatter.PARSER_VERSION
                            )
                        )
                        Text(stringResource(R.string.diagnostic_advertising_notice))
                        Text(
                            stringResource(R.string.diagnostic_gatt_state),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.diagnostic_stability_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.mask_addresses_export),
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = maskAddresses,
                                onCheckedChange = { maskAddresses = it }
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onStartScan,
                                enabled = uiState.permissionStatus ==
                                    BlePermissionStatus.GRANTED &&
                                    uiState.bluetoothSupport ==
                                    BleSupportStatus.SUPPORTED &&
                                    uiState.bluetoothPower ==
                                    BluetoothPowerStatus.ON &&
                                    !uiState.isScanning
                            ) {
                                Text(
                                    stringResource(
                                        R.string.diagnostic_communication_test
                                    )
                                )
                            }
                            OutlinedButton(
                                onClick = { onCopyText(diagnosticText) },
                                enabled = uiState.devices.isNotEmpty()
                            ) {
                                Text(stringResource(R.string.copy_diagnostic))
                            }
                            OutlinedButton(
                                onClick = { onShareText(diagnosticText) },
                                enabled = uiState.devices.isNotEmpty()
                            ) {
                                Text(stringResource(R.string.export_diagnostic))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = pluralStringResource(
                        R.plurals.devices_found,
                        uiState.devices.size,
                        uiState.devices.size
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.devices.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_devices),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    items = uiState.devices,
                    key = { device -> device.address }
                ) { device ->
                    DeviceCard(
                        device = device,
                        advertisementHistory = uiState.advertisementHistory[device.address]
                            .orEmpty(),
                        onCopyText = onCopyText
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentStatusCard(uiState: ScannerUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusRow(
                label = stringResource(R.string.bluetooth_support_label),
                value = supportText(uiState.bluetoothSupport)
            )
            StatusRow(
                label = stringResource(R.string.bluetooth_power_label),
                value = bluetoothPowerText(uiState.bluetoothPower)
            )
            StatusRow(
                label = stringResource(R.string.permissions_label),
                value = permissionText(uiState.permissionStatus)
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ScanStatus(uiState: ScannerUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
        }
        Column {
            Text(
                text = if (uiState.isScanning) {
                    stringResource(R.string.scan_in_progress)
                } else {
                    stringResource(R.string.scan_stopped)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            if (uiState.isScanning) {
                Text(
                    text = stringResource(
                        R.string.seconds_remaining,
                        uiState.secondsRemaining
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uiState.scanPhase?.let { phase ->
                    Text(
                        text = when (phase) {
                            BleScanPhase.DISCOVERY ->
                                stringResource(R.string.scan_phase_discovery)
                            BleScanPhase.SAMPLING ->
                                stringResource(R.string.scan_phase_sampling)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    uiState: ScannerUiState,
    onRequestPermissions: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onClearResults: () -> Unit
) {
    val canStart = uiState.permissionStatus == BlePermissionStatus.GRANTED &&
        uiState.bluetoothSupport == BleSupportStatus.SUPPORTED &&
        uiState.bluetoothPower == BluetoothPowerStatus.ON &&
        !uiState.isScanning

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onRequestPermissions,
            enabled = uiState.permissionStatus != BlePermissionStatus.GRANTED &&
                !uiState.isScanning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.request_permissions))
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onStartScan,
                enabled = canStart,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.start_scan))
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onStopScan,
                enabled = uiState.isScanning,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.stop_scan))
            }
        }

        TextButton(
            onClick = onClearResults,
            enabled = uiState.devices.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_results))
        }
    }
}

@Composable
private fun ErrorCard(
    error: BleScanError,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = errorText(error),
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.dismiss_error))
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: BleDeviceResult,
    advertisementHistory: List<BleDeviceResult>,
    onCopyText: (String) -> Unit
) {
    var expanded by rememberSaveable(device.address) { mutableStateOf(false) }
    val time = DateFormat.getTimeInstance(DateFormat.MEDIUM)
        .format(Date(device.lastSeenEpochMillis))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device.advertisedName
                    ?.takeIf(String::isNotBlank)
                    ?: stringResource(R.string.unnamed_device),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.device_address, device.address),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.device_rssi, device.rssi),
                style = MaterialTheme.typography.bodyMedium
            )

            if (device.isPossibleChipseaOrOkok) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.possible_chipsea),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            device.okOkAdvertisement?.let { advertisement ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.okok_advertisement_detected),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.okok_weight_value,
                        advertisement.weightValue
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(
                        R.string.okok_sequence,
                        advertisement.sequenceNumber,
                        formatByte(advertisement.property)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.last_detection, time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    if (expanded) {
                        stringResource(R.string.hide_technical_data)
                    } else {
                        stringResource(R.string.show_technical_data)
                    }
                )
            }

            if (expanded) {
                TechnicalDeviceData(
                    device = device,
                    advertisementHistory = advertisementHistory,
                    onCopyText = onCopyText
                )
            }
        }
    }
}

@Composable
private fun TechnicalDeviceData(
    device: BleDeviceResult,
    advertisementHistory: List<BleDeviceResult>,
    onCopyText: (String) -> Unit
) {
    val unavailable = stringResource(R.string.not_available)
    val serviceUuids = device.serviceUuids
        .joinToString(separator = "\n")
        .ifEmpty { unavailable }
    val manufacturerIds = device.manufacturerData.keys
        .sorted()
        .joinToString(separator = "\n") { id ->
            String.format(Locale.ROOT, "0x%04X (%d)", id, id)
        }
        .ifEmpty { unavailable }
    val manufacturerData = device.manufacturerData.entries
        .sortedBy { (id, _) -> id }
        .joinToString(separator = "\n") { (id, payload) ->
            "${String.format(Locale.ROOT, "0x%04X", id)}: ${BleHexFormatter.format(payload)}"
        }
        .ifEmpty { unavailable }
    val serviceData = device.serviceData.entries
        .sortedBy { (uuid, _) -> uuid.toString() }
        .joinToString(separator = "\n") { (uuid, payload) ->
            "$uuid: ${BleHexFormatter.format(payload)}"
        }
        .ifEmpty { unavailable }
    val rawRecord = device.rawScanRecord
        ?.let(BleHexFormatter::format)
        ?.ifEmpty { unavailable }
        ?: unavailable
    val detectionSignals = device.chipseaDetectionSignals
        .joinToString(separator = "\n")
        .ifEmpty { unavailable }

    SelectionContainer {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { onCopyText(rawRecord) },
                enabled = rawRecord != unavailable
            ) {
                Text(stringResource(R.string.copy_raw_payload))
            }
            TechnicalValue(
                label = stringResource(R.string.technical_service_uuids),
                value = serviceUuids
            )
            TechnicalValue(
                label = stringResource(R.string.technical_tx_power),
                value = device.txPower?.let { "$it dBm" } ?: unavailable
            )
            TechnicalValue(
                label = stringResource(R.string.technical_manufacturer_ids),
                value = manufacturerIds
            )
            TechnicalValue(
                label = stringResource(R.string.technical_manufacturer_data),
                value = manufacturerData
            )
            TechnicalValue(
                label = stringResource(R.string.technical_service_data),
                value = serviceData
            )
            TechnicalValue(
                label = stringResource(R.string.technical_raw_record),
                value = rawRecord
            )
            TechnicalValue(
                label = stringResource(R.string.technical_detection_signals),
                value = detectionSignals
            )
            device.okOkAdvertisement?.let { advertisement ->
                TechnicalValue(
                    label = stringResource(R.string.technical_okok_raw_data),
                    value = advertisement.rawManufacturerDataHex
                )
                TechnicalValue(
                    label = stringResource(R.string.technical_okok_weight),
                    value = "${advertisement.rawWeight} / ${advertisement.weightValue}"
                )
                TechnicalValue(
                    label = stringResource(R.string.technical_okok_secondary),
                    value =
                        "${advertisement.rawSecondaryValue} / " +
                            "${advertisement.secondaryValue}"
                )
                TechnicalValue(
                    label = stringResource(R.string.technical_okok_command),
                    value =
                        "${advertisement.commandId ?: unavailable} / " +
                            "${advertisement.sequenceNumber} / " +
                            formatByte(advertisement.property)
                )
                TechnicalValue(
                    label = stringResource(R.string.technical_okok_notes),
                    value = advertisement.parserNotes.joinToString(separator = "\n")
                )
            }
            AdvertisementHistory(advertisementHistory)
        }
    }
}

@Composable
private fun AdvertisementHistory(history: List<BleDeviceResult>) {
    if (history.isEmpty()) return

    val visibleHistory = history.takeLast(MAX_VISIBLE_HISTORY).asReversed()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.technical_advertisement_history),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = pluralStringResource(
                R.plurals.history_summary,
                visibleHistory.size,
                history.size,
                visibleHistory.size
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        visibleHistory.forEach { result ->
            val time = DateFormat.getTimeInstance(DateFormat.MEDIUM)
                .format(Date(result.lastSeenEpochMillis))
            val rawRecord = result.rawScanRecord
                ?.let(BleHexFormatter::format)
                ?: stringResource(R.string.not_available)

            Column {
                Text(
                    text = stringResource(
                        R.string.history_entry_header,
                        time,
                        result.rssi
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = rawRecord,
                    style = MaterialTheme.typography.bodySmall
                )
                result.okOkAdvertisement?.let { advertisement ->
                    Text(
                        text = stringResource(
                            R.string.history_okok_value,
                            advertisement.sequenceNumber,
                            advertisement.weightValue,
                            formatByte(advertisement.property)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TechnicalValue(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun supportText(status: BleSupportStatus): String = when (status) {
    BleSupportStatus.UNKNOWN -> stringResource(R.string.status_checking)
    BleSupportStatus.SUPPORTED -> stringResource(R.string.status_supported)
    BleSupportStatus.BLUETOOTH_UNAVAILABLE ->
        stringResource(R.string.status_bluetooth_unavailable)
    BleSupportStatus.BLE_UNSUPPORTED -> stringResource(R.string.status_ble_unsupported)
}

@Composable
private fun bluetoothPowerText(status: BluetoothPowerStatus): String = when (status) {
    BluetoothPowerStatus.UNKNOWN -> stringResource(R.string.status_checking)
    BluetoothPowerStatus.ON -> stringResource(R.string.status_bluetooth_on)
    BluetoothPowerStatus.OFF -> stringResource(R.string.status_bluetooth_off)
    BluetoothPowerStatus.PERMISSION_REQUIRED ->
        stringResource(R.string.status_permission_required)
}

@Composable
private fun permissionText(status: BlePermissionStatus): String = when (status) {
    BlePermissionStatus.REQUIRED -> stringResource(R.string.permission_required)
    BlePermissionStatus.GRANTED -> stringResource(R.string.permission_granted)
    BlePermissionStatus.DENIED -> stringResource(R.string.permission_denied)
    BlePermissionStatus.PERMANENTLY_DENIED ->
        stringResource(R.string.permission_permanently_denied)
}

@Composable
private fun errorText(error: BleScanError): String = when (error) {
    BleScanError.MissingPermission -> stringResource(R.string.error_missing_permission)
    BleScanError.BluetoothUnavailable ->
        stringResource(R.string.error_bluetooth_unavailable)
    BleScanError.BleUnsupported -> stringResource(R.string.error_ble_unsupported)
    BleScanError.BluetoothDisabled -> stringResource(R.string.error_bluetooth_disabled)
    BleScanError.ScannerUnavailable -> stringResource(R.string.error_scanner_unavailable)
    BleScanError.SecurityFailure -> stringResource(R.string.error_security)
    is BleScanError.AndroidScanFailure ->
        stringResource(R.string.error_android_scan, error.errorCode)
    is BleScanError.UnexpectedFailure ->
        stringResource(R.string.error_unexpected, error.operation)
}

private fun formatByte(value: Int): String =
    String.format(Locale.ROOT, "0x%02X", value)

private const val MAX_VISIBLE_HISTORY = 12
