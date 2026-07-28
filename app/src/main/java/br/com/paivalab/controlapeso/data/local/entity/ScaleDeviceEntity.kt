package br.com.paivalab.controlapeso.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "scale_devices",
    indices = [
        Index(value = ["bluetoothAddress"], unique = true),
        Index(value = ["isPreferred"])
    ]
)
data class ScaleDeviceEntity(
    @PrimaryKey val id: String,
    val displayName: String?,
    val bluetoothAddress: String,
    val protocolName: String?,
    val lastConnectedAt: Instant?,
    val lastSeenAt: Instant?,
    val isPreferred: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
