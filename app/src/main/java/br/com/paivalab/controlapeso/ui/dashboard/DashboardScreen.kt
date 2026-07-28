package br.com.paivalab.controlapeso.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.ui.components.WeightChart
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.ui.theme.LocalVisualEffects
import kotlin.math.abs

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onMeasure: () -> Unit,
    onManual: () -> Unit,
    onHistory: () -> Unit,
    onProfiles: () -> Unit,
    onGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val profile = state.profile
    val unit = profile?.preferredWeightUnit ?: state.preferences.defaultWeightUnit
    val chartHeight = when (state.preferences.chartSize) {
        ChartSize.COMPACT -> 180.dp
        ChartSize.COMFORTABLE -> 240.dp
        ChartSize.LARGE -> 320.dp
    }
    val background = if (LocalVisualEffects.current == VisualEffects.REDUCED) {
        Modifier.background(MaterialTheme.colorScheme.background)
    } else {
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                    MaterialTheme.colorScheme.background
                )
            )
        )
    }
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .then(background)
    ) {
        val wide = maxWidth >= 840.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    greeting(profile?.name, state.currentHour),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.dashboard_scale_state),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onMeasure) {
                        Text(stringResource(R.string.measure_with_scale))
                    }
                    OutlinedButton(onClick = onManual) {
                        Text(stringResource(R.string.add_manual_weight))
                    }
                    OutlinedButton(onClick = onHistory) {
                        Text(stringResource(R.string.nav_history))
                    }
                }
            }
            if (state.isLoading) {
                item { CircularProgressIndicator() }
            } else if (state.hasError) {
                item {
                    Text(
                        stringResource(R.string.data_load_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (profile == null) {
                item {
                    EmptyDashboard(
                        text = stringResource(R.string.dashboard_no_profile),
                        action = stringResource(R.string.create_profile),
                        onAction = onProfiles
                    )
                }
            } else if (state.measurements.isEmpty()) {
                item {
                    EmptyDashboard(
                        text = stringResource(R.string.dashboard_no_measurements),
                        action = stringResource(R.string.start_measurement),
                        onAction = onMeasure
                    )
                }
            } else {
                if (state.measurements.any { it.source == MeasurementSource.DEMO }) {
                    item {
                        Text(
                            stringResource(R.string.history_contains_demo),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (wide) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                LatestWeightCard(state, unit)
                                GoalCard(state, unit, onGoals)
                            }
                            Card(modifier = Modifier.weight(1.4f)) {
                                WeightChart(
                                    measurements = state.measurements,
                                    unit = unit,
                                    movingAverage = if (
                                        state.preferences.movingAverageEnabled
                                    ) {
                                        state.statistics?.movingAverage.orEmpty()
                                    } else {
                                        emptyList()
                                    },
                                    height = chartHeight,
                                    selectedId = selectedId,
                                    onSelect = { selectedId = it.id },
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    statisticsItem(state, unit)
                } else {
                    item { LatestWeightCard(state, unit) }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            WeightChart(
                                measurements = state.measurements,
                                unit = unit,
                                movingAverage = if (
                                    state.preferences.movingAverageEnabled
                                ) {
                                    state.statistics?.movingAverage.orEmpty()
                                } else {
                                    emptyList()
                                },
                                height = chartHeight,
                                selectedId = selectedId,
                                onSelect = { selectedId = it.id },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    statisticsItem(state, unit)
                    item { GoalCard(state, unit, onGoals) }
                }
            }
        }
    }
}

private fun LazyListScope.statisticsItem(
    state: DashboardUiState,
    unit: br.com.paivalab.controlapeso.domain.model.WeightUnit
) {
    val stats = state.statistics ?: return
    item {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    stringResource(R.string.period_summary),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    stringResource(
                        R.string.statistics_average,
                        unit.fromKilograms(stats.averageWeightKg),
                        unit.symbol
                    )
                )
                Text(
                    stringResource(
                        R.string.statistics_variation,
                        unit.fromKilograms(stats.absoluteVariationKg),
                        unit.symbol,
                        stats.percentageVariation ?: 0.0
                    )
                )
                Text(
                    stringResource(
                        R.string.statistics_min_max,
                        unit.fromKilograms(stats.minimumWeightKg),
                        unit.fromKilograms(stats.maximumWeightKg),
                        unit.symbol
                    )
                )
                Text(
                    pluralStringResource(
                        R.plurals.statistics_count,
                        stats.measurementCount,
                        stats.measurementCount
                    )
                )
            }
        }
    }
}

@Composable
private fun LatestWeightCard(
    state: DashboardUiState,
    unit: br.com.paivalab.controlapeso.domain.model.WeightUnit
) {
    val latest = state.measurements.last()
    val previous = state.measurements.getOrNull(state.measurements.lastIndex - 1)
    val difference = previous?.let { latest.weightKg - it.weightKg }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.last_weight), style = MaterialTheme.typography.titleMedium)
            Text(
                unit.formatFromKilograms(latest.weightKg),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                MeasurementTimeFormatter.dateTime(latest)
            )
            Text(
                when {
                    difference == null -> stringResource(R.string.variation_insufficient)
                    abs(difference) < 0.0001 -> stringResource(R.string.variation_unchanged)
                    difference > 0 -> stringResource(
                        R.string.variation_increase,
                        unit.fromKilograms(difference),
                        unit.symbol
                    )
                    else -> stringResource(
                        R.string.variation_reduction,
                        unit.fromKilograms(abs(difference)),
                        unit.symbol
                    )
                }
            )
            state.bmi?.let {
                Text(stringResource(R.string.bmi_derived, it))
                Text(
                    stringResource(R.string.bmi_disclaimer),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    state: DashboardUiState,
    unit: br.com.paivalab.controlapeso.domain.model.WeightUnit,
    onGoals: () -> Unit
) {
    val goal = state.activeGoal
    val progress = state.goalProgress
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.goal_progress_title), style = MaterialTheme.typography.titleLarge)
            if (goal == null || progress == null) {
                Text(stringResource(R.string.no_active_goal))
                OutlinedButton(onClick = onGoals) {
                    Text(stringResource(R.string.create_goal))
                }
            } else {
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
                            unit.fromKilograms(progress.remainingKg),
                            unit.symbol
                        )
                    }
                )
                Text(
                    stringResource(
                        R.string.goal_target_value,
                        unit.fromKilograms(goal.targetWeightKg),
                        unit.symbol
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyDashboard(text: String, action: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun greeting(name: String?, currentHour: Int): String {
    val greetingRes = when (currentHour) {
        in 5..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
    val base = stringResource(greetingRes)
    return name?.let { stringResource(R.string.greeting_with_name, base, it) } ?: base
}
