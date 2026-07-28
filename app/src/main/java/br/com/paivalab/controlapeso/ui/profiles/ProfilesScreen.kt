package br.com.paivalab.controlapeso.ui.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.components.BrazilianDateTextField

@Composable
fun ProfilesScreen(
    state: ProfilesUiState,
    onCreate: () -> Unit,
    onEdit: (Profile) -> Unit,
    onSetActive: (Profile) -> Unit,
    onDelete: (Profile) -> Unit,
    onDismissForm: () -> Unit,
    onNameChange: (String) -> Unit,
    onAvatarChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    onSave: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onExportBeforeDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.profiles_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.create_profile))
            }
        }
        if (state.profiles.isEmpty()) {
            item { Text(stringResource(R.string.no_profiles)) }
        }
        items(state.profiles, key = Profile::id) { profile ->
            ProfileCard(profile, onEdit, onSetActive, onDelete)
        }
    }

    state.form?.let { form ->
        ProfileDialog(
            form = form,
            isSaving = state.isSaving,
            error = state.error,
            onDismiss = onDismissForm,
            onNameChange = onNameChange,
            onAvatarChange = onAvatarChange,
            onHeightChange = onHeightChange,
            onBirthDateChange = onBirthDateChange,
            onUnitChange = onUnitChange,
            onSave = onSave
        )
    }

    state.deleteCandidate?.let { profile ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.delete_profile_title, profile.name)) },
            text = { Text(stringResource(R.string.delete_profile_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Column {
                    TextButton(onClick = onExportBeforeDelete) {
                        Text(stringResource(R.string.export_before_delete))
                    }
                    TextButton(onClick = onDismissDelete) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    onEdit: (Profile) -> Unit,
    onSetActive: (Profile) -> Unit,
    onDelete: (Profile) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(profile) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(profile.name, style = MaterialTheme.typography.titleLarge)
            Text(
                if (profile.isActive) {
                    stringResource(R.string.active_profile)
                } else {
                    stringResource(R.string.inactive_profile)
                },
                color = if (profile.isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(stringResource(R.string.profile_unit, profile.preferredWeightUnit.symbol))
            profile.heightCm?.let {
                Text(stringResource(R.string.profile_height, it))
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!profile.isActive) {
                    OutlinedButton(onClick = { onSetActive(profile) }) {
                        Text(stringResource(R.string.make_active))
                    }
                }
                TextButton(onClick = { onEdit(profile) }) {
                    Text(stringResource(R.string.edit))
                }
                TextButton(onClick = { onDelete(profile) }) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun ProfileDialog(
    form: ProfileForm,
    isSaving: Boolean,
    error: ProfileFormError?,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onAvatarChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (form.editingId == null) R.string.create_profile
                    else R.string.edit_profile
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.profile_name)) },
                    singleLine = true
                )
                Text(stringResource(R.string.avatar_label))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "ocean" to R.string.avatar_ocean,
                        "aqua" to R.string.avatar_aqua,
                        "sand" to R.string.avatar_sand,
                        "leaf" to R.string.avatar_leaf
                    ).forEach { (avatar, label) ->
                        FilterChip(
                            selected = form.avatarKey == avatar,
                            onClick = { onAvatarChange(avatar) },
                            label = { Text(stringResource(label)) }
                        )
                    }
                }
                OutlinedTextField(
                    value = form.heightText,
                    onValueChange = onHeightChange,
                    label = { Text(stringResource(R.string.height_cm_optional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                BrazilianDateTextField(
                    value = form.birthDateText,
                    onValueChange = onBirthDateChange,
                    label = stringResource(R.string.birth_date_optional)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeightUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = form.unit == unit,
                            onClick = { onUnitChange(unit) },
                            label = { Text(unit.symbol) }
                        )
                    }
                }
                error?.let {
                    Text(
                        profileErrorText(it),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isSaving) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun profileErrorText(error: ProfileFormError): String = stringResource(
    when (error) {
        ProfileFormError.NAME_REQUIRED -> R.string.error_profile_name_required
        ProfileFormError.NAME_TOO_LONG -> R.string.error_profile_name_long
        ProfileFormError.INVALID_HEIGHT -> R.string.error_profile_height
        ProfileFormError.INVALID_BIRTH_DATE -> R.string.error_profile_birth_date
        ProfileFormError.FUTURE_BIRTH_DATE -> R.string.error_profile_future_birth
        ProfileFormError.SAVE_FAILED -> R.string.error_save_profile
    }
)
