package br.com.paivalab.controlapeso.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import java.text.DateFormat
import java.time.Instant
import java.util.Date

@Composable
fun DevicesScreen(
    state: DevicesUiState,
    onPreferred: (ScaleDevice) -> Unit,
    onForget: (ScaleDevice) -> Unit,
    onDismissForget: () -> Unit,
    onConfirmForget: () -> Unit,
    onMeasure: () -> Unit,
    onDiagnostic: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.devices_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(stringResource(R.string.devices_no_background_connection))
        }
        if (state.isLoading) {
            item { CircularProgressIndicator() }
        } else if (state.hasError) {
            item {
                Text(
                    stringResource(R.string.data_load_error),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (state.devices.isEmpty()) {
            item { Text(stringResource(R.string.devices_empty)) }
        }
        items(state.devices, key = { it.device.id }) { item ->
            DeviceCard(
                item = item,
                onPreferred = { onPreferred(item.device) },
                onForget = { onForget(item.device) },
                onMeasure = onMeasure
            )
        }
        item {
            OutlinedButton(onClick = onDiagnostic, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.open_diagnostic))
            }
        }
    }

    state.pendingForget?.let { device ->
        AlertDialog(
            onDismissRequest = onDismissForget,
            title = { Text(stringResource(R.string.forget_scale_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.forget_scale_body,
                        device.displayName ?: stringResource(R.string.unnamed_device)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmForget) {
                    Text(stringResource(R.string.forget))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissForget) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DeviceCard(
    item: KnownScaleItem,
    onPreferred: () -> Unit,
    onForget: () -> Unit,
    onMeasure: () -> Unit
) {
    val device = item.device
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                device.displayName ?: stringResource(R.string.unnamed_device),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                if (device.isPreferred) {
                    stringResource(R.string.preferred_scale)
                } else {
                    stringResource(R.string.known_scale)
                },
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(
                    R.string.device_protocol,
                    device.protocolName ?: stringResource(R.string.not_available)
                )
            )
            Text(stringResource(R.string.device_advertising_state))
            Text(
                stringResource(
                    R.string.device_last_used,
                    item.lastUsedAt.localizedOrUnavailable()
                )
            )
            Text(
                stringResource(
                    R.string.device_last_seen,
                    device.lastSeenAt.localizedOrUnavailable()
                )
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMeasure) {
                    Text(stringResource(R.string.measure_with_scale))
                }
                if (!device.isPreferred) {
                    OutlinedButton(onClick = onPreferred) {
                        Text(stringResource(R.string.make_preferred))
                    }
                }
                TextButton(onClick = onForget) {
                    Text(stringResource(R.string.forget))
                }
            }
        }
    }
}

@Composable
private fun Instant?.localizedOrUnavailable(): String =
    this?.let {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date.from(it))
    } ?: stringResource(R.string.not_available)
