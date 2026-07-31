package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.xs
        )
    ) {
        SectionHeader(title = title, supportingText = supportingText)
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.md),
                verticalArrangement = Arrangement.spacedBy(
                    ControlaPesoDesignSystem.spacing.sm
                )
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {}
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .heightIn(min = ControlaPesoDesignSystem.sizes.minimumTouchTarget)
            .then(
                if (onClick != null) {
                    Modifier.semantics { role = Role.Button }
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = ControlaPesoDesignSystem.spacing.md,
                vertical = ControlaPesoDesignSystem.spacing.sm
            ),
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
            Text(title, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailingContent()
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = ControlaPesoDesignSystem.spacing.md),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
