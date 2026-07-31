package br.com.paivalab.controlapeso.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.BrazilianDateTimeFormatter
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectAvailability
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.SettingsSection

private enum class SettingsSectionKey {
    APPEARANCE,
    MEASUREMENT,
    HISTORY,
    REMINDERS,
    INTEGRATIONS,
    DIAGNOSTICS
}

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
    onManageHealthPermissions: () -> Unit = {},
    onHealthEnabledChange: (Boolean) -> Unit,
    onHealthSync: () -> Unit,
    onConfirmHealthSync: () -> Unit = {},
    onDismissHealthSync: () -> Unit = {},
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
    onDataBackup: () -> Unit = {},
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    onReviewOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val preferences = state.preferences
    var selectedSection by remember { mutableStateOf<SettingsSectionKey?>(null) }
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
        val landingColumns = if (
            maxWidth >= 840.dp && LocalDensity.current.fontScale < 1.5f
        ) 2 else 1
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
            if (selectedSection == null) {
                item {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        stringResource(R.string.settings_landing_body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    SettingsLandingGrid(
                        columns = landingColumns,
                        items = listOf(
                            SettingsLandingItem(
                                title = stringResource(R.string.appearance_section),
                                body = stringResource(R.string.settings_appearance_summary),
                                icon = Icons.Filled.Settings,
                                onClick = { selectedSection = SettingsSectionKey.APPEARANCE }
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.measurement_settings_section),
                                body = stringResource(R.string.settings_measurement_summary),
                                icon = Icons.Filled.AddCircle,
                                onClick = { selectedSection = SettingsSectionKey.MEASUREMENT }
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.history_settings_section),
                                body = stringResource(R.string.settings_history_summary),
                                icon = Icons.AutoMirrored.Filled.List,
                                onClick = { selectedSection = SettingsSectionKey.HISTORY }
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.reminders_section),
                                body = stringResource(R.string.settings_reminders_summary),
                                icon = Icons.Filled.Notifications,
                                onClick = { selectedSection = SettingsSectionKey.REMINDERS }
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.integrations_section),
                                body = stringResource(R.string.settings_integrations_summary),
                                icon = Icons.Filled.Build,
                                onClick = { selectedSection = SettingsSectionKey.INTEGRATIONS }
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.profiles_title),
                                body = stringResource(R.string.settings_profiles_summary),
                                icon = Icons.Filled.Person,
                                onClick = onProfiles
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.goals_title),
                                body = stringResource(R.string.settings_goals_summary),
                                icon = Icons.Filled.AddCircle,
                                onClick = onGoals
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.devices_title),
                                body = stringResource(R.string.settings_devices_summary),
                                icon = Icons.Filled.Build,
                                onClick = onDevices
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.reports_title),
                                body = stringResource(R.string.settings_reports_summary),
                                icon = Icons.Filled.Info,
                                onClick = onReports
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.data_backup_title),
                                body = stringResource(R.string.settings_backup_summary),
                                icon = Icons.Filled.Info,
                                onClick = onDataBackup
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.privacy_title),
                                body = stringResource(R.string.settings_privacy_summary),
                                icon = Icons.Filled.Lock,
                                onClick = onPrivacy
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.diagnostic_settings_section),
                                body = stringResource(R.string.settings_diagnostic_summary),
                                icon = Icons.Filled.Build,
                                onClick = { selectedSection = SettingsSectionKey.DIAGNOSTICS }
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.about_title),
                                body = stringResource(R.string.settings_about_summary),
                                icon = Icons.Filled.Info,
                                onClick = onAbout
                            ),
                            SettingsLandingItem(
                                title = stringResource(R.string.review_onboarding),
                                body = stringResource(R.string.settings_review_summary),
                                icon = Icons.Filled.PlayArrow,
                                onClick = onReviewOnboarding
                            )
                        )
                    )
                }
                if (demoAvailable) {
                    item {
                        SettingsSection(
                            title = stringResource(R.string.demo_section),
                            supportingText = stringResource(R.string.demo_debug_notice)
                        ) {
                            CompactActionButton(
                                CompactAction(
                                    label = stringResource(R.string.demo_measurement),
                                    icon = Icons.Filled.AddCircle,
                                    onClick = onOpenDemoMeasurement,
                                    primary = true
                                )
                            )
                            CompactActionButton(
                                CompactAction(
                                    label = stringResource(R.string.clear_demo_data),
                                    icon = Icons.Filled.Delete,
                                    onClick = onClearDemoData
                                )
                            )
                        }
                    }
                }
            } else {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedSection = null }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                        Text(
                            settingsSectionTitle(requireNotNull(selectedSection)),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                when (selectedSection) {
                    SettingsSectionKey.APPEARANCE -> item {
                        AppearanceSettings(
                            preferences = preferences,
                            onThemeChange = onThemeChange,
                            onDynamicColorsChange = onDynamicColorsChange,
                            onEffectsChange = onEffectsChange,
                            onHighContrastChange = onHighContrastChange,
                            onChartSizeChange = onChartSizeChange
                        )
                    }
                    SettingsSectionKey.MEASUREMENT -> item {
                        MeasurementSettings(
                            state = state,
                            onDefaultUnitChange = onDefaultUnitChange,
                            onDefaultProfileChange = onDefaultProfileChange,
                            onAutoSaveChange = onAutoSaveChange,
                            onConfirmSaveChange = onConfirmSaveChange,
                            onVibrationChange = onVibrationChange,
                            onSoundChange = onSoundChange
                        )
                    }
                    SettingsSectionKey.HISTORY -> item {
                        HistorySettings(
                            preferences = preferences,
                            onHistoryPeriodChange = onHistoryPeriodChange,
                            onMovingAverageChange = onMovingAverageChange,
                            onGroupingChange = onGroupingChange
                        )
                    }
                    SettingsSectionKey.REMINDERS -> item {
                        ReminderSettings(
                            preferences = preferences,
                            onReminderEnabledChange = onReminderEnabledChange,
                            onReminderDayToggle = onReminderDayToggle,
                            onReminderTimeChange = onReminderTimeChange
                        )
                    }
                    SettingsSectionKey.INTEGRATIONS -> item {
                        IntegrationSettings(
                            state = state,
                            onRequestHealthPermission = onRequestHealthPermission,
                            onManageHealthPermissions = onManageHealthPermissions,
                            onHealthEnabledChange = onHealthEnabledChange,
                            onHealthSync = onHealthSync
                        )
                    }
                    SettingsSectionKey.DIAGNOSTICS -> item {
                        DiagnosticSettings(
                            state = state,
                            available = diagnosticLoggingAvailable,
                            onDetailedLogsChange = onDetailedLogsChange,
                            onDiagnostic = onDiagnostic
                        )
                    }
                    else -> Unit
                }
            }
        }
        SnackbarHost(
            snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.healthSyncConfirmationVisible) {
        AlertDialog(
            onDismissRequest = onDismissHealthSync,
            title = { Text(stringResource(R.string.health_sync_preview_title)) },
            text = {
                if (state.healthSyncMeasurementCount == 0) {
                    Text(stringResource(R.string.health_sync_preview_empty))
                } else {
                    val first = state.healthSyncFirstAt?.let {
                        BrazilianDateTimeFormatter.dateTime(
                            it.atZone(java.time.ZoneId.systemDefault())
                        )
                    }.orEmpty()
                    val last = state.healthSyncLastAt?.let {
                        BrazilianDateTimeFormatter.dateTime(
                            it.atZone(java.time.ZoneId.systemDefault())
                        )
                    }.orEmpty()
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            ControlaPesoDesignSystem.spacing.sm
                        )
                    ) {
                        Text(
                            pluralStringResource(
                                R.plurals.health_sync_preview_count,
                                state.healthSyncMeasurementCount,
                                state.healthSyncMeasurementCount,
                                first,
                                last
                            )
                        )
                        Text(stringResource(R.string.health_sync_preview_notice))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmHealthSync,
                    enabled = state.healthSyncMeasurementCount > 0 &&
                        !state.isHealthSyncing
                ) {
                    Text(stringResource(R.string.health_sync_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissHealthSync) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private data class SettingsLandingItem(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun SettingsLandingGrid(
    items: List<SettingsLandingItem>,
    columns: Int,
    modifier: Modifier = Modifier
) {
    if (columns == 2) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.md
            )
        ) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ControlaPesoDesignSystem.spacing.md
                    )
                ) {
                    rowItems.forEach { item ->
                        SettingsLandingCard(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.md
            )
        ) {
            items.forEach { item -> SettingsLandingCard(item = item) }
        }
    }
}

@Composable
private fun SettingsLandingCard(
    item: SettingsLandingItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = item.onClick)
            .semantics(mergeDescendants = true) { role = Role.Button },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ControlaPesoDesignSystem.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    ControlaPesoDesignSystem.spacing.xxs
                )
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun settingsSectionTitle(section: SettingsSectionKey): String = stringResource(
    when (section) {
        SettingsSectionKey.APPEARANCE -> R.string.appearance_section
        SettingsSectionKey.MEASUREMENT -> R.string.measurement_settings_section
        SettingsSectionKey.HISTORY -> R.string.history_settings_section
        SettingsSectionKey.REMINDERS -> R.string.reminders_section
        SettingsSectionKey.INTEGRATIONS -> R.string.integrations_section
        SettingsSectionKey.DIAGNOSTICS -> R.string.diagnostic_settings_section
    }
)

@Composable
private fun AppearanceSettings(
    preferences: br.com.paivalab.controlapeso.data.preferences.AppPreferences,
    onThemeChange: (ThemeMode) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onEffectsChange: (VisualEffects) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onChartSizeChange: (ChartSize) -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.appearance_section),
        supportingText = stringResource(R.string.settings_appearance_detail)
    ) {
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
                onEffectsChange(if (it) VisualEffects.REDUCED else VisualEffects.FULL)
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

@Composable
private fun MeasurementSettings(
    state: SettingsUiState,
    onDefaultUnitChange: (WeightUnit) -> Unit,
    onDefaultProfileChange: (String) -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    onConfirmSaveChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.measurement_settings_section),
        supportingText = stringResource(R.string.settings_measurement_detail)
    ) {
        if (state.profiles.isNotEmpty()) {
            Text(stringResource(R.string.default_profile), fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.profiles.forEach { profile ->
                    FilterChip(
                        selected = profile.isActive,
                        onClick = { onDefaultProfileChange(profile.id) },
                        label = { Text(profile.name) },
                        modifier = Modifier.heightIn(
                            min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                        )
                    )
                }
            }
        }
        Text(stringResource(R.string.default_unit), fontWeight = FontWeight.SemiBold)
        ChoiceRow(
            values = WeightUnit.entries,
            selected = state.preferences.defaultWeightUnit,
            label = { it.symbol },
            onSelected = onDefaultUnitChange
        )
        SettingSwitch(
            label = stringResource(R.string.auto_save_stable),
            checked = state.preferences.autoSaveStableMeasurement,
            onCheckedChange = onAutoSaveChange
        )
        SettingSwitch(
            label = stringResource(R.string.confirm_before_save),
            checked = state.preferences.confirmBeforeSaving,
            onCheckedChange = onConfirmSaveChange
        )
        SettingSwitch(
            label = stringResource(R.string.vibration_feedback),
            checked = state.preferences.vibrationEnabled,
            onCheckedChange = onVibrationChange
        )
        SettingSwitch(
            label = stringResource(R.string.sound_feedback),
            checked = state.preferences.soundEnabled,
            onCheckedChange = onSoundChange
        )
    }
}

@Composable
private fun HistorySettings(
    preferences: br.com.paivalab.controlapeso.data.preferences.AppPreferences,
    onHistoryPeriodChange: (HistoryPeriod) -> Unit,
    onMovingAverageChange: (Boolean) -> Unit,
    onGroupingChange: (HistoryGrouping) -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.history_settings_section),
        supportingText = stringResource(R.string.settings_history_detail)
    ) {
        Text(stringResource(R.string.default_history_period), fontWeight = FontWeight.SemiBold)
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

@Composable
private fun ReminderSettings(
    preferences: br.com.paivalab.controlapeso.data.preferences.AppPreferences,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderDayToggle: (Int) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.reminders_section),
        supportingText = stringResource(R.string.reminder_approximate_notice)
    ) {
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
                    selected = preferences.reminderDaysMask and (1 shl index) != 0,
                    onClick = { onReminderDayToggle(index) },
                    enabled = preferences.remindersEnabled,
                    label = { Text(stringResource(label)) },
                    modifier = Modifier.heightIn(
                        min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                    )
                )
            }
        }
        Text(stringResource(R.string.reminder_time), fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NumberStepper(
                label = stringResource(R.string.hour),
                value = preferences.reminderHour,
                range = 0..23,
                onValueChange = { onReminderTimeChange(it, preferences.reminderMinute) }
            )
            NumberStepper(
                label = stringResource(R.string.minute),
                value = preferences.reminderMinute,
                range = 0..59,
                step = 5,
                onValueChange = { onReminderTimeChange(preferences.reminderHour, it) }
            )
        }
    }
}

@Composable
private fun IntegrationSettings(
    state: SettingsUiState,
    onRequestHealthPermission: () -> Unit,
    onManageHealthPermissions: () -> Unit,
    onHealthEnabledChange: (Boolean) -> Unit,
    onHealthSync: () -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.integrations_section),
        supportingText = stringResource(R.string.settings_integrations_detail)
    ) {
        Text(
            text = stringResource(
                when (state.healthAvailability) {
                    HealthConnectAvailability.AVAILABLE -> R.string.health_available
                    HealthConnectAvailability.NOT_INSTALLED -> R.string.health_not_installed
                    HealthConnectAvailability.UPDATE_REQUIRED -> R.string.health_update_required
                    HealthConnectAvailability.ANDROID_VERSION_UNSUPPORTED ->
                        R.string.health_android_unsupported
                }
            )
        )
        Text(
            stringResource(
                if (state.healthPermissionGranted) {
                    R.string.health_permission_status_authorized
                } else {
                    R.string.health_permission_status_not_authorized
                }
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
                state.preferences.healthLastSyncAt?.let {
                stringResource(
                    R.string.health_last_sync_at,
                    BrazilianDateTimeFormatter.dateTime(
                        it.atZone(java.time.ZoneId.systemDefault())
                    )
                )
            } ?: stringResource(R.string.health_last_sync_never),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.healthSyncError?.let { error ->
            Text(
                stringResource(
                    when (error) {
                        HealthSyncError.PERMISSION_REQUIRED ->
                            R.string.health_sync_error_permission
                        HealthSyncError.UNAVAILABLE -> R.string.health_sync_error_unavailable
                        HealthSyncError.FAILED -> R.string.health_sync_error_failed
                    }
                ),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (
            state.healthAvailability == HealthConnectAvailability.AVAILABLE &&
            state.activeProfile != null
        ) {
            if (!state.healthPermissionGranted) {
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.health_request_permission),
                        icon = Icons.Filled.Info,
                        onClick = onRequestHealthPermission,
                        primary = true
                    )
                )
            } else {
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.health_manage_permissions),
                        icon = Icons.Filled.Settings,
                        onClick = onManageHealthPermissions
                    )
                )
                SettingSwitch(
                    label = stringResource(R.string.health_record_new_measurements),
                    checked = state.activeProfile.healthConnectEnabled,
                    onCheckedChange = onHealthEnabledChange
                )
                CompactActionButton(
                    CompactAction(
                        label = stringResource(
                            if (state.isHealthSyncing) R.string.health_syncing
                            else R.string.health_sync_now
                        ),
                        icon = Icons.Filled.Refresh,
                        onClick = onHealthSync,
                        primary = true,
                        enabled = state.activeProfile.healthConnectEnabled &&
                            !state.isHealthSyncing &&
                            state.healthSyncMeasurementCount > 0
                    )
                )
                if (state.healthSyncMeasurementCount == 0) {
                    Text(
                        stringResource(R.string.health_sync_no_local_data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun DiagnosticSettings(
    state: SettingsUiState,
    available: Boolean,
    onDetailedLogsChange: (Boolean) -> Unit,
    onDiagnostic: () -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.diagnostic_settings_section),
        supportingText = stringResource(R.string.settings_diagnostic_detail)
    ) {
        if (available) {
            SettingSwitch(
                label = stringResource(R.string.detailed_ble_logs),
                checked = state.preferences.detailedBleLogs,
                onCheckedChange = onDetailedLogsChange
            )
        }
        Text(stringResource(R.string.detailed_logs_notice))
        CompactActionButton(
            CompactAction(
                label = stringResource(R.string.open_diagnostic),
                icon = Icons.Filled.Build,
                onClick = onDiagnostic
            )
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
                label = { Text(label(value)) },
                modifier = Modifier.heightIn(
                    min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                )
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
            TextButton(
                onClick = { onValueChange((value - step).coerceAtLeast(range.first)) },
                enabled = value > range.first
            ) {
                Text("−")
            }
            Text(
                text = value.toString().padStart(2, '0'),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            TextButton(
                onClick = { onValueChange((value + step).coerceAtMost(range.last)) },
                enabled = value < range.last
            ) {
                Text("+")
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
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .heightIn(min = ControlaPesoDesignSystem.sizes.minimumTouchTarget)
            .padding(vertical = ControlaPesoDesignSystem.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
