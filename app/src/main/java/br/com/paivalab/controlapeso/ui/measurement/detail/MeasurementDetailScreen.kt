package br.com.paivalab.controlapeso.ui.measurement.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionGroup
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.HeroMetricCard
import br.com.paivalab.controlapeso.ui.designsystem.components.ProfileAvatar
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList
import br.com.paivalab.controlapeso.ui.history.sourceText

@Composable
fun MeasurementDetailScreen(
    state: MeasurementDetailUiState,
    onEdit: (String) -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onUndoDelete: () -> Unit,
    onConsumeDeleteNotice: () -> Unit,
    onShare: () -> Unit,
    onRequestAssignProfile: () -> Unit = {},
    onDismissAssignProfile: () -> Unit = {},
    onAssignProfile: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.measurement_deleted)
    val undoLabel = stringResource(R.string.undo)
    if (state.deletedMeasurement != null) {
        LaunchedEffect(state.deletedMeasurement.id) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) onUndoDelete()
            else onConsumeDeleteNotice()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val measurement = state.measurement
        val unit = state.unit
        ResponsiveScreenList(
            modifier = Modifier.padding(innerPadding),
            maxContentWidth = 760.dp
        ) {
            item {
                Text(
                    stringResource(R.string.measurement_detail_title),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            if (measurement == null) {
                item {
                    EmptyState(
                        title = stringResource(R.string.measurement_not_found_title),
                        body = stringResource(R.string.measurement_not_found),
                        actionLabel = stringResource(R.string.back),
                        onAction = onNavigateBack,
                        actionIcon = Icons.AutoMirrored.Filled.ArrowBack
                    )
                }
            } else {
                item {
                    HeroMetricCard(
                        label = stringResource(R.string.measurement_weight_label),
                        value = unit.formatFromKilograms(measurement.weightKg),
                        supportingText = MeasurementTimeFormatter.dateTime(measurement),
                        modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(
                                    ControlaPesoDesignSystem.spacing.xs
                                )
                            ) {
                                val profileName = state.profile?.name
                                    ?: stringResource(R.string.unassigned_profile)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        ControlaPesoDesignSystem.spacing.sm
                                    )
                                ) {
                                    ProfileAvatar(
                                        name = profileName,
                                        contentDescription = profileName,
                                        photoPath = state.profilePhotoPath,
                                        avatarKey = state.profileAvatarKey
                                    )
                                    Column {
                                        Text(
                                            profileName,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            stringResource(
                                                R.string.detail_profile,
                                                profileName
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Text(
                                stringResource(
                                    R.string.detail_source,
                                    sourceText(measurement.source)
                                )
                            )
                            measurement.deviceName?.let {
                                Text(stringResource(R.string.detail_device, it))
                            }
                            Text(
                                stringResource(
                                    R.string.detail_stability,
                                    if (measurement.isStable) {
                                        stringResource(R.string.yes)
                                    } else {
                                        stringResource(R.string.no)
                                    }
                                )
                            )
                            state.previousMeasurement?.let { previous ->
                                Text(
                                    stringResource(
                                        R.string.detail_difference,
                                        unit.fromKilograms(
                                            measurement.weightKg - previous.weightKg
                                        ),
                                        unit.symbol
                                    )
                                )
                            }
                            measurement.note?.let {
                                Text(stringResource(R.string.detail_note, it))
                            }
                        }
                    }
                }
                if (measurement.profileId == null && state.profiles.isNotEmpty()) {
                    item {
                        CompactActionButton(
                            CompactAction(
                                label = stringResource(R.string.assign_profile),
                                icon = Icons.Filled.Person,
                                onClick = onRequestAssignProfile
                            )
                        )
                    }
                }
                item {
                    CompactActionGroup(
                        actions = listOf(
                            CompactAction(
                                label = stringResource(R.string.edit),
                                icon = Icons.Filled.Edit,
                                onClick = { onEdit(measurement.id) },
                                primary = true
                            ),
                            CompactAction(
                                label = stringResource(R.string.share),
                                icon = Icons.Filled.Share,
                                onClick = onShare
                            ),
                            CompactAction(
                                label = stringResource(R.string.delete),
                                icon = Icons.Filled.Delete,
                                onClick = onRequestDelete
                            )
                        )
                    )
                }
            }
        }
    }

    if (state.deleteConfirmationVisible) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.delete_measurement_title)) },
            text = { Text(stringResource(R.string.delete_measurement_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.assignProfileDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissAssignProfile,
            title = { Text(stringResource(R.string.assign_profile_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.assign_profile_body))
                    state.profiles.forEach { profile ->
                        OutlinedButton(
                            onClick = { onAssignProfile(profile.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(profile.name)
                        }
                    }
                    if (state.assignmentError) {
                        Text(
                            stringResource(R.string.assignment_error_body),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissAssignProfile) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
