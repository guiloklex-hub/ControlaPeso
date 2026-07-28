package br.com.paivalab.controlapeso.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "profiles",
    indices = [Index(value = ["isActive"])]
)
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarKey: String?,
    val heightCm: Double?,
    val birthDate: LocalDate?,
    val preferredWeightUnit: String,
    val healthConnectEnabled: Boolean,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
