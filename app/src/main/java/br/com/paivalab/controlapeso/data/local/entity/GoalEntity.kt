package br.com.paivalab.controlapeso.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId", "status"]),
        Index(value = ["profileId", "isActive"])
    ]
)
data class GoalEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val startWeightKg: Double,
    val targetWeightKg: Double,
    val startDate: LocalDate,
    val targetDate: LocalDate?,
    val status: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
