package br.com.paivalab.controlapeso.ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import br.com.paivalab.controlapeso.core.time.BrazilianDateTimeFormatter
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionGroup
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusCard
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusCardTone
import java.time.Instant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onRequestPermissions: () -> Unit,
    onOpenBluetoothSettings: () -> Unit = {},
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onClearResults: () -> Unit,
    onDismissError: () -> Unit,
    onCopyText: (String) -> Unit,
    onShareText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var maskAddresses by rememberSaveable { mutableStateOf(true) }
    var technicalInfoExpanded by rememberSaveable { mutableStateOf(false) }
    val diagnosticText = BleDiagnosticFormatter.format(
        devices = uiState.devices,
        history = uiState.advertisementHistory,
        maskAddresses = maskAddresses
    )
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bluetooth_diagnostic)) }
            )
        }
    ) { innerPadding ->
        ResponsiveScreenList(
            modifier = Modifier.padding(innerPadding),
            maxContentWidth = 1_000.dp
        ) {
            item {
                Text(
                    text = stringResource(R.string.scanner_subtitle),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                DiagnosticStatusCard(
                    uiState = uiState,
                    onRequestPermissions = onRequestPermissions,
                    onOpenBluetoothSettings = onOpenBluetoothSettings,
                    onStartScan = onStartScan,
                    onStopScan = onStopScan
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(
                            ControlaPesoDesignSystem.spacing.xs
                        )
                    ) {
                        CompactActionButton(
                            CompactAction(
                                label = stringResource(
                                    if (technicalInfoExpanded) {
                                        R.string.diagnostic_support_information_hide
                                    } else {
                                        R.string.diagnostic_support_information_show
                                    }
                                ),
                                icon = Icons.Filled.Info,
                                onClick = {
                                    technicalInfoExpanded = !technicalInfoExpanded
                                }
                            )
                        )
                        if (technicalInfoExpanded) {
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
                            if (uiState.devices.isNotEmpty()) {
                                CompactActionGroup(
                                    actions = listOf(
                                        CompactAction(
                                            label = stringResource(R.string.copy_diagnostic),
                                            icon = Icons.Filled.Info,
                                            onClick = { onCopyText(diagnosticText) }
                                        ),
                                        CompactAction(
                                            label = stringResource(R.string.export_diagnostic),
                                            icon = Icons.Filled.Share,
                                            onClick = { onShareText(diagnosticText) }
                                        ),
                                        CompactAction(
                                            label = stringResource(R.string.clear_results),
                                            icon = Icons.Filled.Close,
                                            onClick = onClearResults
                                        )
                                    )
                                )
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
                    EmptyState(
                        title = stringResource(R.string.no_devices_title),
                        body = stringResource(R.string.no_devices)
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
private fun DiagnosticStatusCard(
    uiState: ScannerUiState,
    onRequestPermissions: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    val status = when {
        uiState.isScanning -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_scanning),
            description = stringResource(R.string.diagnostic_state_scanning_body),
            tone = StatusCardTone.NEUTRAL,
            action = CompactAction(
                label = stringResource(R.string.stop_scan),
                icon = Icons.Filled.Close,
                onClick = onStopScan
            )
        )
        uiState.permissionStatus != BlePermissionStatus.GRANTED -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_permission),
            description = stringResource(
                if (uiState.permissionStatus == BlePermissionStatus.PERMANENTLY_DENIED) {
                    R.string.diagnostic_state_permission_permanent_body
                } else {
                    R.string.diagnostic_state_permission_body
                }
            ),
            tone = StatusCardTone.ATTENTION,
            action = CompactAction(
                label = stringResource(R.string.request_permissions),
                icon = Icons.Filled.Settings,
                onClick = onRequestPermissions,
                primary = true
            )
        )
        uiState.bluetoothSupport == BleSupportStatus.BLUETOOTH_UNAVAILABLE ||
            uiState.bluetoothSupport == BleSupportStatus.BLE_UNSUPPORTED -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_unavailable),
            description = stringResource(R.string.diagnostic_state_unavailable_body),
            tone = StatusCardTone.ERROR,
            action = null
        )
        uiState.bluetoothPower == BluetoothPowerStatus.OFF -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_bluetooth_off),
            description = stringResource(R.string.diagnostic_state_bluetooth_off_body),
            tone = StatusCardTone.ATTENTION,
            action = CompactAction(
                label = stringResource(R.string.open_bluetooth_settings),
                icon = Icons.Filled.Settings,
                onClick = onOpenBluetoothSettings,
                primary = true
            )
        )
        uiState.error != null -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_failure),
            description = errorText(uiState.error),
            tone = StatusCardTone.ERROR,
            action = CompactAction(
                label = stringResource(R.string.diagnostic_check_connection),
                icon = Icons.Filled.Refresh,
                onClick = onStartScan,
                primary = true
            )
        )
        uiState.bluetoothSupport == BleSupportStatus.UNKNOWN ||
            uiState.bluetoothPower == BluetoothPowerStatus.UNKNOWN -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_checking),
            description = stringResource(R.string.diagnostic_state_checking_body),
            tone = StatusCardTone.NEUTRAL,
            action = null
        )
        !uiState.hasCompletedScan -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_ready),
            description = stringResource(R.string.diagnostic_state_ready_body),
            tone = StatusCardTone.NEUTRAL,
            action = CompactAction(
                label = stringResource(R.string.diagnostic_check_connection),
                icon = Icons.Filled.PlayArrow,
                onClick = onStartScan,
                primary = true
            )
        )
        uiState.devices.isEmpty() -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_no_devices),
            description = stringResource(R.string.diagnostic_state_no_devices_body),
            tone = StatusCardTone.ATTENTION,
            action = CompactAction(
                label = stringResource(R.string.diagnostic_check_connection),
                icon = Icons.Filled.PlayArrow,
                onClick = onStartScan,
                primary = true
            )
        )
        else -> DiagnosticStatus(
            state = stringResource(R.string.diagnostic_state_ok),
            description = stringResource(R.string.diagnostic_state_ok_body),
            tone = StatusCardTone.POSITIVE,
            action = CompactAction(
                label = stringResource(R.string.diagnostic_check_connection),
                icon = Icons.Filled.Refresh,
                onClick = onStartScan,
                primary = true
            )
        )
    }

    StatusCard(
        title = stringResource(R.string.diagnostic_status_heading),
        state = status.state,
        description = status.description,
        icon = Icons.Filled.Info,
        action = status.action,
        tone = status.tone
    )
}

private data class DiagnosticStatus(
    val state: String,
    val description: String,
    val tone: StatusCardTone,
    val action: CompactAction?
)

@Composable
private fun EnvironmentStatusCard(uiState: ScannerUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
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
                text = stringResource(R.string.diagnostic_error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = errorText(error),
                style = MaterialTheme.typography.bodyMedium
            )
            CompactActionButton(
                CompactAction(
                    label = stringResource(R.string.dismiss_error),
                    icon = Icons.Filled.Close,
                    onClick = onDismiss
                ),
                modifier = Modifier.align(Alignment.End)
            )
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
    val time = formatScanTime(device.lastSeenEpochMillis)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md)) {
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

            CompactActionButton(
                CompactAction(
                    label = stringResource(
                        if (expanded) {
                            R.string.hide_technical_data
                        } else {
                            R.string.show_technical_data
                        }
                    ),
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { expanded = !expanded }
                )
            )

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
            CompactActionButton(
                CompactAction(
                    label = stringResource(R.string.copy_raw_payload),
                    icon = Icons.Filled.Info,
                    onClick = { onCopyText(rawRecord) },
                    enabled = rawRecord != unavailable
                )
            )
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
            val time = formatScanTime(result.lastSeenEpochMillis)
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

private fun formatScanTime(epochMillis: Long): String =
    BrazilianDateTimeFormatter.time(
        Instant.ofEpochMilli(epochMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()
    )

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
