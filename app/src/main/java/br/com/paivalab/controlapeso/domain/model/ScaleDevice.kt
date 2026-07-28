package br.com.paivalab.controlapeso.domain.model

import java.time.Instant

data class ScaleDevice(
    val id: String,
    val displayName: String?,
    val bluetoothAddress: String,
    val protocolName: String?,
    val lastConnectedAt: Instant?,
    val lastSeenAt: Instant?,
    val isPreferred: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
