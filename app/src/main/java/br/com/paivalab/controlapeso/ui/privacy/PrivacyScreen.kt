package br.com.paivalab.controlapeso.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.ErrorState
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList

@Composable
fun PrivacyScreen(
    state: PrivacyUiState,
    onExport: () -> Unit,
    onClearTemporaryFiles: () -> Unit,
    onConsumeTemporaryNotice: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onContinueDelete: () -> Unit,
    onConfirmationTextChange: (String) -> Unit,
    onConfirmDelete: () -> Unit,
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
                    body = stringResource(R.string.privacy_health_body)
                )
            }
            item {
                OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.export_before_delete))
                }
            }
            item {
                OutlinedButton(
                    onClick = onClearTemporaryFiles,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.clear_temporary_files))
                }
            }
            item {
                Button(
                    onClick = onRequestDelete,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isDeleting
                ) {
                    Text(stringResource(R.string.delete_all_data))
                }
            }
            if (state.error) {
                item {
                    ErrorState(
                        title = stringResource(R.string.privacy_delete_error_title),
                        body = stringResource(R.string.delete_all_error)
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body)
        }
    }
}
