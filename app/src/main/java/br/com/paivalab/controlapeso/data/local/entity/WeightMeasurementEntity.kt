package br.com.paivalab.controlapeso.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "weight_measurements",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScaleDeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["profileId", "measuredAt"]),
        Index(value = ["deviceId"]),
        Index(value = ["source"]),
        Index(value = ["rawPayloadHex"])
    ]
)
data class WeightMeasurementEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val weightKg: Double,
    val measuredAt: Instant,
    val zoneOffsetSeconds: Int?,
    val source: String,
    val isStable: Boolean,
    val deviceId: String?,
    val deviceName: String?,
    val deviceAddress: String?,
    val note: String?,
    val rawPayloadHex: String?,
    val impedanceOne: Double?,
    val impedanceTwo: Double?,
    val bodyFatPercent: Double?,
    val muscleMassKg: Double?,
    val bodyWaterPercent: Double?,
    val boneMassKg: Double?,
    val visceralFatLevel: Double?,
    val metabolicAge: Int?,
    val createdAt: Instant,
    val updatedAt: Instant
)
