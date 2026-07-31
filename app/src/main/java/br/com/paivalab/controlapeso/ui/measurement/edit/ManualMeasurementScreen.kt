package br.com.paivalab.controlapeso.ui.measurement.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.measurement.ManualValidationError
import br.com.paivalab.controlapeso.ui.components.BrazilianDateTextField
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.MeasurementUnitSelector
import br.com.paivalab.controlapeso.ui.designsystem.components.ProfileContextHeader
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList

@Composable
fun ManualMeasurementScreen(
    state: ManualMeasurementUiState,
    editing: Boolean,
    onProfileChange: (String) -> Unit,
    onClearProfile: () -> Unit = {},
    onWeightChange: (String) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissDuplicate: () -> Unit,
    onConfirmDuplicate: () -> Unit,
    onSaved: (String) -> Unit,
    onConsumeSaved: () -> Unit,
    onSwitchProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    state.savedMeasurementId?.let { id ->
        LaunchedEffect(id) {
            onSaved(id)
            onConsumeSaved()
        }
    }

    ResponsiveScreenList(
        modifier = modifier.imePadding(),
        maxContentWidth = 720.dp
    ) {
        item {
            val selectedProfile = state.profiles.firstOrNull {
                it.id == state.selectedProfileId
            }
            ProfileContextHeader(
                name = selectedProfile?.name,
                unitSymbol = state.unit.symbol,
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
                stringResource(
                    if (editing) R.string.edit_measurement_title
                    else R.string.manual_measurement_title
                ),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                stringResource(R.string.manual_measurement_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.profiles.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.measurement_without_profile_title),
                    body = stringResource(R.string.measurement_without_profile_body)
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
            OutlinedTextField(
                value = state.weightText,
                onValueChange = onWeightChange,
                label = { Text(stringResource(R.string.weight_with_unit, state.unit.symbol)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            MeasurementUnitSelector(
                selected = state.unit,
                onSelected = onUnitChange,
                title = stringResource(R.string.manual_unit_title),
                description = stringResource(R.string.manual_unit_body),
                unitLabels = mapOf(
                    WeightUnit.KILOGRAM to stringResource(R.string.unit_kilogram_label),
                    WeightUnit.POUND to stringResource(R.string.unit_pound_label)
                ),
                unitDescriptions = mapOf(
                    WeightUnit.KILOGRAM to stringResource(R.string.unit_kilogram_description),
                    WeightUnit.POUND to stringResource(R.string.unit_pound_description)
                )
            )
        }
        item {
            BrazilianDateTextField(
                value = state.dateText,
                onValueChange = onDateChange,
                label = stringResource(R.string.date_label),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.timeText,
                onValueChange = onTimeChange,
                label = { Text(stringResource(R.string.time_label)) },
                supportingText = { Text(stringResource(R.string.time_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.note_optional)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.errors.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.errors.forEach { error ->
                        Text(
                            manualErrorText(error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        item {
            CompactActionButton(
                CompactAction(
                    label = stringResource(
                        if (state.selectedProfileId == null) {
                            R.string.save_without_profile
                        } else {
                            R.string.save_measurement
                        }
                    ),
                    icon = Icons.Filled.Edit,
                    onClick = onSave,
                    primary = true,
                    enabled = !state.isSaving
                )
            )
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
}

@Composable
private fun manualErrorText(error: ManualValidationError): String = stringResource(
    when (error) {
        ManualValidationError.PROFILE_REQUIRED -> R.string.error_profile_required
        ManualValidationError.WEIGHT_REQUIRED -> R.string.error_weight_required
        ManualValidationError.INVALID_WEIGHT -> R.string.error_invalid_weight
        ManualValidationError.WEIGHT_OUT_OF_RANGE -> R.string.error_weight_range
        ManualValidationError.INVALID_DATE -> R.string.error_invalid_date
        ManualValidationError.INVALID_TIME -> R.string.error_invalid_time
        ManualValidationError.FUTURE_DATE -> R.string.error_future_date
        ManualValidationError.NOTE_TOO_LONG -> R.string.error_note_too_long
    }
)
