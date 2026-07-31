package br.com.paivalab.controlapeso.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.BrazilianDateTimeFormatter
import br.com.paivalab.controlapeso.data.update.UpdateDownloadFailure
import java.time.Instant

@Composable
fun ReleaseUpdateDialog(
    state: ReleaseUpdateUiState,
    onDismiss: () -> Unit,
    onDownloadAndInstall: () -> Unit,
    onDismissInstallFeedback: () -> Unit
) {
    state.release?.takeIf { state.isDialogVisible }?.let { release ->
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.update_available_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(
                            R.string.update_available_body,
                            release.version.toString(),
                            releaseDate(release.publishedAt.toEpochMilli())
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.update_changelog),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                    )
                    Text(
                        release.body.ifBlank { stringResource(R.string.update_no_changelog) },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (state.isDownloading) {
                        Text(
                            stringResource(R.string.update_downloading),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    state.downloadFailure?.let { failure ->
                        Text(
                            updateDownloadFailureText(failure),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (state.installFeedback == UpdateInstallFeedback.UNKNOWN_SOURCES) {
                        Text(
                            stringResource(R.string.update_unknown_sources),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDownloadAndInstall, enabled = !state.isDownloading) {
                    Text(stringResource(R.string.update_download_install))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !state.isDownloading) {
                    Text(stringResource(R.string.update_later))
                }
            }
        )
    }
    state.installFeedback?.let { feedback ->
        if (state.release != null) return@let
        AlertDialog(
            onDismissRequest = onDismissInstallFeedback,
            title = { Text(stringResource(R.string.update_available_title)) },
            text = {
                Text(
                    when (feedback) {
                        UpdateInstallFeedback.INSTALL_STARTED ->
                            stringResource(R.string.update_install_started)
                        UpdateInstallFeedback.INSTALL_FAILED ->
                            stringResource(R.string.update_error_install)
                        UpdateInstallFeedback.UNKNOWN_SOURCES ->
                            stringResource(R.string.update_unknown_sources)
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissInstallFeedback) {
                    Text(stringResource(R.string.dismiss_error))
                }
            }
        )
    }
}

@Composable
private fun updateDownloadFailureText(failure: UpdateDownloadFailure): String = stringResource(
    when (failure) {
        UpdateDownloadFailure.CHECKSUM_NOT_FOUND,
        UpdateDownloadFailure.CHECKSUM_MISMATCH -> R.string.update_error_checksum
        UpdateDownloadFailure.APK_PACKAGE_INVALID,
        UpdateDownloadFailure.APK_SIGNATURE_INVALID,
        UpdateDownloadFailure.APK_VERSION_INVALID,
        UpdateDownloadFailure.APK_INVALID -> R.string.update_error_apk
        UpdateDownloadFailure.NETWORK -> R.string.update_error_network
        UpdateDownloadFailure.STORAGE -> R.string.update_error_storage
    }
)

private fun releaseDate(epochMillis: Long): String = BrazilianDateTimeFormatter.date(
    Instant.ofEpochMilli(epochMillis)
)
