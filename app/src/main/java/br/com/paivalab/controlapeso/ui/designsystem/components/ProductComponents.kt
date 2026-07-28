package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem

@Composable
fun ProfileAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(ControlaPesoDesignSystem.sizes.minimumTouchTarget),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                name.trim().take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MeasurementListItem(
    value: String,
    dateTime: String,
    modifier: Modifier = Modifier,
    source: String? = null,
    note: String? = null,
    onClick: (() -> Unit)? = null
) {
    val clickable = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(clickable)
            .padding(vertical = ControlaPesoDesignSystem.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.md
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xxs
            )
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(
                dateTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            note?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        source?.let { StatusPill(it) }
    }
}

