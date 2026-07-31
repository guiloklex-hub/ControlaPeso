package br.com.paivalab.controlapeso.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.data.backup.RestoreMode
import br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList
import br.com.paivalab.controlapeso.ui.designsystem.components.SettingsSection

@Composable
fun DataBackupScreen(
    state: ReportsUiState,
    localBackupState: LocalBackupUiState,
    onLocalBackupFrequencyChange: (LocalBackupFrequency) -> Unit,
    onCreateLocalBackup: () -> Unit,
    onShareLatestBackup: () -> Unit,
    onDismissLocalFeedback: () -> Unit,
    onImport: () -> Unit,
    onClearTemporaryFiles: () -> Unit,
    onDismissImport: () -> Unit,
    onRestore: (RestoreMode) -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    ResponsiveScreenList(modifier = modifier, maxContentWidth = 900.dp) {
        item {
            Text(
                stringResource(R.string.data_backup_title),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(stringResource(R.string.data_backup_body))
        }
        item {
            LocalBackupSection(
                state = localBackupState,
                onFrequencyChange = onLocalBackupFrequencyChange,
                onCreate = onCreateLocalBackup,
                onShare = onShareLatestBackup,
                onDismissFeedback = onDismissLocalFeedback
            )
        }
        item {
            SettingsSection(
                title = stringResource(R.string.backup_restore_title),
                supportingText = stringResource(R.string.backup_restore_body)
            ) {
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.import_json),
                        icon = Icons.Filled.Refresh,
                        onClick = onImport,
                        primary = true,
                        enabled = !state.isWorking
                    )
                )
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.clear_temporary_files),
                        icon = Icons.Filled.Delete,
                        onClick = onClearTemporaryFiles
                    )
                )
            }
        }
        state.message?.let { message ->
            item {
                TextButton(onClick = onDismissMessage) {
                    Text(dataBackupMessageText(message))
                }
            }
        }
        state.error?.let { error ->
            item {
                ErrorState(
                    title = stringResource(R.string.data_backup_error_title),
                    body = state.validationErrors.take(5).joinToString("\n")
                        .ifBlank { dataBackupErrorText(error) },
                    actionLabel = stringResource(R.string.dismiss_error),
                    onAction = onDismissMessage,
                    actionIcon = Icons.Filled.Close
                )
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        ControlaPesoDesignSystem.spacing.xxs
                    )
                ) {
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
private fun LocalBackupSection(
    state: LocalBackupUiState,
    onFrequencyChange: (LocalBackupFrequency) -> Unit,
    onCreate: () -> Unit,
    onShare: () -> Unit,
    onDismissFeedback: () -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.local_backup_title),
        supportingText = stringResource(R.string.local_backup_body)
    ) {
        Text(stringResource(R.string.local_backup_frequency_label))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            ),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
        ) {
            LocalBackupFrequency.entries.forEach { frequency ->
                FilterChip(
                    selected = state.frequency == frequency,
                    onClick = { onFrequencyChange(frequency) },
                    label = { Text(localBackupFrequencyLabel(frequency)) }
                )
            }
        }
        Text(
            state.latestAt?.let {
                stringResource(R.string.local_backup_last_at, formatBackupDateTime(it))
            } ?: stringResource(R.string.local_backup_never),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.sm
            ),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
        ) {
            CompactActionButton(
                CompactAction(
                    label = stringResource(R.string.local_backup_now),
                    icon = Icons.Filled.Refresh,
                    onClick = onCreate,
                    primary = true,
                    enabled = !state.isWorking
                )
            )
            CompactActionButton(
                CompactAction(
                    label = stringResource(R.string.local_backup_share_latest),
                    icon = Icons.Filled.Share,
                    onClick = onShare,
                    enabled = state.latestAvailable && !state.isWorking
                )
            )
        }
        Text(
            stringResource(R.string.local_backup_share_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (state.isWorking) Text(stringResource(R.string.working))
        state.message?.let {
            TextButton(onClick = onDismissFeedback) {
                Text(localBackupMessageText(it))
            }
        }
        state.error?.let { error ->
            ErrorState(
                title = stringResource(R.string.local_backup_error_title),
                body = localBackupErrorText(error),
                actionLabel = stringResource(R.string.dismiss_error),
                onAction = onDismissFeedback,
                actionIcon = Icons.Filled.Close
            )
        }
    }
}

private fun localBackupFrequencyLabel(value: LocalBackupFrequency): String = when (value) {
    LocalBackupFrequency.OFF -> "Desativado"
    LocalBackupFrequency.DAILY -> "Diário"
    LocalBackupFrequency.WEEKLY -> "Semanal"
    LocalBackupFrequency.MONTHLY -> "Mensal (30 dias)"
}

@Composable
private fun formatBackupDateTime(value: java.time.Instant): String =
    br.com.paivalab.controlapeso.core.time.BrazilianDateTimeFormatter.dateTime(value)

@Composable
private fun localBackupMessageText(message: LocalBackupMessage): String = stringResource(
    when (message) {
        LocalBackupMessage.CREATED -> R.string.local_backup_created
        LocalBackupMessage.SHARED -> R.string.local_backup_shared
    }
)

@Composable
private fun localBackupErrorText(error: LocalBackupError): String = stringResource(
    when (error) {
        LocalBackupError.CREATE_FAILED -> R.string.local_backup_create_error
        LocalBackupError.SHARE_UNAVAILABLE -> R.string.local_backup_share_error
    }
)

@Composable
private fun dataBackupMessageText(message: ReportsMessage): String = stringResource(
    when (message) {
        ReportsMessage.RESTORED_MERGE -> R.string.message_restore_merge
        ReportsMessage.RESTORED_REPLACE -> R.string.message_restore_replace
        ReportsMessage.CACHE_CLEARED -> R.string.message_cache_cleared
        ReportsMessage.GENERATED -> R.string.message_report_generated
        ReportsMessage.SAVED -> R.string.message_file_saved
    }
)

@Composable
private fun dataBackupErrorText(error: ReportsError): String = stringResource(
    when (error) {
        ReportsError.FILE_READ_FAILED -> R.string.error_file_read
        ReportsError.RESTORE_FAILED -> R.string.error_restore
        ReportsError.PROFILE_REQUIRED -> R.string.error_profile_required
        ReportsError.INVALID_PERIOD -> R.string.invalid_custom_period
        ReportsError.NO_MEASUREMENTS -> R.string.error_report_no_measurements
        ReportsError.GENERATION_FAILED -> R.string.error_report_generation
        ReportsError.FILE_WRITE_FAILED -> R.string.error_file_write
    }
)
