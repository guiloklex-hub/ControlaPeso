package br.com.paivalab.controlapeso.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectAvailability
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList

@Composable
fun PrivacyScreen(
    state: PrivacyUiState,
    onExport: () -> Unit,
    onDataBackup: () -> Unit = {},
    onClearTemporaryFiles: () -> Unit,
    onConsumeTemporaryNotice: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onContinueDelete: () -> Unit,
    onConfirmationTextChange: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbar = remember { SnackbarHostState() }
    val clearedMessage = stringResource(R.string.temporary_files_cleared)
    LaunchedEffect(state.temporaryFilesCleared) {
        if (state.temporaryFilesCleared) {
            snackbar.showSnackbar(clearedMessage)
            onConsumeTemporaryNotice()
        }
    }
    Box(modifier.fillMaxSize()) {
        ResponsiveScreenList {
            item {
                Text(
                    stringResource(R.string.privacy_title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                PrivacyCard(
                    title = stringResource(R.string.privacy_local_title),
                    body = stringResource(R.string.privacy_local_body)
                )
            }
            item {
                PrivacyCard(
                    title = stringResource(R.string.privacy_permissions_title),
                    body = stringResource(R.string.privacy_permissions_body)
                )
            }
            item {
                PrivacyCard(
                    title = stringResource(R.string.privacy_health_title),
                    body = stringResource(
                        R.string.privacy_health_body_with_status,
                        healthStatusText(state.healthAvailability, state.healthPermissionGranted)
                    )
                )
            }
            item {
                PrivacyCard(
                    title = stringResource(R.string.privacy_sharing_title),
                    body = stringResource(R.string.privacy_sharing_body)
                )
            }
            item {
                PrivacyCard(
                    title = stringResource(R.string.privacy_rights_title),
                    body = stringResource(R.string.privacy_rights_body)
                )
            }
            item {
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.export_before_delete),
                        icon = Icons.Filled.Share,
                        onClick = onExport
                    )
                )
            }
            item {
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.open_data_backup),
                        icon = Icons.Filled.Build,
                        onClick = onDataBackup
                    )
                )
            }
            item {
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.clear_temporary_files),
                        icon = Icons.Filled.Info,
                        onClick = onClearTemporaryFiles
                    )
                )
            }
            item {
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.delete_all_data),
                        icon = Icons.Filled.Delete,
                        onClick = onRequestDelete,
                        primary = true,
                        enabled = !state.isDeleting
                    )
                )
            }
            if (state.error) {
                item {
                    ErrorState(
                        title = stringResource(R.string.privacy_delete_error_title),
                        body = stringResource(R.string.delete_all_error),
                        actionLabel = stringResource(R.string.dismiss_error),
                        onAction = onDismissError,
                        actionIcon = Icons.Filled.Close
                    )
                }
            }
        }
        SnackbarHost(
            snackbar,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.showFirstConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.delete_all_title)) },
            text = { Text(stringResource(R.string.delete_all_first_body)) },
            confirmButton = {
                TextButton(onClick = onContinueDelete) {
                    Text(stringResource(R.string.continue_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.showFinalConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.delete_all_final_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.delete_all_final_body))
                    OutlinedTextField(
                        value = state.confirmationText,
                        onValueChange = onConfirmationTextChange,
                        label = { Text(stringResource(R.string.delete_confirmation_label)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    enabled = state.confirmationText.trim().uppercase() ==
                        PrivacyViewModel.CONFIRMATION_WORD && !state.isDeleting
                ) {
                    Text(
                        stringResource(
                            if (state.isDeleting) R.string.deleting else R.string.delete
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete, enabled = !state.isDeleting) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun healthStatusText(
    availability: HealthConnectAvailability,
    permissionGranted: Boolean
): String = stringResource(
    when (availability) {
        HealthConnectAvailability.AVAILABLE -> if (permissionGranted) {
            R.string.health_permission_status_authorized
        } else {
            R.string.health_permission_status_not_authorized
        }
        HealthConnectAvailability.NOT_INSTALLED -> R.string.health_not_installed
        HealthConnectAvailability.UPDATE_REQUIRED -> R.string.health_update_required
        HealthConnectAvailability.ANDROID_VERSION_UNSUPPORTED ->
            R.string.health_android_unsupported
    }
)

@Composable
private fun PrivacyCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
        ) {
            Text(
                title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium
            )
            Text(body)
        }
    }
}
