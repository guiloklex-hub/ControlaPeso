package br.com.paivalab.controlapeso.ui.history

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.ui.components.WeightChart
import java.time.YearMonth

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onRangeChange: (HistoryRange) -> Unit,
    onCustomStartChange: (String) -> Unit,
    onCustomEndChange: (String) -> Unit,
    onToggleSource: (MeasurementSource) -> Unit,
    onMeasurementClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val unit = state.profile?.preferredWeightUnit ?: state.preferences.defaultWeightUnit
    val chartHeight = when (state.preferences.chartSize) {
        ChartSize.COMPACT -> 180.dp
        ChartSize.COMFORTABLE -> 240.dp
        ChartSize.LARGE -> 320.dp
    }
    val reversedMeasurements = state.measurements.asReversed()
    BoxWithConstraints(modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HistoryRange.entries.forEach { range ->
                        FilterChip(
                            selected = state.filters.range == range,
                            onClick = { onRangeChange(range) },
                            label = { Text(historyRangeText(range)) }
                        )
                    }
                }
            }
            if (state.filters.range == HistoryRange.CUSTOM) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.filters.customStartText,
                            onValueChange = onCustomStartChange,
                            label = { Text(stringResource(R.string.start_date)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.filters.customEndText,
                            onValueChange = onCustomEndChange,
                            label = { Text(stringResource(R.string.end_date)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    if (state.filters.invalidCustomRange) {
                        Text(
                            stringResource(R.string.invalid_custom_period),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.filter_by_source),
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeasurementSource.entries
                        .filterNot { it == MeasurementSource.DEMO }
                        .forEach { source ->
                            FilterChip(
                                selected = source in state.filters.sources,
                                onClick = { onToggleSource(source) },
                                label = { Text(sourceText(source)) }
                            )
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
            } else if (state.profile == null) {
                item { Text(stringResource(R.string.dashboard_no_profile)) }
            } else if (state.measurements.isEmpty()) {
                item { Text(stringResource(R.string.history_empty)) }
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
                item {
                    if (expanded && state.statistics != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ChartCard(
                                state = state,
                                selectedId = selectedId,
                                chartHeight = chartHeight,
                                onSelect = { selectedId = it.id },
                                modifier = Modifier.weight(1.5f)
                            )
                            StatisticsCard(
                                state = state,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        ChartCard(
                            state = state,
                            selectedId = selectedId,
                            chartHeight = chartHeight,
                            onSelect = { selectedId = it.id }
                        )
                    }
                }
                if (!expanded && state.statistics != null) {
                    item { StatisticsCard(state) }
                }
                if (expanded) {
                    item {
                        ExpandedHistoryListDetail(
                            measurements = reversedMeasurements,
                            unit = unit,
                            grouping = state.preferences.historyGrouping,
                            selectedId = selectedId,
                            onSelect = { selectedId = it.id },
                            onOpenDetail = onMeasurementClick
                        )
                    }
                } else {
                    itemsIndexed(
                        items = reversedMeasurements,
                        key = { _, measurement -> measurement.id }
                    ) { index, measurement ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val previous = reversedMeasurements.getOrNull(index - 1)
                            val groupLabel = groupLabel(
                                measurement = measurement,
                                previous = previous,
                                grouping = state.preferences.historyGrouping
                            )
                            if (groupLabel != null) {
                                Text(groupLabel, fontWeight = FontWeight.Bold)
                            }
                            HistoryMeasurementCard(
                                measurement = measurement,
                                unit = unit,
                                onClick = { onMeasurementClick(measurement.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    state: HistoryUiState,
    selectedId: String?,
    chartHeight: androidx.compose.ui.unit.Dp,
    onSelect: (WeightMeasurement) -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = state.profile?.preferredWeightUnit ?: state.preferences.defaultWeightUnit
    Card(modifier = modifier.fillMaxWidth()) {
        WeightChart(
            measurements = state.measurements,
            unit = unit,
            movingAverage = if (state.preferences.movingAverageEnabled) {
                state.statistics?.movingAverage.orEmpty()
            } else {
                emptyList()
            },
            selectedId = selectedId,
            onSelect = onSelect,
            height = chartHeight,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun StatisticsCard(
    state: HistoryUiState,
    modifier: Modifier = Modifier
) {
    val stats = state.statistics ?: return
    val unit = state.profile?.preferredWeightUnit ?: state.preferences.defaultWeightUnit
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                stringResource(R.string.period_summary),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(
                    R.string.statistics_first_last,
                    unit.fromKilograms(stats.firstWeightKg),
                    unit.fromKilograms(stats.lastWeightKg),
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
                    R.string.statistics_average,
                    unit.fromKilograms(stats.averageWeightKg),
                    unit.symbol
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
            stats.averageFrequencyDays?.let {
                Text(stringResource(R.string.statistics_frequency, it))
            }
        }
    }
}

@Composable
private fun ExpandedHistoryListDetail(
    measurements: List<WeightMeasurement>,
    unit: br.com.paivalab.controlapeso.domain.model.WeightUnit,
    grouping: HistoryGrouping,
    selectedId: String?,
    onSelect: (WeightMeasurement) -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val selected = measurements.firstOrNull { it.id == selectedId }
        ?: measurements.firstOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 480.dp, max = 700.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = measurements,
                key = { _, measurement -> measurement.id }
            ) { index, measurement ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    groupLabel(
                        measurement = measurement,
                        previous = measurements.getOrNull(index - 1),
                        grouping = grouping
                    )?.let { Text(it, fontWeight = FontWeight.Bold) }
                    HistoryMeasurementCard(
                        measurement = measurement,
                        unit = unit,
                        onClick = { onSelect(measurement) }
                    )
                }
            }
        }
        Card(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.selected_measurement),
                    style = MaterialTheme.typography.titleLarge
                )
                if (selected == null) {
                    Text(stringResource(R.string.history_empty))
                } else {
                    Text(
                        unit.formatFromKilograms(selected.weightKg),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(formatMeasurementDateTime(selected))
                    Text(
                        stringResource(
                            R.string.detail_source,
                            sourceText(selected.source)
                        )
                    )
                    selected.deviceName?.let {
                        Text(stringResource(R.string.detail_device, it))
                    }
                    Text(
                        stringResource(
                            R.string.detail_stability,
                            stringResource(
                                if (selected.isStable) R.string.yes else R.string.no
                            )
                        )
                    )
                    selected.note?.let {
                        Text(stringResource(R.string.detail_note, it))
                    }
                    Button(onClick = { onOpenDetail(selected.id) }) {
                        Text(stringResource(R.string.open_full_detail))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryMeasurementCard(
    measurement: WeightMeasurement,
    unit: br.com.paivalab.controlapeso.domain.model.WeightUnit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    unit.formatFromKilograms(measurement.weightKg),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(formatMeasurementDateTime(measurement))
            }
            Text(sourceText(measurement.source))
        }
    }
}

private fun formatMeasurementDateTime(measurement: WeightMeasurement): String =
    MeasurementTimeFormatter.dateTime(measurement)

private fun groupLabel(
    measurement: WeightMeasurement,
    previous: WeightMeasurement?,
    grouping: HistoryGrouping
): String? {
    if (grouping == HistoryGrouping.NONE) return null
    val date = MeasurementTimeFormatter.localDate(measurement)
    val previousDate = previous?.let(MeasurementTimeFormatter::localDate)
    return when (grouping) {
        HistoryGrouping.NONE -> null
        HistoryGrouping.DAY ->
            date.takeIf { previousDate != it }?.toString()
        HistoryGrouping.MONTH -> {
            val month = YearMonth.from(date)
            month.takeIf { previousDate == null || YearMonth.from(previousDate) != it }
                ?.toString()
        }
    }
}

@Composable
private fun historyRangeText(range: HistoryRange): String = stringResource(
    when (range) {
        HistoryRange.DAYS_7 -> R.string.period_7_days
        HistoryRange.DAYS_30 -> R.string.period_30_days
        HistoryRange.MONTHS_3 -> R.string.period_3_months
        HistoryRange.MONTHS_6 -> R.string.period_6_months
        HistoryRange.YEAR_1 -> R.string.period_1_year
        HistoryRange.ALL -> R.string.period_all
        HistoryRange.CUSTOM -> R.string.period_custom
    }
)

@Composable
fun sourceText(source: MeasurementSource): String = stringResource(
    when (source) {
        MeasurementSource.BLE -> R.string.source_ble
        MeasurementSource.MANUAL -> R.string.source_manual
        MeasurementSource.IMPORT -> R.string.source_import
        MeasurementSource.HEALTH_CONNECT -> R.string.source_health_connect
        MeasurementSource.DEMO -> R.string.source_demo
    }
)
