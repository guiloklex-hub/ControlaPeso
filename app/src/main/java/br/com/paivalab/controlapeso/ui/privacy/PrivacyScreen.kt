package br.com.paivalab.controlapeso.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R

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
    Column(modifier.fillMaxSize()) {
        SnackbarHost(snackbar)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.privacy_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
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
                    Text(
                        stringResource(R.string.delete_all_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body)
        }
    }
}
