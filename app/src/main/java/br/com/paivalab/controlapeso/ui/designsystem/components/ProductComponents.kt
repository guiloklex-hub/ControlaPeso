package br.com.paivalab.controlapeso.ui.designsystem.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CompactAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val primary: Boolean = false,
    val enabled: Boolean = true
)

@Composable
fun ProfileAvatar(
    name: String,
    modifier: Modifier = Modifier,
    contentDescription: String = name,
    photoPath: String? = null,
    photoUri: String? = null,
    avatarKey: String? = null
) {
    val context = LocalContext.current
    val photo by produceState<ImageBitmap?>(initialValue = null, photoPath, photoUri) {
        value = withContext(Dispatchers.IO) {
            when {
                photoUri != null -> context.decodeProfilePhotoUri(photoUri)
                else -> photoPath?.decodeProfilePhoto()
            }
        }
    }
    Surface(
        modifier = modifier
            .size(ControlaPesoDesignSystem.sizes.minimumTouchTarget)
            .semantics { this.contentDescription = contentDescription },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        if (photo != null) {
            Image(
                bitmap = requireNotNull(photo),
                contentDescription = null,
                modifier = Modifier
                    .size(ControlaPesoDesignSystem.sizes.minimumTouchTarget)
                    .clip(MaterialTheme.shapes.extraLarge)
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = avatarIcon(avatarKey),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileContextHeader(
    name: String?,
    unitSymbol: String,
    noProfileLabel: String,
    unitLabel: String,
    switchLabel: String,
    modifier: Modifier = Modifier,
    onSwitchProfile: (() -> Unit)? = null,
    photoPath: String? = null,
    avatarKey: String? = null
) {
    val displayName = name ?: noProfileLabel
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val useStackedLayout = LocalDensity.current.fontScale >= 1.3f ||
                maxWidth < 360.dp
            if (useStackedLayout) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ControlaPesoDesignSystem.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(
                        ControlaPesoDesignSystem.spacing.sm
                    )
                ) {
                    ProfileHeaderIdentity(
                        modifier = Modifier.fillMaxWidth(),
                        name = name,
                        displayName = displayName,
                        unitLabel = unitLabel,
                        unitSymbol = unitSymbol,
                        photoPath = photoPath,
                        avatarKey = avatarKey
                    )
                    onSwitchProfile?.let { onClick ->
                        ProfileSwitchButton(
                            label = switchLabel,
                            onClick = onClick,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ControlaPesoDesignSystem.spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(
                        ControlaPesoDesignSystem.spacing.md
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileHeaderIdentity(
                        modifier = Modifier.weight(1f),
                        name = name,
                        displayName = displayName,
                        unitLabel = unitLabel,
                        unitSymbol = unitSymbol,
                        photoPath = photoPath,
                        avatarKey = avatarKey
                    )
                    onSwitchProfile?.let { onClick ->
                        ProfileSwitchButton(
                            label = switchLabel,
                            onClick = onClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderIdentity(
    modifier: Modifier,
    name: String?,
    displayName: String,
    unitLabel: String,
    unitSymbol: String,
    photoPath: String?,
    avatarKey: String?
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.md
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(
            name = name ?: "?",
            contentDescription = displayName,
            photoPath = photoPath,
            avatarKey = avatarKey
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xxs
            )
        ) {
            Text(displayName, style = MaterialTheme.typography.titleMedium)
            Text(
                unitLabel.format(unitSymbol),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSwitchButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(
            min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
        )
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Filled.Edit,
            contentDescription = null
        )
        Spacer(Modifier.size(ControlaPesoDesignSystem.spacing.xs))
        Text(label)
    }
}

private fun avatarIcon(avatarKey: String?): ImageVector = when (avatarKey) {
    "aqua", "ocean" -> Icons.Filled.Info
    "sand" -> Icons.Filled.Favorite
    "leaf" -> Icons.Filled.Star
    else -> Icons.Filled.Person
}

private fun String.decodeProfilePhoto(): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(this, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 128 || bounds.outHeight / sampleSize > 128) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeFile(
        this,
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
    )?.asImageBitmap()
}

private fun Context.decodeProfilePhotoUri(uriString: String): ImageBitmap? {
    val uri = Uri.parse(uriString)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 128 || bounds.outHeight / sampleSize > 128) {
        sampleSize *= 2
    }
    return contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(
            input,
            null,
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
        )
    }?.asImageBitmap()
}

@Composable
fun CompactActionGroup(
    actions: List<CompactAction>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.xs
        ),
        verticalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.xxs
        )
    ) {
        actions.forEach { action ->
            CompactActionButton(action)
        }
    }
}

@Composable
fun CompactActionButton(
    action: CompactAction,
    modifier: Modifier = Modifier
) {
    val content: @Composable () -> Unit = {
        Icon(imageVector = action.icon, contentDescription = null)
        Spacer(Modifier.size(ControlaPesoDesignSystem.spacing.xs))
        Text(action.label)
    }
    if (action.primary) {
        Button(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = modifier.heightIn(
                min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
            )
        ) {
            content()
        }
    } else {
        OutlinedButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = modifier.heightIn(
                min = ControlaPesoDesignSystem.sizes.minimumTouchTarget
            )
        ) {
            content()
        }
    }
}

@Composable
fun MeasurementListItem(
    value: String,
    dateTime: String,
    modifier: Modifier = Modifier,
    source: String? = null,
    sourceIsDemo: Boolean = false,
    note: String? = null,
    contextLabel: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val clickable = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(clickable)
            .heightIn(min = ControlaPesoDesignSystem.sizes.minimumTouchTarget)
            .then(
                if (onClick != null) {
                    Modifier.semantics(mergeDescendants = true) { role = Role.Button }
                } else {
                    Modifier
                }
            )
            .padding(vertical = ControlaPesoDesignSystem.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(
            ControlaPesoDesignSystem.spacing.md
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xxs
            )
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium)
            contextLabel?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        source?.let {
            DataOriginBadge(
                source = it,
                isDemo = sourceIsDemo
            )
        }
    }
}
