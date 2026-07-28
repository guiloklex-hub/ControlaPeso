package br.com.paivalab.controlapeso.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectAvailability
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.SettingsItem

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeChange: (ThemeMode) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onEffectsChange: (VisualEffects) -> Unit,
    onDefaultUnitChange: (WeightUnit) -> Unit,
    onDefaultProfileChange: (String) -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    onConfirmSaveChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onHistoryPeriodChange: (HistoryPeriod) -> Unit,
    onMovingAverageChange: (Boolean) -> Unit,
    onGroupingChange: (HistoryGrouping) -> Unit,
    onChartSizeChange: (ChartSize) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onDetailedLogsChange: (Boolean) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderDayToggle: (Int) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit,
    onRequestHealthPermission: () -> Unit,
    onHealthEnabledChange: (Boolean) -> Unit,
    onHealthSync: () -> Unit,
    diagnosticLoggingAvailable: Boolean,
    demoAvailable: Boolean,
    onOpenDemoMeasurement: () -> Unit,
    onClearDemoData: () -> Unit,
    onDismissMessage: () -> Unit,
    onProfiles: () -> Unit,
    onGoals: () -> Unit,
    onDevices: () -> Unit,
    onDiagnostic: () -> Unit,
    onReports: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences = state.preferences
    val snackbarHostState = remember { SnackbarHostState() }
    val message = when (state.message) {
        SettingsMessage.HEALTH_SYNC_COMPLETE -> pluralStringResource(
            R.plurals.health_sync_complete,
            state.healthSyncedCount,
            state.healthSyncedCount
        )
        SettingsMessage.HEALTH_PERMISSION_REQUIRED ->
            stringResource(R.string.health_permission_required)
        SettingsMessage.HEALTH_SYNC_FAILED -> pluralStringResource(
            R.plurals.health_sync_failed,
            state.healthSyncedCount,
            state.healthSyncedCount
        )
        SettingsMessage.DEMO_DATA_CLEARED -> stringResource(R.string.demo_data_cleared)
        null -> null
    }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onDismissMessage()
        }
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val horizontalPadding = when {
            maxWidth >= 840.dp -> ControlaPesoDesignSystem.sizes.expandedContentPadding
            maxWidth >= 600.dp -> ControlaPesoDesignSystem.sizes.mediumContentPadding
            else -> ControlaPesoDesignSystem.sizes.compactContentPadding
        }
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .widthIn(max = 840.dp),
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
                    stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                SettingsSection(stringResource(R.string.appearance_section)) {
                    Text(stringResource(R.string.theme_label), fontWeight = FontWeight.SemiBold)
                    ChoiceRow(
                        values = ThemeMode.entries,
                        selected = preferences.themeMode,
                        label = {
                            stringResource(
                                when (it) {
                                    ThemeMode.SYSTEM -> R.string.theme_system_short
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                }
                            )
                        },
                        onSelected = onThemeChange
                    )
                    SettingSwitch(
                        label = stringResource(R.string.dynamic_colors),
                        checked = preferences.dynamicColors,
                        onCheckedChange = onDynamicColorsChange
                    )
                    SettingSwitch(
                        label = stringResource(R.string.reduced_effects),
                        checked = preferences.visualEffects == VisualEffects.REDUCED,
                        onCheckedChange = {
                            onEffectsChange(
                                if (it) VisualEffects.REDUCED else VisualEffects.FULL
                            )
                        }
                    )
                    SettingSwitch(
                        label = stringResource(R.string.high_contrast),
                        checked = preferences.highContrast,
                        onCheckedChange = onHighContrastChange
                    )
                    Text(stringResource(R.string.chart_size), fontWeight = FontWeight.SemiBold)
                    ChoiceRow(
                        values = ChartSize.entries,
                        selected = preferences.chartSize,
                        label = {
                            stringResource(
                                when (it) {
                                    ChartSize.COMPACT -> R.string.size_compact
                                    ChartSize.COMFORTABLE -> R.string.size_comfortable
                                    ChartSize.LARGE -> R.string.size_large
                                }
                            )
                        },
                        onSelected = onChartSizeChange
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.measurement_settings_section)) {
                    if (state.profiles.isNotEmpty()) {
                        Text(
                            stringResource(R.string.default_profile),
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.profiles.forEach { profile ->
                                FilterChip(
                                    selected = profile.isActive,
                                    onClick = { onDefaultProfileChange(profile.id) },
                                    label = { Text(profile.name) }
                                )
                            }
                        }
                    }
                    Text(stringResource(R.string.default_unit), fontWeight = FontWeight.SemiBold)
                    ChoiceRow(
                        values = WeightUnit.entries,
                        selected = preferences.defaultWeightUnit,
                        label = { it.symbol },
                        onSelected = onDefaultUnitChange
                    )
                    SettingSwitch(
                        label = stringResource(R.string.auto_save_stable),
                        checked = preferences.autoSaveStableMeasurement,
                        onCheckedChange = onAutoSaveChange
                    )
                    SettingSwitch(
                        label = stringResource(R.string.confirm_before_save),
                        checked = preferences.confirmBeforeSaving,
                        onCheckedChange = onConfirmSaveChange
                    )
                    SettingSwitch(
                        label = stringResource(R.string.vibration_feedback),
                        checked = preferences.vibrationEnabled,
                        onCheckedChange = onVibrationChange
                    )
                    SettingSwitch(
                        label = stringResource(R.string.sound_feedback),
                        checked = preferences.soundEnabled,
                        onCheckedChange = onSoundChange
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.history_settings_section)) {
                    Text(
                        stringResource(R.string.default_history_period),
                        fontWeight = FontWeight.SemiBold
                    )
                    ChoiceRow(
                        values = HistoryPeriod.entries,
                        selected = preferences.defaultHistoryPeriod,
                        label = { periodLabel(it) },
                        onSelected = onHistoryPeriodChange
                    )
                    SettingSwitch(
                        label = stringResource(R.string.moving_average),
                        checked = preferences.movingAverageEnabled,
                        onCheckedChange = onMovingAverageChange
                    )
                    Text(stringResource(R.string.history_grouping), fontWeight = FontWeight.SemiBold)
                    ChoiceRow(
                        values = HistoryGrouping.entries,
                        selected = preferences.historyGrouping,
                        label = {
                            stringResource(
                                when (it) {
                                    HistoryGrouping.NONE -> R.string.group_none
                                    HistoryGrouping.DAY -> R.string.group_day
                                    HistoryGrouping.MONTH -> R.string.group_month
                                }
                            )
                        },
                        onSelected = onGroupingChange
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.reminders_section)) {
                    SettingSwitch(
                        label = stringResource(R.string.enable_reminders),
                        checked = preferences.remindersEnabled,
                        onCheckedChange = onReminderEnabledChange
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val dayLabels = listOf(
                            R.string.day_mon,
                            R.string.day_tue,
                            R.string.day_wed,
                            R.string.day_thu,
                            R.string.day_fri,
                            R.string.day_sat,
                            R.string.day_sun
                        )
                        dayLabels.forEachIndexed { index, label ->
                            FilterChip(
                                selected =
                                    preferences.reminderDaysMask and (1 shl index) != 0,
                                onClick = { onReminderDayToggle(index) },
                                enabled = preferences.remindersEnabled,
                                label = { Text(stringResource(label)) }
                            )
                        }
                    }
                    Text(stringResource(R.string.reminder_time), fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        NumberStepper(
                            label = stringResource(R.string.hour),
                            value = preferences.reminderHour,
                            range = 0..23,
                            onValueChange = {
                                onReminderTimeChange(it, preferences.reminderMinute)
                            }
                        )
                        NumberStepper(
                            label = stringResource(R.string.minute),
                            value = preferences.reminderMinute,
                            range = 0..59,
                            step = 5,
                            onValueChange = {
                                onReminderTimeChange(preferences.reminderHour, it)
                            }
                        )
                    }
                    Text(
                        stringResource(R.string.reminder_approximate_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.integrations_section)) {
                    Text(
                        text = stringResource(
                            when (state.healthAvailability) {
                                HealthConnectAvailability.AVAILABLE ->
                                    R.string.health_available
                                HealthConnectAvailability.NOT_INSTALLED ->
                                    R.string.health_not_installed
                                HealthConnectAvailability.UPDATE_REQUIRED ->
                                    R.string.health_update_required
                                HealthConnectAvailability.ANDROID_VERSION_UNSUPPORTED ->
                                    R.string.health_android_unsupported
                            }
                        )
                    )
                    if (
                        state.healthAvailability == HealthConnectAvailability.AVAILABLE &&
                        state.activeProfile != null
                    ) {
                        if (!state.healthPermissionGranted) {
                            Button(onClick = onRequestHealthPermission) {
                                Text(stringResource(R.string.health_request_permission))
                            }
                        } else {
                            SettingSwitch(
                                label = stringResource(R.string.health_enable_profile),
                                checked = state.activeProfile.healthConnectEnabled,
                                onCheckedChange = onHealthEnabledChange
                            )
                            Button(
                                onClick = onHealthSync,
                                enabled = state.activeProfile.healthConnectEnabled &&
                                    !state.isHealthSyncing
                            ) {
                                Text(
                                    stringResource(
                                        if (state.isHealthSyncing) {
                                            R.string.health_syncing
                                        } else {
                                            R.string.health_sync_now
                                        }
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.health_local_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.diagnostic_settings_section)) {
                    if (diagnosticLoggingAvailable) {
                        SettingSwitch(
                            label = stringResource(R.string.detailed_ble_logs),
                            checked = preferences.detailedBleLogs,
                            onCheckedChange = onDetailedLogsChange
                        )
                    }
                    Text(
                        stringResource(R.string.detailed_logs_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (demoAvailable) {
                item {
                    SettingsSection(stringResource(R.string.demo_section)) {
                        Text(
                            stringResource(R.string.demo_debug_notice),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onOpenDemoMeasurement,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.demo_measurement))
                        }
                        OutlinedButton(
                            onClick = onClearDemoData,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.clear_demo_data))
                        }
                    }
                }
            }
            items(
                listOf(
                    R.string.profiles_title to onProfiles,
                    R.string.goals_title to onGoals,
                    R.string.devices_title to onDevices,
                    R.string.bluetooth_diagnostic to onDiagnostic,
                    R.string.reports_title to onReports,
                    R.string.privacy_title to onPrivacy,
                    R.string.about_title to onAbout
                )
            ) { (label, action) ->
                SettingsItem(
                    title = stringResource(label),
                    onClick = action
                )
            }
        }
        SnackbarHost(
            snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun <T> ChoiceRow(
    values: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        values.forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(label(value)) }
            )
        }
    }
}

@Composable
private fun periodLabel(period: HistoryPeriod): String = stringResource(
    when (period) {
        HistoryPeriod.DAYS_7 -> R.string.period_7_days
        HistoryPeriod.DAYS_30 -> R.string.period_30_days
        HistoryPeriod.MONTHS_3 -> R.string.period_3_months
        HistoryPeriod.MONTHS_6 -> R.string.period_6_months
        HistoryPeriod.YEAR_1 -> R.string.period_1_year
        HistoryPeriod.ALL -> R.string.period_all
    }
)

@Composable
private fun NumberStepper(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    step: Int = 1
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onValueChange((value - step).coerceAtLeast(range.first)) },
                enabled = value > range.first
            ) {
                Text("−")
            }
            Text(
                text = value.toString().padStart(2, '0'),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            OutlinedButton(
                onClick = { onValueChange((value + step).coerceAtMost(range.last)) },
                enabled = value < range.last
            ) {
                Text("+")
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.xs
        )
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.sm
            )
        ) {
            content()
        }
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ControlaPesoDesignSystem.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
