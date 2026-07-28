package br.com.paivalab.controlapeso.ui.goals

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.WeightUnit

@Composable
fun GoalsScreen(
    state: GoalsUiState,
    onCreate: () -> Unit,
    onEdit: (br.com.paivalab.controlapeso.domain.model.WeightGoal) -> Unit,
    onSetStatus: (br.com.paivalab.controlapeso.domain.model.WeightGoal, GoalStatus) -> Unit,
    onDelete: (br.com.paivalab.controlapeso.domain.model.WeightGoal) -> Unit,
    onDismissForm: () -> Unit,
    onStartChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = state.profile?.preferredWeightUnit
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.goals_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.goal_health_notice),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Button(
                onClick = onCreate,
                enabled = state.profile != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.create_goal))
            }
        }
        if (state.profile == null) {
            item { Text(stringResource(R.string.dashboard_no_profile)) }
        } else if (state.goals.isEmpty()) {
            item { Text(stringResource(R.string.no_goals)) }
        }
        items(state.goals, key = { it.goal.id }) { item ->
            val goal = item.goal
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(
                            R.string.goal_target_value,
                            unit?.fromKilograms(goal.targetWeightKg) ?: goal.targetWeightKg,
                            unit?.symbol ?: WeightUnit.KILOGRAM.symbol
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(goalStatusText(goal.status))
                    item.progress?.let { progress ->
                        LinearProgressIndicator(
                            progress = { progress.progressFraction.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            if (progress.reached) {
                                stringResource(R.string.goal_reached_neutral)
                            } else {
                                stringResource(
                                    R.string.goal_remaining,
                                    unit?.fromKilograms(progress.remainingKg)
                                        ?: progress.remainingKg,
                                    unit?.symbol ?: WeightUnit.KILOGRAM.symbol
                                )
                            }
                        )
                    }
                    goal.targetDate?.let {
                        Text(stringResource(R.string.goal_target_date, it.toString()))
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onEdit(goal) }) {
                            Text(stringResource(R.string.edit))
                        }
                        when (goal.status) {
                            GoalStatus.ACTIVE -> {
                                TextButton(onClick = { onSetStatus(goal, GoalStatus.PAUSED) }) {
                                    Text(stringResource(R.string.pause_goal))
                                }
                                TextButton(onClick = {
                                    onSetStatus(goal, GoalStatus.COMPLETED)
                                }) {
                                    Text(stringResource(R.string.complete_goal))
                                }
                            }
                            GoalStatus.PAUSED,
                            GoalStatus.COMPLETED -> {
                                TextButton(onClick = { onSetStatus(goal, GoalStatus.ACTIVE) }) {
                                    Text(stringResource(R.string.reactivate_goal))
                                }
                            }
                        }
                        TextButton(onClick = { onDelete(goal) }) {
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
        }
    }

    state.form?.let { form ->
        AlertDialog(
            onDismissRequest = onDismissForm,
            title = {
                Text(
                    stringResource(
                        if (form.editingId == null) R.string.create_goal
                        else R.string.edit_goal
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = form.startWeightText,
                        onValueChange = onStartChange,
                        label = {
                            Text(
                                stringResource(
                                    R.string.goal_start_weight,
                                    unit?.symbol ?: WeightUnit.KILOGRAM.symbol
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    OutlinedTextField(
                        value = form.targetWeightText,
                        onValueChange = onTargetChange,
                        label = {
                            Text(
                                stringResource(
                                    R.string.goal_target_weight,
                                    unit?.symbol ?: WeightUnit.KILOGRAM.symbol
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    OutlinedTextField(
                        value = form.targetDateText,
                        onValueChange = onDateChange,
                        label = { Text(stringResource(R.string.target_date_optional)) },
                        supportingText = { Text(stringResource(R.string.iso_date_hint)) }
                    )
                    state.error?.let {
                        Text(goalErrorText(it), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onSave, enabled = !state.isSaving) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissForm, enabled = !state.isSaving) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.delete_goal_title)) },
            text = { Text(stringResource(R.string.delete_goal_body)) },
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
private fun goalStatusText(status: GoalStatus): String = stringResource(
    when (status) {
        GoalStatus.ACTIVE -> R.string.goal_status_active
        GoalStatus.PAUSED -> R.string.goal_status_paused
        GoalStatus.COMPLETED -> R.string.goal_status_completed
    }
)

@Composable
private fun goalErrorText(error: GoalFormError): String = stringResource(
    when (error) {
        GoalFormError.INVALID_START -> R.string.error_goal_start
        GoalFormError.INVALID_TARGET -> R.string.error_goal_target
        GoalFormError.INVALID_DATE -> R.string.error_goal_date
        GoalFormError.SAVE_FAILED -> R.string.error_goal_save
    }
)
