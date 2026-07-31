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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionGroup
import br.com.paivalab.controlapeso.ui.designsystem.components.MeasurementUnitSelector
import br.com.paivalab.controlapeso.ui.designsystem.components.ProfileContextHeader
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusPill
import kotlinx.coroutines.delay

@Composable
fun LiveMeasurementScreen(
    state: BleMeasurementUiState,
    onRequestPermissions: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onProfileChange: (String) -> Unit,
    onClearProfile: () -> Unit = {},
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
    onDismissError: () -> Unit = {},
    onSwitchProfile: () -> Unit = {},
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
            val selectedProfile = state.profiles.firstOrNull {
                it.id == state.selectedProfileId
            }
            ProfileContextHeader(
                name = selectedProfile?.name,
                unitSymbol = state.preferences.defaultWeightUnit.symbol,
                noProfileLabel = stringResource(R.string.unassigned_profile),
                unitLabel = stringResource(R.string.dashboard_unit_label),
                switchLabel = stringResource(R.string.switch_profile),
                onSwitchProfile = onSwitchProfile,
                photoPath = state.selectedProfileId?.let {
                    state.profilePhotoPaths[it]
                },
                avatarKey = selectedProfile?.avatarKey
            )
        }
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
        if (state.profiles.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.measurement_without_profile_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                Text(stringResource(R.string.profile_label), fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.selectedProfileId == null,
                        onClick = onClearProfile,
                        label = { Text(stringResource(R.string.unassigned_profile)) },
                        modifier = Modifier.heightIn(
                            min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                        )
                    )
                    state.profiles.forEach { profile ->
                        FilterChip(
                            selected = state.selectedProfileId == profile.id,
                            onClick = { onProfileChange(profile.id) },
                            label = { Text(profile.name) },
                            modifier = Modifier.heightIn(
                                min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                            )
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
            EnvironmentSummary(state)
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
                    body = message,
                    actionLabel = stringResource(R.string.dismiss_error),
                    onAction = onDismissError,
                    actionIcon = Icons.Filled.Close
                )
            }
        }
        item {
            CompactActionGroup(
                actions = buildList {
                    if (state.permissionStatus != BlePermissionStatus.GRANTED) {
                        add(
                            CompactAction(
                                label = stringResource(R.string.request_permissions),
                                icon = Icons.Filled.PlayArrow,
                                onClick = onRequestPermissions
                            )
                        )
                    }
                    add(
                        CompactAction(
                            label = stringResource(R.string.start_measurement),
                            icon = Icons.Filled.PlayArrow,
                            onClick = onStart,
                            primary = true,
                            enabled = !state.isScanning &&
                                state.preferences.confirmedBleUnit != null &&
                                state.bluetoothSupport == BleSupportStatus.SUPPORTED &&
                                state.bluetoothPower == BluetoothPowerStatus.ON &&
                                state.permissionStatus == BlePermissionStatus.GRANTED
                        )
                    )
                    if (state.isScanning) {
                        add(
                            CompactAction(
                                label = stringResource(R.string.cancel_measurement),
                                icon = Icons.Filled.Info,
                                onClick = onStop
                            )
                        )
                    }
                }
            )
        }
        if (state.preferences.confirmedBleUnit == null) {
            item {
                Text(
                    text = stringResource(R.string.start_measurement_requires_unit),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                CompactActionButton(
                    CompactAction(
                        label = stringResource(
                            if (state.selectedProfileId == null) {
                                R.string.save_without_profile
                            } else {
                                R.string.confirm_and_save
                            }
                        ),
                        icon = Icons.Filled.Edit,
                        onClick = onSave,
                        primary = true,
                        enabled = !state.isSaving &&
                            state.preferences.confirmedBleUnit != null
                    )
                )
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
                        CompactActionGroup(
                            actions = buildList {
                                if (state.selectedProfileId != it.profileId) {
                                    add(
                                        CompactAction(
                                            label = stringResource(
                                                if (state.selectedProfileId == null) {
                                                    R.string.remove_profile_from_saved
                                                } else {
                                                    R.string.apply_profile_to_saved
                                                }
                                            ),
                                            icon = Icons.Filled.Person,
                                            onClick = onUpdateSavedProfile
                                        )
                                    )
                                }
                                add(
                                    CompactAction(
                                        label = stringResource(R.string.save_note),
                                        icon = Icons.Filled.Edit,
                                        onClick = onUpdateNote
                                    )
                                )
                                add(
                                    CompactAction(
                                        label = stringResource(R.string.share),
                                        icon = Icons.Filled.Share,
                                        onClick = onShare
                                    )
                                )
                                add(
                                    CompactAction(
                                        label = stringResource(R.string.delete),
                                        icon = Icons.Filled.Delete,
                                        onClick = onRequestDeleteSaved
                                    )
                                )
                            }
                        )
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
        color = if (unit == null) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = if (unit == null) {
            ControlaPesoDesignSystem.elevation.floating
        } else {
            ControlaPesoDesignSystem.elevation.resting
        }
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
        ) {
            if (unit == null) {
                Text(
                    stringResource(R.string.ble_unit_first_step_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    stringResource(R.string.ble_unit_first_step_body),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                MeasurementUnitSelector(
                    selected = unit,
                    onSelected = onConfirm,
                    title = stringResource(R.string.ble_unit_select_required),
                    description = stringResource(R.string.ble_unit_compare),
                    unitLabels = mapOf(
                        WeightUnit.KILOGRAM to stringResource(
                            R.string.confirm_advertised_unit,
                            WeightUnit.KILOGRAM.symbol
                        ),
                        WeightUnit.POUND to stringResource(
                            R.string.confirm_advertised_unit,
                            WeightUnit.POUND.symbol
                        )
                    ),
                    unitDescriptions = mapOf(
                        WeightUnit.KILOGRAM to stringResource(R.string.unit_kilogram_description),
                        WeightUnit.POUND to stringResource(R.string.unit_pound_description)
                    )
                )
            } else {
                Text(
                    stringResource(R.string.ble_unit_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(stringResource(R.string.ble_unit_confirmed, unit.symbol))
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.change_unit_confirmation),
                        icon = Icons.Filled.Edit,
                        onClick = onClear
                    )
                )
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
                } ?: when (state.status) {
                    LiveMeasurementStatus.SEARCHING -> stringResource(R.string.live_searching)
                    LiveMeasurementStatus.WAITING_FOR_WEIGHT ->
                        stringResource(R.string.waiting_for_scale)
                    else -> stringResource(R.string.live_ready)
                },
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
            if (state.isScanning || progress != null || state.stableEvent != null) {
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
            } else {
                Text(stringResource(R.string.live_idle_body))
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
