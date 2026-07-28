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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.ui.components.WeightChart
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.GoalProgressCard
import br.com.paivalab.controlapeso.ui.designsystem.components.HeroMetricCard
import br.com.paivalab.controlapeso.ui.designsystem.components.LoadingState
import br.com.paivalab.controlapeso.ui.designsystem.components.MetricTile
import br.com.paivalab.controlapeso.ui.designsystem.components.SectionHeader
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusPill
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
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val wide = maxWidth >= 840.dp
        val horizontalPadding = when {
            maxWidth >= 840.dp -> ControlaPesoDesignSystem.sizes.expandedContentPadding
            maxWidth >= 600.dp -> ControlaPesoDesignSystem.sizes.mediumContentPadding
            else -> ControlaPesoDesignSystem.sizes.compactContentPadding
        }
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .widthIn(max = ControlaPesoDesignSystem.sizes.contentMaxWidth),
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
                    greeting(profile?.name, state.currentHour),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    stringResource(R.string.dashboard_scale_state),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        ControlaPesoDesignSystem.spacing.xs
                    )
                ) {
                    Button(
                        onClick = onMeasure,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.measure_with_scale))
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onManual) {
                            Text(stringResource(R.string.add_manual_weight))
                        }
                        TextButton(onClick = onHistory) {
                            Text(stringResource(R.string.nav_history))
                        }
                    }
                }
            }
            if (state.isLoading) {
                item { LoadingState(stringResource(R.string.loading_data)) }
            } else if (state.hasError) {
                item {
                    ErrorState(
                        title = stringResource(R.string.dashboard_load_error_title),
                        body = stringResource(R.string.data_load_error)
                    )
                }
            } else if (profile == null) {
                item {
                    EmptyDashboard(
                        title = stringResource(R.string.dashboard_no_profile_title),
                        body = stringResource(R.string.dashboard_no_profile),
                        action = stringResource(R.string.create_profile),
                        onAction = onProfiles
                    )
                }
            } else if (state.measurements.isEmpty()) {
                item {
                    EmptyDashboard(
                        title = stringResource(R.string.dashboard_no_measurements_title),
                        body = stringResource(R.string.dashboard_no_measurements),
                        action = stringResource(R.string.start_measurement),
                        onAction = onMeasure
                    )
                }
            } else {
                if (state.measurements.any { it.source == MeasurementSource.DEMO }) {
                    item {
                        StatusPill(
                            text = stringResource(R.string.history_contains_demo),
                            color = MaterialTheme.colorScheme.error
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
                            Card(
                                modifier = Modifier.weight(1.4f),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            SectionHeader(
                                title = stringResource(R.string.nav_history),
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 16.dp,
                                    end = 16.dp
                                )
                            )
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
        Column(
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.sm
            )
        ) {
            SectionHeader(stringResource(R.string.period_summary))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = stringResource(R.string.last_weight),
                    value = stringResource(
                        R.string.statistics_average,
                        unit.fromKilograms(stats.averageWeightKg),
                        unit.symbol
                    )
                )
                MetricTile(
                    label = stringResource(R.string.period_summary),
                    value = stringResource(
                        R.string.statistics_variation,
                        unit.fromKilograms(stats.absoluteVariationKg),
                        unit.symbol,
                        stats.percentageVariation ?: 0.0
                    )
                )
                MetricTile(
                    label = pluralStringResource(
                        R.plurals.statistics_count,
                        stats.measurementCount,
                        stats.measurementCount
                    ),
                    value = stringResource(
                        R.string.statistics_min_max,
                        unit.fromKilograms(stats.minimumWeightKg),
                        unit.fromKilograms(stats.maximumWeightKg),
                        unit.symbol
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
    HeroMetricCard(
        label = stringResource(R.string.last_weight),
        value = unit.formatFromKilograms(latest.weightKg),
        supportingText = MeasurementTimeFormatter.dateTime(latest),
        trend = when {
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
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    GoalProgressCard(
        title = stringResource(R.string.goal_progress_title),
        progress = progress?.progressFraction?.toFloat(),
        summary = when {
            goal == null || progress == null -> stringResource(R.string.no_active_goal)
            progress.reached -> stringResource(R.string.goal_reached_neutral)
            else -> stringResource(
                R.string.goal_remaining,
                unit.fromKilograms(progress.remainingKg),
                unit.symbol
            )
        },
        supportingText = goal?.let {
            stringResource(
                R.string.goal_target_value,
                unit.fromKilograms(it.targetWeightKg),
                unit.symbol
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        if (goal == null || progress == null) {
            OutlinedButton(onClick = onGoals) {
                Text(stringResource(R.string.create_goal))
            }
        }
    }
}

@Composable
private fun EmptyDashboard(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit
) {
    EmptyState(
        title = title,
        body = body,
        actionLabel = action,
        onAction = onAction
    )
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
