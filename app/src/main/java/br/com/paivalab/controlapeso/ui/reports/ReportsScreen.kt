package br.com.paivalab.controlapeso.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.data.export.ReportFormat
import br.com.paivalab.controlapeso.ui.components.BrazilianDateTextField
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList
import br.com.paivalab.controlapeso.ui.designsystem.components.SettingsSection

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onProfileChange: (String) -> Unit,
    onPeriodChange: (ReportPeriod) -> Unit,
    onCustomStartChange: (String) -> Unit,
    onCustomEndChange: (String) -> Unit,
    onFormatChange: (ReportFormat) -> Unit,
    onIncludeChartChange: (Boolean) -> Unit,
    onIncludeTableChange: (Boolean) -> Unit,
    onIncludeNotesChange: (Boolean) -> Unit,
    onGenerate: () -> Unit,
    onShare: () -> Unit,
    onShareSummary: () -> Unit,
    onSaveFile: () -> Unit,
    onOpenDataBackup: () -> Unit,
    onDismissMessage: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFormatDialog by rememberSaveable { mutableStateOf(false) }
    var showPeriodDialog by rememberSaveable { mutableStateOf(false) }
    var showContentDialog by rememberSaveable { mutableStateOf(false) }
    ResponsiveScreenList(
        modifier = modifier,
        maxContentWidth = 1_000.dp
    ) {
        item {
            Text(
                stringResource(R.string.reports_title),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                stringResource(R.string.reports_local_notice),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            SettingsSection(
                title = stringResource(R.string.report_configuration_title),
                supportingText = stringResource(R.string.report_configuration_body)
            ) {
                    Text(stringResource(R.string.report_format), fontWeight = FontWeight.SemiBold)
                    CompactActionButton(
                        CompactAction(
                            label = stringResource(
                                R.string.report_format_selector,
                                reportFormatText(state.format)
                            ),
                            icon = Icons.Filled.Info,
                            onClick = { showFormatDialog = true }
                        )
                    )
                    if (state.format == ReportFormat.JSON) {
                        Text(stringResource(R.string.json_backup_all_data))
                    } else {
                        Text(stringResource(R.string.profile_label), fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.profiles.forEach { profile ->
                                FilterChip(
                                    selected = state.selectedProfileId == profile.id,
                                    onClick = { onProfileChange(profile.id) },
                                    label = { Text(profile.name) },
                                    modifier = Modifier.heightIn(
                                        min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
                                    )
                                )
                            }
                        }
                        Text(stringResource(R.string.report_period), fontWeight = FontWeight.SemiBold)
                        CompactActionButton(
                            CompactAction(
                                label = stringResource(
                                    R.string.report_period_selector,
                                    reportPeriodText(state.period)
                                ),
                                icon = Icons.Filled.Info,
                                onClick = { showPeriodDialog = true }
                            )
                        )
                        if (state.period == ReportPeriod.CUSTOM) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                BrazilianDateTextField(
                                    value = state.customStartText,
                                    onValueChange = onCustomStartChange,
                                    label = stringResource(R.string.start_date),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                BrazilianDateTextField(
                                    value = state.customEndText,
                                    onValueChange = onCustomEndChange,
                                    label = stringResource(R.string.end_date),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.report_unit_global, state.unit.symbol),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            CompactActionButton(
                                CompactAction(
                                    label = stringResource(R.string.change_in_settings),
                                    icon = Icons.Filled.Settings,
                                    onClick = onOpenSettings
                                )
                            )
                        }
                        Text(stringResource(R.string.report_content_label), fontWeight = FontWeight.SemiBold)
                        Text(
                            reportContentSummary(state),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CompactActionButton(
                            CompactAction(
                                label = stringResource(R.string.report_content_selector),
                                icon = Icons.Filled.Edit,
                                onClick = { showContentDialog = true }
                            )
                        )
                    }
                    if (state.format == ReportFormat.JSON) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.report_unit_global, state.unit.symbol),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            CompactActionButton(
                                CompactAction(
                                    label = stringResource(R.string.change_in_settings),
                                    icon = Icons.Filled.Settings,
                                    onClick = onOpenSettings
                                )
                            )
                        }
                    }
                    CompactActionButton(
                        CompactAction(
                            label = stringResource(
                                if (state.isWorking) R.string.working
                                else R.string.generate_report
                            ),
                            icon = Icons.Filled.AddCircle,
                            onClick = onGenerate,
                            primary = true,
                            enabled = !state.isWorking &&
                                (state.format == ReportFormat.JSON ||
                                    state.profiles.isNotEmpty())
                        )
                    )
            }
        }
        item {
            SettingsSection(
                title = stringResource(R.string.backup_status_title),
                supportingText = stringResource(R.string.backup_status_body)
            ) {
                Text(stringResource(R.string.backup_status_manual_notice))
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.open_data_backup),
                        icon = Icons.Filled.Build,
                        onClick = onOpenDataBackup
                    )
                )
            }
        }
        state.generatedFile?.let { generated ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(
                            ControlaPesoDesignSystem.spacing.xs
                        )
                    ) {
                        Text(
                            stringResource(R.string.generated_file, generated.displayName),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.report_review_title),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            state.generatedSummaryText
                                ?: stringResource(R.string.json_backup_all_data)
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CompactActionButton(
                                CompactAction(
                                    label = stringResource(R.string.share),
                                    icon = Icons.Filled.Share,
                                    onClick = onShare,
                                    primary = true
                                )
                            )
                            if (state.generatedSummaryText != null) {
                                CompactActionButton(
                                    CompactAction(
                                        label = stringResource(R.string.share_summary),
                                        icon = Icons.Filled.Info,
                                        onClick = onShareSummary
                                    )
                                )
                            }
                            CompactActionButton(
                                CompactAction(
                                    label = stringResource(R.string.save_copy),
                                    icon = Icons.Filled.Edit,
                                    onClick = onSaveFile
                                )
                            )
                        }
                    }
                }
            }
        }
        state.message?.let {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(reportMessageText(it), modifier = Modifier.weight(1f))
                        TextButton(onClick = onDismissMessage) {
                            Text(stringResource(R.string.dismiss_error))
                        }
                    }
                }
            }
        }
        state.error?.let {
            item {
                ErrorState(
                    title = stringResource(R.string.report_error_title),
                    body = state.validationErrors
                        .take(5)
                        .joinToString(separator = "\n")
                        .ifBlank { reportErrorText(it) },
                    actionLabel = stringResource(R.string.dismiss_error),
                    onAction = onDismissMessage,
                    actionIcon = Icons.Filled.Close
                )
            }
        }
    }

    if (showFormatDialog) {
        AlertDialog(
            onDismissRequest = { showFormatDialog = false },
            title = { Text(stringResource(R.string.report_format)) },
            text = {
                Column {
                    ReportFormat.entries.forEach { format ->
                        ReportOptionRow(
                            label = reportFormatText(format),
                            selected = state.format == format,
                            onClick = {
                                onFormatChange(format)
                                showFormatDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFormatDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPeriodDialog) {
        AlertDialog(
            onDismissRequest = { showPeriodDialog = false },
            title = { Text(stringResource(R.string.report_period)) },
            text = {
                Column {
                    ReportPeriod.entries.forEach { period ->
                        ReportOptionRow(
                            label = reportPeriodText(period),
                            selected = state.period == period,
                            onClick = {
                                onPeriodChange(period)
                                showPeriodDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPeriodDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showContentDialog) {
        AlertDialog(
            onDismissRequest = { showContentDialog = false },
            title = { Text(stringResource(R.string.report_content_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.report_content_dialog_body))
                    if (state.format == ReportFormat.PDF) {
                        ReportCheckbox(
                            label = stringResource(R.string.include_chart),
                            checked = state.includeChart,
                            onCheckedChange = onIncludeChartChange
                        )
                        ReportCheckbox(
                            label = stringResource(R.string.include_table),
                            checked = state.includeTable,
                            onCheckedChange = onIncludeTableChange
                        )
                    }
                    ReportCheckbox(
                        label = stringResource(R.string.include_notes),
                        checked = state.includeNotes,
                        onCheckedChange = onIncludeNotesChange
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showContentDialog = false }) {
                    Text(stringResource(R.string.save))
                }
            }
        )
    }

}

@Composable
private fun ReportCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ControlaPesoDesignSystem.sizes.minimumTouchTarget)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ReportOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ControlaPesoDesignSystem.sizes.minimumTouchTarget)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label)
    }
}

@Composable
private fun reportPeriodText(period: ReportPeriod): String = stringResource(
    when (period) {
        ReportPeriod.DAYS_7 -> R.string.period_7_days
        ReportPeriod.DAYS_30 -> R.string.period_30_days
        ReportPeriod.MONTHS_3 -> R.string.period_3_months
        ReportPeriod.MONTHS_6 -> R.string.period_6_months
        ReportPeriod.YEAR_1 -> R.string.period_1_year
        ReportPeriod.ALL -> R.string.period_all
        ReportPeriod.CUSTOM -> R.string.period_custom
    }
)

@Composable
private fun reportFormatText(format: ReportFormat): String = stringResource(
    when (format) {
        ReportFormat.PDF -> R.string.report_format_pdf
        ReportFormat.CSV -> R.string.report_format_csv
        ReportFormat.JSON -> R.string.report_format_json
    }
)

@Composable
private fun reportContentSummary(state: ReportsUiState): String {
    if (state.format == ReportFormat.JSON) {
        return stringResource(R.string.json_backup_all_data)
    }
    val selected = buildList {
        if (state.format == ReportFormat.PDF && state.includeChart) {
            add(stringResource(R.string.include_chart))
        }
        if (state.format == ReportFormat.PDF && state.includeTable) {
            add(stringResource(R.string.include_table))
        }
        if (state.includeNotes) add(stringResource(R.string.include_notes))
    }
    return selected.takeIf { it.isNotEmpty() }?.joinToString(" · ")
        ?: stringResource(R.string.report_content_none)
}

@Composable
private fun reportMessageText(message: ReportsMessage): String = stringResource(
    when (message) {
        ReportsMessage.GENERATED -> R.string.message_report_generated
        ReportsMessage.SAVED -> R.string.message_file_saved
        ReportsMessage.RESTORED_MERGE -> R.string.message_restore_merge
        ReportsMessage.RESTORED_REPLACE -> R.string.message_restore_replace
        ReportsMessage.CACHE_CLEARED -> R.string.message_cache_cleared
    }
)

@Composable
private fun reportErrorText(error: ReportsError): String = stringResource(
    when (error) {
        ReportsError.PROFILE_REQUIRED -> R.string.error_profile_required
        ReportsError.INVALID_PERIOD -> R.string.invalid_custom_period
        ReportsError.NO_MEASUREMENTS -> R.string.error_report_no_measurements
        ReportsError.GENERATION_FAILED -> R.string.error_report_generation
        ReportsError.FILE_READ_FAILED -> R.string.error_file_read
        ReportsError.FILE_WRITE_FAILED -> R.string.error_file_write
        ReportsError.RESTORE_FAILED -> R.string.error_restore
    }
)
