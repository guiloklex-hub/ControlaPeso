package br.com.paivalab.controlapeso.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.data.backup.RestoreMode
import br.com.paivalab.controlapeso.data.export.ReportFormat
import br.com.paivalab.controlapeso.domain.model.WeightUnit

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
    onIncludeMetricsChange: (Boolean) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    onGenerate: () -> Unit,
    onShare: () -> Unit,
    onShareSummary: () -> Unit,
    onSaveFile: () -> Unit,
    onImport: () -> Unit,
    onClearTemporaryFiles: () -> Unit,
    onDismissImport: () -> Unit,
    onRestore: (RestoreMode) -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.reports_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.reports_local_notice))
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(stringResource(R.string.report_format), fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportFormat.entries.forEach { format ->
                            FilterChip(
                                selected = state.format == format,
                                onClick = { onFormatChange(format) },
                                label = { Text(format.name) }
                            )
                        }
                    }
                    if (state.format == ReportFormat.JSON) {
                        Text(stringResource(R.string.json_backup_all_data))
                    } else {
                        Text(stringResource(R.string.profile_label), fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.profiles.forEach { profile ->
                                FilterChip(
                                    selected = state.selectedProfileId == profile.id,
                                    onClick = { onProfileChange(profile.id) },
                                    label = { Text(profile.name) }
                                )
                            }
                        }
                        Text(stringResource(R.string.report_period), fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReportPeriod.entries.forEach { period ->
                                FilterChip(
                                    selected = state.period == period,
                                    onClick = { onPeriodChange(period) },
                                    label = { Text(reportPeriodText(period)) }
                                )
                            }
                        }
                        if (state.period == ReportPeriod.CUSTOM) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.customStartText,
                                    onValueChange = onCustomStartChange,
                                    label = { Text(stringResource(R.string.start_date)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = state.customEndText,
                                    onValueChange = onCustomEndChange,
                                    label = { Text(stringResource(R.string.end_date)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeightUnit.entries.forEach { unit ->
                                FilterChip(
                                    selected = state.unit == unit,
                                    onClick = { onUnitChange(unit) },
                                    label = { Text(unit.symbol) }
                                )
                            }
                        }
                        if (state.format == ReportFormat.PDF) {
                            ReportSwitch(
                                stringResource(R.string.include_chart),
                                state.includeChart,
                                onIncludeChartChange
                            )
                            ReportSwitch(
                                stringResource(R.string.include_table),
                                state.includeTable,
                                onIncludeTableChange
                            )
                        }
                        ReportSwitch(
                            stringResource(R.string.include_notes),
                            state.includeNotes,
                            onIncludeNotesChange
                        )
                        ReportSwitch(
                            stringResource(R.string.include_metrics),
                            state.includeAdditionalMetrics,
                            onIncludeMetricsChange
                        )
                    }
                    Button(
                        onClick = onGenerate,
                        enabled = !state.isWorking &&
                            (state.format == ReportFormat.JSON || state.profiles.isNotEmpty()),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(
                                if (state.isWorking) R.string.working
                                else R.string.generate_report
                            )
                        )
                    }
                }
            }
        }
        state.generatedFile?.let { generated ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Button(onClick = onShare) {
                                Text(stringResource(R.string.share))
                            }
                            if (state.generatedSummaryText != null) {
                                OutlinedButton(onClick = onShareSummary) {
                                    Text(stringResource(R.string.share_summary))
                                }
                            }
                            OutlinedButton(onClick = onSaveFile) {
                                Text(stringResource(R.string.save_copy))
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.backup_restore_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(stringResource(R.string.backup_restore_body))
                    OutlinedButton(onClick = onImport, enabled = !state.isWorking) {
                        Text(stringResource(R.string.import_json))
                    }
                    TextButton(onClick = onClearTemporaryFiles) {
                        Text(stringResource(R.string.clear_temporary_files))
                    }
                }
            }
        }
        state.message?.let {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            reportErrorText(it),
                            color = MaterialTheme.colorScheme.error
                        )
                        state.validationErrors.take(5).forEach { detail ->
                            Text(
                                detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    state.importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = onDismissImport,
            title = { Text(stringResource(R.string.import_preview_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.import_preview_body,
                        preview.profileCount,
                        preview.measurementCount,
                        preview.goalCount,
                        preview.deviceCount,
                        preview.duplicateIdCount
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onRestore(RestoreMode.MERGE) }) {
                    Text(stringResource(R.string.merge_data))
                }
            },
            dismissButton = {
                Column {
                    TextButton(onClick = { onRestore(RestoreMode.REPLACE) }) {
                        Text(stringResource(R.string.replace_data))
                    }
                    TextButton(onClick = onDismissImport) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }
}

@Composable
private fun ReportSwitch(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
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
