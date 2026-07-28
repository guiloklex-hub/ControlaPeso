package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    StateSurface(
        title = title,
        body = body,
        modifier = modifier,
        actionLabel = actionLabel,
        onAction = onAction
    )
}

@Composable
fun ErrorState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    StateSurface(
        title = title,
        body = body,
        modifier = modifier,
        actionLabel = actionLabel,
        onAction = onAction,
        containerColor = MaterialTheme.colorScheme.errorContainer
    )
}

@Composable
fun LoadingState(
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ControlaPesoDesignSystem.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.md
        )
    ) {
        CircularProgressIndicator()
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StateSurface(
    title: String,
    body: String,
    modifier: Modifier,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    containerColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.surfaceContainerLow
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.sm
            )
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyLarge)
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

