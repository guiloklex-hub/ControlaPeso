package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem

@Composable
fun HeroMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    trend: String? = null,
    footer: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ControlaPesoDesignSystem.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                value,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            supportingText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            trend?.let {
                TrendBadge(
                    text = it,
                    color = ControlaPesoDesignSystem.colors.trendNeutral
                )
            }
            footer()
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xxs
            )
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.titleLarge)
            supportingText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ControlaPesoDesignSystem.colors.informational
) {
    Surface(
        modifier = modifier.semantics { contentDescription = text },
        shape = MaterialTheme.shapes.extraLarge,
        color = color.copy(alpha = 0.14f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text,
            modifier = Modifier.padding(
                horizontal = ControlaPesoDesignSystem.spacing.sm,
                vertical = ControlaPesoDesignSystem.spacing.xs
            ),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun TrendBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ControlaPesoDesignSystem.colors.trendNeutral
) {
    StatusPill(text = text, modifier = modifier, color = color)
}

@Composable
fun PrimaryActionCard(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActionCard(
        title = title,
        body = body,
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun SecondaryActionCard(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActionCard(
        title = title,
        body = body,
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ActionCard(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun GoalProgressCard(
    title: String,
    progress: Float?,
    summary: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    actions: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.sm
            )
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            progress?.let {
                LinearProgressIndicator(
                    progress = { it.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = summary }
                )
            }
            Text(summary, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(
                    ControlaPesoDesignSystem.spacing.xs
                ),
                verticalArrangement = Arrangement.spacedBy(
                    ControlaPesoDesignSystem.spacing.xxs
                )
            ) {
                actions()
            }
        }
    }
}
