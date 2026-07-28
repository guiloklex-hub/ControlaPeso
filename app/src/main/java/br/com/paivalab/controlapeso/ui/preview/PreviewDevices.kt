package br.com.paivalab.controlapeso.ui.preview

import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import br.com.paivalab.controlapeso.ui.devices.KnownScaleItem
import java.time.Instant

object PreviewDevices {
    private val preferred = ScaleDevice(
        id = "preview-scale",
        displayName = "Balança do quarto",
        bluetoothAddress = "00:00:00:00:00:00",
        protocolName = "OKOK advertisement",
        lastConnectedAt = null,
        lastSeenAt = Instant.parse("2026-07-28T10:30:00Z"),
        isPreferred = true,
        createdAt = Instant.parse("2026-06-01T12:00:00Z"),
        updatedAt = Instant.parse("2026-07-28T10:30:00Z")
    )

    private val secondary = preferred.copy(
        id = "preview-scale-secondary",
        displayName = "Balança de teste com nome longo",
        bluetoothAddress = "00:00:00:00:00:01",
        protocolName = null,
        isPreferred = false,
        lastSeenAt = Instant.parse("2026-07-25T08:00:00Z")
    )

    val known = listOf(
        KnownScaleItem(
            device = preferred,
            lastUsedAt = Instant.parse("2026-07-28T10:30:00Z")
        ),
        KnownScaleItem(
            device = secondary,
            lastUsedAt = Instant.parse("2026-07-25T08:00:00Z")
        )
    )
}
