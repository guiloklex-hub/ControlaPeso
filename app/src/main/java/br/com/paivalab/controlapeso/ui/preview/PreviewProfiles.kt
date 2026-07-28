package br.com.paivalab.controlapeso.ui.preview

import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.Instant
import java.time.LocalDate

object PreviewProfiles {
    val primary = Profile(
        id = "preview-profile-primary",
        name = "Ana",
        avatarKey = null,
        heightCm = 168.0,
        birthDate = LocalDate.of(1992, 4, 18),
        preferredWeightUnit = WeightUnit.KILOGRAM,
        healthConnectEnabled = false,
        isActive = true,
        createdAt = Instant.parse("2026-01-01T12:00:00Z"),
        updatedAt = Instant.parse("2026-07-28T12:00:00Z")
    )

    val longName = primary.copy(
        id = "preview-profile-long",
        name = "Alexandre de Albuquerque",
        isActive = false
    )

    val all = listOf(primary, longName)
}

