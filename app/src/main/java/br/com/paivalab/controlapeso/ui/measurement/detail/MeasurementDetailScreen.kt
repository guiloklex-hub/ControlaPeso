package br.com.paivalab.controlapeso.ui.measurement.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.history.sourceText
import java.time.format.FormatStyle

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
        val unit = state.profile?.preferredWeightUnit ?: WeightUnit.KILOGRAM
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.measurement_detail_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (measurement == null) {
                item { Text(stringResource(R.string.measurement_not_found)) }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                unit.formatFromKilograms(measurement.weightKg),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                MeasurementTimeFormatter.dateTime(
                                    measurement,
                                    dateStyle = FormatStyle.FULL
                                )
                            )
                            Text(
                                stringResource(
                                    R.string.detail_profile,
                                    state.profile?.name
                                        ?: stringResource(R.string.not_available)
                                )
                            )
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
                item {
                    AdditionalMetricsCard(state, unit)
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onEdit(measurement.id) }) {
                            Text(stringResource(R.string.edit))
                        }
                        OutlinedButton(onClick = onShare) {
                            Text(stringResource(R.string.share))
                        }
                        TextButton(onClick = onRequestDelete) {
                            Text(stringResource(R.string.delete))
                        }
                    }
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
}

@Composable
private fun AdditionalMetricsCard(
    state: MeasurementDetailUiState,
    unit: WeightUnit
) {
    val measurement = state.measurement ?: return
    val values = listOfNotNull(
        measurement.impedanceOne?.let {
            stringResource(R.string.metric_impedance_one, it)
        },
        measurement.impedanceTwo?.let {
            stringResource(R.string.metric_impedance_two, it)
        },
        measurement.bodyFatPercent?.let {
            stringResource(R.string.metric_body_fat, it)
        },
        measurement.muscleMassKg?.let {
            stringResource(
                R.string.metric_muscle_mass,
                unit.fromKilograms(it),
                unit.symbol
            )
        },
        measurement.bodyWaterPercent?.let {
            stringResource(R.string.metric_body_water, it)
        },
        measurement.boneMassKg?.let {
            stringResource(
                R.string.metric_bone_mass,
                unit.fromKilograms(it),
                unit.symbol
            )
        },
        measurement.visceralFatLevel?.let {
            stringResource(R.string.metric_visceral_fat, it)
        },
        measurement.metabolicAge?.let {
            stringResource(R.string.metric_metabolic_age, it)
        }
    )
    if (values.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                stringResource(R.string.additional_metrics),
                style = MaterialTheme.typography.titleMedium
            )
            values.forEach { Text(it) }
        }
    }
}
