package br.com.paivalab.controlapeso.ui.measurement.live

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import br.com.paivalab.controlapeso.bluetooth.BleScanError
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.measurement.SaveBleResult
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusPill
import kotlinx.coroutines.delay

@Composable
fun LiveMeasurementScreen(
    state: BleMeasurementUiState,
    onRequestPermissions: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onProfileChange: (String) -> Unit,
    onConfirmUnit: (WeightUnit) -> Unit,
    onClearUnit: () -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissDuplicate: () -> Unit,
    onConfirmDuplicate: () -> Unit,
    onUpdateNote: () -> Unit,
    onUpdateSavedProfile: () -> Unit,
    onRequestDeleteSaved: () -> Unit,
    onDismissDeleteSaved: () -> Unit,
    onConfirmDeleteSaved: () -> Unit,
    onUndoDeleteSaved: () -> Unit,
    onConsumeDeletedMeasurement: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    demoMode: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.measurement_deleted)
    val undoLabel = stringResource(R.string.undo)
    LaunchedEffect(state.savedMeasurement?.id) {
        if (state.savedMeasurement != null) {
            if (state.preferences.vibrationEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            if (state.preferences.soundEnabled) {
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
                try {
                    tone.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                    delay(150)
                } finally {
                    tone.release()
                }
            }
        }
    }
    state.deletedMeasurement?.let { deleted ->
        LaunchedEffect(deleted.id) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDeleteSaved()
            } else {
                onConsumeDeletedMeasurement()
            }
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val horizontalPadding = when {
                maxWidth >= 840.dp ->
                    ControlaPesoDesignSystem.sizes.expandedContentPadding
                maxWidth >= 600.dp ->
                    ControlaPesoDesignSystem.sizes.mediumContentPadding
                else -> ControlaPesoDesignSystem.sizes.compactContentPadding
            }
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(
                    horizontal = horizontalPadding,
                    vertical = ControlaPesoDesignSystem.spacing.lg
                ),
                verticalArrangement = Arrangement.spacedBy(
                    ControlaPesoDesignSystem.spacing.md
                )
            ) {
        item {
            Text(
                stringResource(R.string.live_measurement_title),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        if (demoMode) {
            item {
                StatusPill(
                    text = stringResource(R.string.demo_debug_notice),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        item {
            EnvironmentSummary(state)
        }
        if (state.profiles.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.profile_required_before_measurement),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            item {
                Text(stringResource(R.string.profile_label), fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.profiles.forEach { profile ->
                        FilterChip(
                            selected = state.selectedProfileId == profile.id,
                            onClick = { onProfileChange(profile.id) },
                            label = { Text(profile.name) }
                        )
                    }
                }
            }
        }
        item {
            UnitConfirmation(
                unit = state.preferences.confirmedBleUnit,
                onConfirm = onConfirmUnit,
                onClear = onClearUnit
            )
        }
        item {
            MeasurementCard(state)
        }
        if (state.error != null || state.saveError != null) {
            item {
                val message = state.error?.let { liveScanErrorText(it) }
                    ?: state.saveError?.let { liveSaveErrorText(it) }
                    ?: stringResource(R.string.live_error)
                ErrorState(
                    title = stringResource(R.string.live_error),
                    body = message
                )
            }
        }
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.permissionStatus != BlePermissionStatus.GRANTED) {
                    OutlinedButton(onClick = onRequestPermissions) {
                        Text(stringResource(R.string.request_permissions))
                    }
                }
                Button(
                    onClick = onStart,
                    enabled = !state.isScanning &&
                        state.profiles.isNotEmpty() &&
                        state.bluetoothSupport == BleSupportStatus.SUPPORTED &&
                        state.bluetoothPower == BluetoothPowerStatus.ON &&
                        state.permissionStatus == BlePermissionStatus.GRANTED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.start_measurement))
                }
                OutlinedButton(onClick = onStop, enabled = state.isScanning) {
                    Text(stringResource(R.string.cancel_measurement))
                }
            }
        }
        if (state.stableEvent != null || state.savedMeasurement != null) {
            item {
                OutlinedTextField(
                    value = state.note,
                    onValueChange = onNoteChange,
                    label = { Text(stringResource(R.string.note_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
        if (state.stableEvent != null && state.savedMeasurement == null) {
            item {
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving &&
                        state.selectedProfileId != null &&
                        state.preferences.confirmedBleUnit != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.confirm_and_save))
                }
            }
        }
        state.savedMeasurement?.let {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.measurement_saved),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.selectedProfileId != it.profileId) {
                                TextButton(onClick = onUpdateSavedProfile) {
                                    Text(stringResource(R.string.apply_profile_to_saved))
                                }
                            }
                            TextButton(onClick = onUpdateNote) {
                                Text(stringResource(R.string.save_note))
                            }
                            TextButton(onClick = onShare) {
                                Text(stringResource(R.string.share))
                            }
                            TextButton(onClick = onRequestDeleteSaved) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                    }
                }
            }
        }
    }
        }
    }

    if (state.probableDuplicate) {
        AlertDialog(
            onDismissRequest = onDismissDuplicate,
            title = { Text(stringResource(R.string.probable_duplicate_title)) },
            text = { Text(stringResource(R.string.probable_duplicate_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmDuplicate) {
                    Text(stringResource(R.string.save_anyway))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDuplicate) {
                    Text(stringResource(R.string.review))
                }
            }
        )
    }

    if (state.deleteConfirmationVisible) {
        AlertDialog(
            onDismissRequest = onDismissDeleteSaved,
            title = { Text(stringResource(R.string.delete_measurement_title)) },
            text = { Text(stringResource(R.string.delete_measurement_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteSaved) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteSaved) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EnvironmentSummary(state: BleMeasurementUiState) {
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
            StatusPill(
                text = liveStatusText(state.status),
                color = when (state.status) {
                    LiveMeasurementStatus.STABLE,
                    LiveMeasurementStatus.SAVED ->
                        ControlaPesoDesignSystem.colors.stable
                    LiveMeasurementStatus.SEARCHING,
                    LiveMeasurementStatus.WAITING_FOR_WEIGHT,
                    LiveMeasurementStatus.RECEIVING ->
                        ControlaPesoDesignSystem.colors.measuring
                    LiveMeasurementStatus.ERROR,
                    LiveMeasurementStatus.BLUETOOTH_OFF ->
                        MaterialTheme.colorScheme.error
                    else -> ControlaPesoDesignSystem.colors.informational
                }
            )
            Text(
                text = when {
                    state.bluetoothSupport != BleSupportStatus.SUPPORTED ->
                        stringResource(R.string.status_ble_unsupported)
                    state.bluetoothPower == BluetoothPowerStatus.OFF ->
                        stringResource(R.string.error_bluetooth_disabled)
                    state.permissionStatus != BlePermissionStatus.GRANTED ->
                        stringResource(R.string.error_missing_permission)
                    else -> stringResource(R.string.ble_ready)
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (state.isScanning) {
                Text(stringResource(R.string.seconds_remaining, state.secondsRemaining))
            }
        }
    }
}

@Composable
private fun UnitConfirmation(
    unit: WeightUnit?,
    onConfirm: (WeightUnit) -> Unit,
    onClear: () -> Unit
) {
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
            Text(
                stringResource(R.string.ble_unit_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (unit == null) {
                Text(stringResource(R.string.ble_unit_unconfirmed))
                Text(stringResource(R.string.ble_unit_compare))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeightUnit.entries.forEach { candidate ->
                        OutlinedButton(onClick = { onConfirm(candidate) }) {
                            Text(
                                stringResource(
                                    R.string.confirm_advertised_unit,
                                    candidate.symbol
                                )
                            )
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.ble_unit_confirmed, unit.symbol))
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.change_unit_confirmation))
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(state: BleMeasurementUiState) {
    val reading = state.latestReading
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.sm
            )
        ) {
            Text(
                text = reading?.let {
                    val suffix = state.preferences.confirmedBleUnit?.symbol
                        ?: stringResource(R.string.unit_unconfirmed_short)
                    stringResource(R.string.live_weight_value, it.advertisedValue, suffix)
                } ?: stringResource(R.string.waiting_for_scale),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = if (reading == null) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.displayLarge
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            val progress = state.stability
            LinearProgressIndicator(
                progress = { progress?.fraction ?: 0f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (state.stableEvent != null) {
                    stringResource(R.string.weight_stable)
                } else {
                    pluralStringResource(
                        R.plurals.stability_progress,
                        progress?.requiredReadings ?: 8,
                        progress?.readingsCollected ?: 0,
                        progress?.requiredReadings ?: 8
                    )
                }
            )
            reading?.let {
                Text(
                    stringResource(
                        R.string.scale_signal,
                        it.deviceName ?: stringResource(R.string.unnamed_device),
                        signalText(it.rssi)
                    )
                )
            }
        }
    }
}

@Composable
private fun liveStatusText(status: LiveMeasurementStatus): String = stringResource(
    when (status) {
        LiveMeasurementStatus.READY -> R.string.live_ready
        LiveMeasurementStatus.BLUETOOTH_OFF -> R.string.status_bluetooth_off
        LiveMeasurementStatus.PERMISSION_REQUIRED -> R.string.permission_required
        LiveMeasurementStatus.SEARCHING -> R.string.live_searching
        LiveMeasurementStatus.WAITING_FOR_WEIGHT -> R.string.waiting_for_scale
        LiveMeasurementStatus.RECEIVING -> R.string.live_receiving
        LiveMeasurementStatus.STABLE -> R.string.weight_stable
        LiveMeasurementStatus.SAVING -> R.string.saving
        LiveMeasurementStatus.SAVED -> R.string.measurement_saved
        LiveMeasurementStatus.ERROR -> R.string.live_error
    }
)

@Composable
private fun signalText(rssi: Int): String = stringResource(
    when {
        rssi >= -60 -> R.string.signal_strong
        rssi >= -75 -> R.string.signal_medium
        else -> R.string.signal_weak
    }
)

@Composable
private fun liveScanErrorText(error: BleScanError): String = stringResource(
    when (error) {
        BleScanError.MissingPermission -> R.string.error_missing_permission
        BleScanError.BluetoothUnavailable -> R.string.error_bluetooth_unavailable
        BleScanError.BleUnsupported -> R.string.error_ble_unsupported
        BleScanError.BluetoothDisabled -> R.string.error_bluetooth_disabled
        BleScanError.ScannerUnavailable -> R.string.error_scanner_unavailable
        BleScanError.SecurityFailure -> R.string.error_security
        is BleScanError.AndroidScanFailure -> R.string.live_error
        is BleScanError.UnexpectedFailure -> R.string.live_error
    }
)

@Composable
private fun liveSaveErrorText(error: SaveBleResult): String = stringResource(
    when (error) {
        SaveBleResult.UnitNotConfirmed -> R.string.ble_unit_unconfirmed
        SaveBleResult.InvalidWeight -> R.string.error_weight_range
        is SaveBleResult.ProbableDuplicate -> R.string.probable_duplicate_body
        is SaveBleResult.Saved -> R.string.measurement_saved
    }
)
