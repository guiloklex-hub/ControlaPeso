package br.com.paivalab.controlapeso.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.paivalab.controlapeso.BuildConfig
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.BrazilianDateTimeFormatter
import br.com.paivalab.controlapeso.ui.update.ReleaseCheckStatus
import br.com.paivalab.controlapeso.ui.update.ReleaseUpdateUiState
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactAction
import br.com.paivalab.controlapeso.ui.designsystem.components.CompactActionButton
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList

@Composable
fun AboutScreen(
    updateState: ReleaseUpdateUiState = ReleaseUpdateUiState(),
    onCheckForUpdates: () -> Unit = {},
    onViewReleaseNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ResponsiveScreenList(modifier = modifier) {
        item {
            Text(
                stringResource(R.string.about_title),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        item {
            AboutUpdatesCard(
                state = updateState,
                onCheckForUpdates = onCheckForUpdates,
                onViewReleaseNotes = onViewReleaseNotes
            )
        }
        item {
            AboutCard(
                stringResource(R.string.app_name),
                stringResource(
                    R.string.about_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                    BuildConfig.BUILD_TYPE
                )
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_privacy_heading),
                stringResource(R.string.about_privacy_body)
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_protocol_heading),
                stringResource(R.string.about_protocol_body)
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_medical_heading),
                stringResource(R.string.about_medical_body)
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_licenses_heading),
                stringResource(R.string.about_licenses_body)
            )
        }
    }
}

@Composable
private fun AboutUpdatesCard(
    state: ReleaseUpdateUiState,
    onCheckForUpdates: () -> Unit,
    onViewReleaseNotes: () -> Unit
) {
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
                stringResource(R.string.about_updates_heading),
                style = MaterialTheme.typography.titleMedium
            )
            Text(stringResource(R.string.about_updates_body))
            Text(
                stringResource(
                    when (state.checkStatus) {
                        ReleaseCheckStatus.NOT_CHECKED -> R.string.update_status_not_checked
                        ReleaseCheckStatus.CHECKING -> R.string.update_status_checking
                        ReleaseCheckStatus.UP_TO_DATE -> R.string.update_status_up_to_date
                        ReleaseCheckStatus.UPDATE_AVAILABLE -> R.string.update_status_available
                        ReleaseCheckStatus.FAILED -> R.string.update_status_failed
                    }
                )
            )
            state.lastCheckAt?.let {
                Text(
                    stringResource(
                        R.string.update_last_checked,
                        BrazilianDateTimeFormatter.dateTime(it)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.release?.let { release ->
                Text(
                    stringResource(R.string.update_about_available, release.version.toString()),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                CompactActionButton(
                    CompactAction(
                        label = stringResource(R.string.about_view_release_notes),
                        icon = Icons.Filled.Info,
                        onClick = onViewReleaseNotes
                    )
                )
            }
            CompactActionButton(
                CompactAction(
                    label = stringResource(R.string.update_check_now),
                    icon = Icons.Filled.Refresh,
                    onClick = onCheckForUpdates,
                    enabled = !state.isChecking && !state.isDownloading
                )
            )
        }
    }
}

@Composable
private fun AboutCard(title: String, body: String) {
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
