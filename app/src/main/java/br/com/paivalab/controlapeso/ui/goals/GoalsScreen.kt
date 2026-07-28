package br.com.paivalab.controlapeso.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.components.BrazilianDateTextField
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.GoalProgressCard
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList

@Composable
fun GoalsScreen(
    state: GoalsUiState,
    onCreate: () -> Unit,
    onEdit: (WeightGoal) -> Unit,
    onSetStatus: (WeightGoal, GoalStatus) -> Unit,
    onDelete: (WeightGoal) -> Unit,
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
    ResponsiveScreenList(modifier = modifier) {
        item {
            Text(
                stringResource(R.string.goals_title),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                stringResource(R.string.goal_health_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            item {
                EmptyState(
                    title = stringResource(R.string.goals_profile_required_title),
                    body = stringResource(R.string.dashboard_no_profile)
                )
            }
        } else if (state.goals.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.goals_empty_title),
                    body = stringResource(R.string.no_goals),
                    actionLabel = stringResource(R.string.create_goal),
                    onAction = onCreate
                )
            }
        }
        items(state.goals, key = { it.goal.id }) { item ->
            val goal = item.goal
            val displayUnit = unit ?: WeightUnit.KILOGRAM
            val progressSummary = item.progress?.let { progress ->
                if (progress.reached) {
                    stringResource(R.string.goal_reached_neutral)
                } else {
                    stringResource(
                        R.string.goal_remaining,
                        displayUnit.fromKilograms(progress.remainingKg),
                        displayUnit.symbol
                    )
                }
            } ?: stringResource(R.string.goal_progress_waiting)
            val details = buildList {
                add(
                    stringResource(
                        R.string.goal_start_value,
                        displayUnit.fromKilograms(goal.startWeightKg),
                        displayUnit.symbol
                    )
                )
                state.latestWeightKg?.let {
                    add(
                        stringResource(
                            R.string.goal_current_value,
                            displayUnit.fromKilograms(it),
                            displayUnit.symbol
                        )
                    )
                }
                goal.targetDate?.let {
                    add(
                        stringResource(
                            R.string.goal_target_date,
                            BrazilianDateFormatter.format(it)
                        )
                    )
                }
                add(goalStatusText(goal.status))
            }.joinToString(separator = " · ")
            GoalProgressCard(
                title = stringResource(
                    R.string.goal_target_value,
                    displayUnit.fromKilograms(goal.targetWeightKg),
                    displayUnit.symbol
                ),
                progress = item.progress?.progressFraction?.toFloat(),
                summary = progressSummary,
                supportingText = details,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                    BrazilianDateTextField(
                        value = form.targetDateText,
                        onValueChange = onDateChange,
                        label = stringResource(R.string.target_date_optional)
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
