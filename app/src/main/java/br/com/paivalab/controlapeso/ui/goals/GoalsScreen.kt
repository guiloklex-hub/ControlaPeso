package br.com.paivalab.controlapeso.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionGroup
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.GoalProgressCard
import br.com.paivalab.controlapeso.ui.designsystem.components.ProfileContextHeader
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
    onSwitchProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val unit = state.unit
    var expandedGoalMenuId by rememberSaveable { mutableStateOf<String?>(null) }
    ResponsiveScreenList(modifier = modifier) {
        item {
            ProfileContextHeader(
                name = state.profile?.name,
                unitSymbol = unit.symbol,
                noProfileLabel = stringResource(R.string.dashboard_no_profile_label),
                unitLabel = stringResource(R.string.dashboard_unit_label),
                switchLabel = stringResource(R.string.switch_profile),
                onSwitchProfile = if (state.profile != null) onSwitchProfile else null,
                photoPath = state.profilePhotoPath,
                avatarKey = state.profile?.avatarKey
            )
        }
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
            CompactActionGroup(
                actions = listOf(
                    CompactAction(
                        label = stringResource(R.string.create_goal),
                        icon = Icons.Filled.AddCircle,
                        onClick = onCreate,
                        primary = true,
                        enabled = state.profile != null
                    )
                )
            )
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
                    body = stringResource(R.string.no_goals)
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
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.edit),
                        icon = Icons.Filled.Edit,
                        onClick = { onEdit(goal) },
                        primary = true
                    )
                )
                androidx.compose.foundation.layout.Box {
                    IconButton(
                        onClick = { expandedGoalMenuId = goal.id },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.goal_more_actions)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedGoalMenuId == goal.id,
                        onDismissRequest = { expandedGoalMenuId = null }
                    ) {
                        when (goal.status) {
                            GoalStatus.ACTIVE -> {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.pause_goal)) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    },
                                    onClick = {
                                        expandedGoalMenuId = null
                                        onSetStatus(goal, GoalStatus.PAUSED)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.complete_goal)) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    },
                                    onClick = {
                                        expandedGoalMenuId = null
                                        onSetStatus(goal, GoalStatus.COMPLETED)
                                    }
                                )
                            }
                            GoalStatus.PAUSED,
                            GoalStatus.COMPLETED -> {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.reactivate_goal)) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    },
                                    onClick = {
                                        expandedGoalMenuId = null
                                        onSetStatus(goal, GoalStatus.ACTIVE)
                                    }
                                )
                            }
                        }
                    }
                }
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.delete),
                        icon = Icons.Filled.Delete,
                        onClick = { onDelete(goal) }
                    )
                )
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
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
