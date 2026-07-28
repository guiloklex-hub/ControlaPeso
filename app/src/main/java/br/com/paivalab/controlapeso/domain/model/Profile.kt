package br.com.paivalab.controlapeso.domain.model

import java.time.Instant
import java.time.LocalDate

data class Profile(
    val id: String,
    val name: String,
    val avatarKey: String?,
    val heightCm: Double?,
    val birthDate: LocalDate?,
    val preferredWeightUnit: WeightUnit,
    val healthConnectEnabled: Boolean,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
