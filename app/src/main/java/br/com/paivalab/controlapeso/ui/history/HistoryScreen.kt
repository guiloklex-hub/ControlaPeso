package br.com.paivalab.controlapeso.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.BuildConfig
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.ui.components.BrazilianDateTextField
import br.com.paivalab.controlapeso.ui.components.WeightChart
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.LoadingState
import br.com.paivalab.controlapeso.ui.designsystem.components.MeasurementListItem
import br.com.paivalab.controlapeso.ui.designsystem.components.MetricTile
import br.com.paivalab.controlapeso.ui.designsystem.components.ProfileAvatar
import br.com.paivalab.controlapeso.ui.designsystem.components.SectionHeader
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusPill
import java.time.YearMonth

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onRangeChange: (HistoryRange) -> Unit,
    onCustomStartChange: (String) -> Unit,
    onCustomEndChange: (String) -> Unit,
    onToggleSource: (MeasurementSource) -> Unit,
    onMeasurementClick: (String) -> Unit,
    onToggleUnassigned: (String) -> Unit = {},
    onAssignmentProfileChange: (String) -> Unit = {},
    onAssignSelected: () -> Unit = {},
    onResetFilters: () -> Unit = {},
    onDismissAssignmentError: () -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var rangeMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var sourceMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val unit = state.preferences.defaultWeightUnit
    val defaultRange = state.preferences.defaultHistoryPeriod.toHistoryRangeForUi()
    val chartHeight = when (state.preferences.chartSize) {
        ChartSize.COMPACT -> 180.dp
        ChartSize.COMFORTABLE -> 240.dp
        ChartSize.LARGE -> 320.dp
    }
    val reversedMeasurements = state.measurements.asReversed()
    val hasTrend = state.measurements.size >= 2
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val expanded = maxWidth >= 840.dp
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
                    stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        FilterChip(
                            selected = state.filters.range != defaultRange,
                            onClick = { rangeMenuExpanded = true },
                            label = {
                                Text(
                                    stringResource(
                                        R.string.history_period_filter,
                                        historyRangeText(state.filters.range)
                                    )
                                )
                            },
                            modifier = Modifier.heightIn(
                                min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                            )
                        )
                        DropdownMenu(
                            expanded = rangeMenuExpanded,
                            onDismissRequest = { rangeMenuExpanded = false }
                        ) {
                            HistoryRange.entries.forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(historyRangeText(range)) },
                                    onClick = {
                                        onRangeChange(range)
                                        rangeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Box {
                        val sourceLabel = if (state.filters.sources.isEmpty()) {
                            stringResource(R.string.all_sources)
                        } else {
                            stringResource(
                                R.string.selected_sources_count,
                                state.filters.sources.size
                            )
                        }
                        FilterChip(
                            selected = state.filters.sources.isNotEmpty(),
                            onClick = { sourceMenuExpanded = true },
                            label = {
                                Text(stringResource(R.string.history_source_filter, sourceLabel))
                            },
                            modifier = Modifier.heightIn(
                                min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                            )
                        )
                        DropdownMenu(
                            expanded = sourceMenuExpanded,
                            onDismissRequest = { sourceMenuExpanded = false }
                        ) {
                            MeasurementSource.entries
                                .filterNot { it == MeasurementSource.DEMO }
                                .forEach { source ->
                                    DropdownMenuItem(
                                        text = { Text(sourceText(source)) },
                                        leadingIcon = {
                                            Checkbox(
                                                checked = source in state.filters.sources,
                                                onCheckedChange = null
                                            )
                                        },
                                        onClick = { onToggleSource(source) }
                                    )
                                }
                        }
                    }
                    if (
                        state.filters.range != defaultRange ||
                            state.filters.sources.isNotEmpty() ||
                            state.filters.customStartText.isNotBlank() ||
                            state.filters.customEndText.isNotBlank()
                    ) {
                        TextButton(onClick = {
                            onResetFilters()
                            rangeMenuExpanded = false
                            sourceMenuExpanded = false
                        }) {
                            Text(stringResource(R.string.clear_filters))
                        }
                    }
                }
            }
            if (state.filters.range == HistoryRange.CUSTOM) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BrazilianDateTextField(
                            value = state.filters.customStartText,
                            onValueChange = onCustomStartChange,
                            label = stringResource(R.string.start_date),
                            modifier = Modifier.fillMaxWidth()
                        )
                        BrazilianDateTextField(
                            value = state.filters.customEndText,
                            onValueChange = onCustomEndChange,
                            label = stringResource(R.string.end_date),
                            modifier = Modifier.fillMaxWidth()
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
            if (state.assignmentError) {
                item {
                    ErrorState(
                        title = stringResource(R.string.assignment_error_title),
                        body = stringResource(R.string.assignment_error_body),
                        actionLabel = stringResource(R.string.dismiss_error),
                        onAction = onDismissAssignmentError,
                        actionIcon = Icons.Filled.Close
                    )
                }
            }
            if (state.unassignedMeasurements.isNotEmpty()) {
                item {
                    UnassignedMeasurementsCard(
                        state = state,
                        onToggle = onToggleUnassigned,
                        onProfileChange = onAssignmentProfileChange,
                        onAssign = onAssignSelected
                    )
                }
            }
            if (state.isLoading) {
                item { LoadingState(stringResource(R.string.loading_data)) }
            } else if (state.hasError) {
                item {
                    ErrorState(
                        title = stringResource(R.string.history_load_error_title),
                        body = stringResource(R.string.data_load_error),
                        actionLabel = stringResource(R.string.retry_action),
                        onAction = onRetry,
                        actionIcon = Icons.Filled.Refresh
                    )
                }
            } else if (state.profile == null) {
                item {
                    EmptyState(
                        title = stringResource(R.string.dashboard_no_profile),
                        body = stringResource(R.string.dashboard_no_profile)
                    )
                }
            } else if (state.measurements.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(R.string.history_empty),
                        body = stringResource(R.string.history_empty)
                    )
                }
            } else {
                if (BuildConfig.DEBUG && state.measurements.any {
                        it.source == MeasurementSource.DEMO
                    }) {
                    item {
                        StatusPill(
                            text = stringResource(R.string.history_contains_demo),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                item {
                    if (!hasTrend) {
                        HistoryTrendUnavailableState(state.measurements.size)
                    } else if (expanded && state.statistics != null) {
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
                if (hasTrend && !expanded && state.statistics != null) {
                    item { StatisticsCard(state) }
                }
                if (expanded) {
                    item {
                        ExpandedHistoryListDetail(
                            measurements = reversedMeasurements,
                            unit = unit,
                            profileName = state.profile?.name,
                            profilePhotoPath = state.profile?.id?.let {
                                state.profilePhotoPaths[it]
                            },
                            profileAvatarKey = state.profile?.avatarKey,
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
                                profileName = state.profile?.name,
                                profilePhotoPath = state.profile?.id?.let {
                                    state.profilePhotoPaths[it]
                                },
                                profileAvatarKey = state.profile?.avatarKey,
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
private fun HistoryTrendUnavailableState(measurementCount: Int) {
    EmptyState(
        title = stringResource(R.string.dashboard_trend_unavailable_title),
        body = pluralStringResource(
            R.plurals.dashboard_trend_unavailable_body,
            measurementCount,
            measurementCount
        )
    )
}

@Composable
private fun UnassignedMeasurementsCard(
    state: HistoryUiState,
    onToggle: (String) -> Unit,
    onProfileChange: (String) -> Unit,
    onAssign: () -> Unit
) {
    val unit = state.preferences.defaultWeightUnit
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.unassigned_measurements_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                pluralStringResource(
                    R.plurals.unassigned_measurements_body,
                    state.unassignedMeasurements.size,
                    state.unassignedMeasurements.size
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            if (state.profiles.isEmpty()) {
                Text(stringResource(R.string.unassigned_measurements_no_profile))
            } else {
                Text(
                    stringResource(R.string.assign_to_profile),
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.profiles.forEach { profile ->
                        FilterChip(
                            selected = state.assignmentProfileId == profile.id,
                            onClick = { onProfileChange(profile.id) },
                            label = { Text(profile.name) },
                            modifier = Modifier.heightIn(
                                min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                            )
                        )
                    }
                }
            }
            state.unassignedMeasurements.forEach { measurement ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = measurement.id in state.selectedUnassignedIds,
                        onCheckedChange = { onToggle(measurement.id) }
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            unit.formatFromKilograms(measurement.weightKg),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            formatMeasurementDateTime(measurement),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            sourceText(measurement.source),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            CompactActionButton(
                CompactAction(
                    label = stringResource(R.string.assign_selected_measurements),
                    icon = Icons.Filled.AddCircle,
                    onClick = onAssign,
                    primary = true,
                    enabled = state.selectedUnassignedIds.isNotEmpty() &&
                        state.assignmentProfileId != null &&
                        !state.isAssigning
                )
            )
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
    val unit = state.preferences.defaultWeightUnit
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        SectionHeader(
            title = stringResource(R.string.history_title),
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp
            )
        )
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
    val unit = state.preferences.defaultWeightUnit
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.sm
        )
    ) {
        SectionHeader(stringResource(R.string.period_summary))
        MetricTile(
            label = stringResource(R.string.period_summary),
            value = stringResource(
                R.string.statistics_first_last,
                unit.fromKilograms(stats.firstWeightKg),
                unit.fromKilograms(stats.lastWeightKg),
                unit.symbol
            ),
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(
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
                R.string.statistics_average,
                unit.fromKilograms(stats.averageWeightKg),
                unit.symbol
            ),
            modifier = Modifier.fillMaxWidth(),
            supportingText = stats.averageFrequencyDays?.let {
                stringResource(R.string.statistics_frequency, it)
            }
        )
        MetricTile(
            label = stringResource(R.string.period_summary),
            value = stringResource(
                R.string.statistics_min_max,
                unit.fromKilograms(stats.minimumWeightKg),
                unit.fromKilograms(stats.maximumWeightKg),
                unit.symbol
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ExpandedHistoryListDetail(
    measurements: List<WeightMeasurement>,
    unit: br.com.paivalab.controlapeso.domain.model.WeightUnit,
    profileName: String?,
    profilePhotoPath: String?,
    profileAvatarKey: String?,
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
                        profileName = profileName,
                        profilePhotoPath = profilePhotoPath,
                        profileAvatarKey = profileAvatarKey,
                        onClick = { onSelect(measurement) }
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
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
                    CompactActionButton(
                        CompactAction(
                            label = stringResource(R.string.open_full_detail),
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = { onOpenDetail(selected.id) },
                            primary = true
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryMeasurementCard(
    measurement: WeightMeasurement,
    unit: br.com.paivalab.controlapeso.domain.model.WeightUnit,
    profileName: String? = null,
    profilePhotoPath: String? = null,
    profileAvatarKey: String? = null,
    onClick: () -> Unit
) {
    MeasurementListItem(
        value = unit.formatFromKilograms(measurement.weightKg),
        dateTime = formatMeasurementDateTime(measurement),
        source = sourceText(measurement.source),
        sourceIsDemo = BuildConfig.DEBUG && measurement.source == MeasurementSource.DEMO,
        note = measurement.note,
        contextLabel = profileName ?: stringResource(R.string.unassigned_profile),
        leadingContent = profileName?.let { name ->
            {
                ProfileAvatar(
                    name = name,
                    contentDescription = name,
                    photoPath = profilePhotoPath,
                    avatarKey = profileAvatarKey
                )
            }
        },
        onClick = onClick
    )
}

private fun formatMeasurementDateTime(measurement: WeightMeasurement): String =
    MeasurementTimeFormatter.dateTime(measurement)

private fun HistoryPeriod.toHistoryRangeForUi(): HistoryRange = when (this) {
    HistoryPeriod.DAYS_7 -> HistoryRange.DAYS_7
    HistoryPeriod.DAYS_30 -> HistoryRange.DAYS_30
    HistoryPeriod.MONTHS_3 -> HistoryRange.MONTHS_3
    HistoryPeriod.MONTHS_6 -> HistoryRange.MONTHS_6
    HistoryPeriod.YEAR_1 -> HistoryRange.YEAR_1
    HistoryPeriod.ALL -> HistoryRange.ALL
}

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
            date.takeIf { previousDate != it }?.let(BrazilianDateFormatter::format)
        HistoryGrouping.MONTH -> {
            val month = YearMonth.from(date)
            month.takeIf { previousDate == null || YearMonth.from(previousDate) != it }
                ?.atDay(1)
                ?.let(BrazilianDateFormatter::format)
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
        MeasurementSource.DEMO -> if (BuildConfig.DEBUG) {
            R.string.source_demo
        } else {
            R.string.source_import
        }
    }
)
