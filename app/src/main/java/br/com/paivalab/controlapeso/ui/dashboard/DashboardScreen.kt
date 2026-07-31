package br.com.paivalab.controlapeso.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.BuildConfig
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.ui.components.WeightChart
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.EmptyState
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionGroup
import br.com.paivalab.controlapeso.ui.designsystem.components.GoalProgressCard
import br.com.paivalab.controlapeso.ui.designsystem.components.HeroMetricCard
import br.com.paivalab.controlapeso.ui.designsystem.components.LoadingState
import br.com.paivalab.controlapeso.ui.designsystem.components.MetricTile
import br.com.paivalab.controlapeso.ui.designsystem.components.ProfileContextHeader
import br.com.paivalab.controlapeso.ui.designsystem.components.SectionHeader
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusCard
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusCardTone
import br.com.paivalab.controlapeso.ui.designsystem.components.StatusPill
import br.com.paivalab.controlapeso.ui.update.ReleaseCheckStatus
import kotlin.math.abs

data class DashboardEnvironmentState(
    val bluetoothSupport: BleSupportStatus = BleSupportStatus.UNKNOWN,
    val bluetoothPower: BluetoothPowerStatus = BluetoothPowerStatus.UNKNOWN,
    val bluetoothPermission: BlePermissionStatus = BlePermissionStatus.REQUIRED,
    val updateStatus: ReleaseCheckStatus = ReleaseCheckStatus.NOT_CHECKED,
    val updateVersion: String? = null
)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onMeasure: () -> Unit,
    onManual: () -> Unit,
    onHistory: () -> Unit,
    onProfiles: () -> Unit,
    onGoals: () -> Unit,
    onReports: () -> Unit = {},
    onRetry: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenDataBackup: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    environment: DashboardEnvironmentState = DashboardEnvironmentState(),
    modifier: Modifier = Modifier
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val profile = state.profile
    val unit = state.preferences.defaultWeightUnit
    val hasTrend = state.measurements.size >= 2
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        ControlaPesoDesignSystem.spacing.xs
                    )
                ) {
                    ProfileContextHeader(
                        name = profile?.name,
                        unitSymbol = unit.symbol,
                        noProfileLabel = stringResource(R.string.dashboard_no_profile_label),
                        unitLabel = stringResource(R.string.dashboard_unit_label),
                        switchLabel = stringResource(R.string.switch_profile),
                        onSwitchProfile = onProfiles,
                        photoPath = state.activeProfilePhotoPath,
                        avatarKey = profile?.avatarKey
                    )
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
            }
            item {
                CompactActionGroup(
                    actions = buildList {
                        if (profile == null) {
                            add(
                                CompactAction(
                                    label = stringResource(R.string.create_profile),
                                    icon = Icons.Filled.AddCircle,
                                    onClick = onProfiles,
                                    primary = true
                                )
                            )
                        }
                        add(
                            CompactAction(
                                label = stringResource(R.string.measure_with_scale),
                                icon = Icons.Filled.AddCircle,
                                onClick = onMeasure,
                                primary = profile != null
                            )
                        )
                        add(
                            CompactAction(
                                label = stringResource(R.string.add_manual_weight),
                                icon = Icons.Filled.Edit,
                                onClick = onManual
                            )
                        )
                        add(
                            CompactAction(
                                label = stringResource(R.string.nav_history),
                                icon = Icons.AutoMirrored.Filled.List,
                                onClick = onHistory
                            )
                        )
                        if (profile != null) {
                            add(
                                CompactAction(
                                    label = stringResource(R.string.nav_reports),
                                    icon = Icons.Filled.Info,
                                    onClick = onReports
                                )
                            )
                        }
                    }
                )
            }
            if (!state.isLoading) {
                item {
                    DashboardStatusArea(
                        state = state,
                        environment = environment,
                        wide = wide,
                        onOpenSettings = onOpenSettings,
                        onOpenDataBackup = onOpenDataBackup,
                        onOpenAbout = onOpenAbout,
                        onHistory = onHistory
                    )
                }
            }
            if (state.isLoading) {
                item { LoadingState(stringResource(R.string.loading_data)) }
            } else if (state.hasError) {
                item {
                    ErrorState(
                        title = stringResource(R.string.dashboard_load_error_title),
                        body = stringResource(R.string.data_load_error),
                        actionLabel = stringResource(R.string.retry_action),
                        onAction = onRetry,
                        actionIcon = Icons.Filled.Refresh
                    )
                }
            } else if (profile == null) {
                item {
                    EmptyDashboard(
                        title = stringResource(R.string.dashboard_no_profile_title),
                        body = stringResource(R.string.dashboard_no_profile)
                    )
                }
            } else if (state.measurements.isEmpty()) {
                item {
                    EmptyDashboard(
                        title = stringResource(R.string.dashboard_no_measurements_title),
                        body = stringResource(R.string.dashboard_no_measurements)
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
                                if (hasTrend) {
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
                                } else {
                                    TrendUnavailableState(
                                        measurementCount = state.measurements.size,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (hasTrend) statisticsItem(state, unit)
                } else {
                    item { LatestWeightCard(state, unit) }
                    item { GoalCard(state, unit, onGoals) }
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
                            if (hasTrend) {
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
                            } else {
                                TrendUnavailableState(
                                    measurementCount = state.measurements.size,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    if (hasTrend) statisticsItem(state, unit)
                }
            }
        }
    }
}

private data class DashboardStatusCardData(
    val title: String,
    val state: String,
    val description: String,
    val icon: ImageVector,
    val action: CompactAction,
    val tone: StatusCardTone
)

@Composable
private fun DashboardStatusArea(
    state: DashboardUiState,
    environment: DashboardEnvironmentState,
    wide: Boolean,
    onOpenSettings: () -> Unit,
    onOpenDataBackup: () -> Unit,
    onOpenAbout: () -> Unit,
    onHistory: () -> Unit
) {
    val cards = buildList {
        if (!state.preferences.onboardingCompleted) {
            add(
                DashboardStatusCardData(
                    title = stringResource(R.string.dashboard_status_onboarding_title),
                    state = stringResource(R.string.dashboard_status_onboarding_state),
                    description = stringResource(R.string.dashboard_status_onboarding_body),
                    icon = Icons.Filled.PlayArrow,
                    action = CompactAction(
                        label = stringResource(R.string.dashboard_status_onboarding_action),
                        icon = Icons.Filled.Settings,
                        onClick = onOpenSettings,
                        primary = true
                    ),
                    tone = StatusCardTone.ATTENTION
                )
            )
        }
        add(
            BluetoothStatusCard(
                environment = environment,
                onOpenSettings = onOpenSettings
            )
        )
        add(
            DashboardStatusCardData(
                title = stringResource(R.string.dashboard_status_backup_title),
                state = when (state.preferences.localBackupFrequency) {
                    br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency.OFF ->
                        stringResource(R.string.dashboard_status_backup_disabled)
                    br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency.DAILY ->
                        stringResource(R.string.dashboard_status_backup_daily)
                    br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency.WEEKLY ->
                        stringResource(R.string.dashboard_status_backup_weekly)
                    br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency.MONTHLY ->
                        stringResource(R.string.dashboard_status_backup_monthly)
                },
                description = stringResource(R.string.dashboard_status_backup_body),
                icon = Icons.Filled.Info,
                action = CompactAction(
                    label = stringResource(R.string.open_data_backup),
                    icon = Icons.Filled.Build,
                    onClick = onOpenDataBackup
                ),
                tone = if (
                    state.preferences.localBackupFrequency ==
                    br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency.OFF
                ) {
                    StatusCardTone.NEUTRAL
                } else {
                    StatusCardTone.POSITIVE
                }
            )
        )
        state.profile?.let { profile ->
            add(
                DashboardStatusCardData(
                    title = stringResource(R.string.dashboard_status_health_title),
                    state = if (profile.healthConnectEnabled) {
                        stringResource(R.string.dashboard_status_health_enabled)
                    } else {
                        stringResource(R.string.dashboard_status_health_disabled)
                    },
                    description = stringResource(R.string.dashboard_status_health_body),
                    icon = Icons.Filled.Info,
                    action = CompactAction(
                        label = stringResource(R.string.health_manage_permissions),
                        icon = Icons.Filled.Settings,
                        onClick = onOpenSettings
                    ),
                    tone = if (profile.healthConnectEnabled) {
                        StatusCardTone.POSITIVE
                    } else {
                        StatusCardTone.NEUTRAL
                    }
                )
            )
        }
        when (environment.updateStatus) {
            ReleaseCheckStatus.UPDATE_AVAILABLE -> add(
                DashboardStatusCardData(
                    title = stringResource(R.string.dashboard_status_update_title),
                    state = environment.updateVersion?.let {
                        stringResource(R.string.dashboard_status_update_available, it)
                    } ?: stringResource(R.string.update_status_available),
                    description = stringResource(R.string.dashboard_status_update_body),
                    icon = Icons.Filled.Refresh,
                    action = CompactAction(
                        label = stringResource(R.string.about_title),
                        icon = Icons.Filled.Info,
                        onClick = onOpenAbout,
                        primary = true
                    ),
                    tone = StatusCardTone.ATTENTION
                )
            )
            ReleaseCheckStatus.FAILED -> add(
                DashboardStatusCardData(
                    title = stringResource(R.string.dashboard_status_update_title),
                    state = stringResource(R.string.update_status_failed),
                    description = stringResource(R.string.dashboard_status_update_failed_body),
                    icon = Icons.Filled.Refresh,
                    action = CompactAction(
                        label = stringResource(R.string.about_title),
                        icon = Icons.Filled.Info,
                        onClick = onOpenAbout
                    ),
                    tone = StatusCardTone.NEUTRAL
                )
            )
            else -> Unit
        }
        if (BuildConfig.DEBUG && state.measurements.any {
                it.source == MeasurementSource.DEMO
            }) {
            add(
                DashboardStatusCardData(
                    title = stringResource(R.string.dashboard_status_demo_title),
                    state = stringResource(R.string.dashboard_status_demo_state),
                    description = stringResource(R.string.dashboard_status_demo_body),
                    icon = Icons.Filled.Info,
                    action = CompactAction(
                        label = stringResource(R.string.nav_history),
                        icon = Icons.AutoMirrored.Filled.List,
                        onClick = onHistory
                    ),
                    tone = StatusCardTone.ATTENTION
                )
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(ControlaPesoDesignSystem.spacing.sm)) {
        SectionHeader(
            title = stringResource(R.string.dashboard_status_section),
            supportingText = stringResource(R.string.dashboard_status_section_body)
        )
        if (wide) {
            cards.chunked(2).forEach { rowCards ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ControlaPesoDesignSystem.spacing.sm
                    )
                ) {
                    rowCards.forEach { card ->
                        StatusCard(
                            title = card.title,
                            state = card.state,
                            description = card.description,
                            icon = card.icon,
                            action = card.action,
                            tone = card.tone,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowCards.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else {
            cards.forEach { card ->
                StatusCard(
                    title = card.title,
                    state = card.state,
                    description = card.description,
                    icon = card.icon,
                    action = card.action,
                    tone = card.tone,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BluetoothStatusCard(
    environment: DashboardEnvironmentState,
    onOpenSettings: () -> Unit
): DashboardStatusCardData {
    val (state, description, tone) = when {
        environment.bluetoothPermission == BlePermissionStatus.PERMANENTLY_DENIED -> Triple(
            stringResource(R.string.dashboard_status_bluetooth_permanently_denied),
            stringResource(R.string.dashboard_status_bluetooth_permission_body),
            StatusCardTone.ERROR
        )
        environment.bluetoothPermission != BlePermissionStatus.GRANTED -> Triple(
            stringResource(R.string.dashboard_status_bluetooth_permission),
            stringResource(R.string.dashboard_status_bluetooth_permission_body),
            StatusCardTone.ATTENTION
        )
        environment.bluetoothSupport == BleSupportStatus.BLUETOOTH_UNAVAILABLE -> Triple(
            stringResource(R.string.status_bluetooth_unavailable),
            stringResource(R.string.dashboard_status_bluetooth_unavailable_body),
            StatusCardTone.ERROR
        )
        environment.bluetoothSupport == BleSupportStatus.BLE_UNSUPPORTED -> Triple(
            stringResource(R.string.status_ble_unsupported),
            stringResource(R.string.dashboard_status_bluetooth_unavailable_body),
            StatusCardTone.ERROR
        )
        environment.bluetoothPower == BluetoothPowerStatus.OFF -> Triple(
            stringResource(R.string.status_bluetooth_off),
            stringResource(R.string.dashboard_status_bluetooth_off_body),
            StatusCardTone.ATTENTION
        )
        environment.bluetoothPower == BluetoothPowerStatus.ON -> Triple(
            stringResource(R.string.dashboard_status_bluetooth_ready),
            stringResource(R.string.dashboard_status_bluetooth_ready_body),
            StatusCardTone.POSITIVE
        )
        else -> Triple(
            stringResource(R.string.status_checking),
            stringResource(R.string.dashboard_status_bluetooth_unknown_body),
            StatusCardTone.NEUTRAL
        )
    }
    return DashboardStatusCardData(
        title = stringResource(R.string.dashboard_status_bluetooth_title),
        state = state,
        description = description,
        icon = Icons.Filled.Info,
        action = CompactAction(
            label = stringResource(R.string.dashboard_status_open_settings),
            icon = Icons.Filled.Settings,
            onClick = onOpenSettings
        ),
        tone = tone
    )
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
        // Derived body metrics are intentionally omitted until their policy
        // is validated; the hero stays focused on the measured value.
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
            CompactActionButton(
                action = CompactAction(
                    label = stringResource(R.string.create_goal),
                    icon = Icons.Filled.Edit,
                    onClick = onGoals
                )
            )
        }
    }
}

@Composable
private fun EmptyDashboard(
    title: String,
    body: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    EmptyState(
        title = title,
        body = body,
        actionLabel = action,
        onAction = onAction
    )
}

@Composable
private fun TrendUnavailableState(
    measurementCount: Int,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = stringResource(R.string.dashboard_trend_unavailable_title),
        body = pluralStringResource(
            R.plurals.dashboard_trend_unavailable_body,
            measurementCount,
            measurementCount
        ),
        modifier = modifier
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
