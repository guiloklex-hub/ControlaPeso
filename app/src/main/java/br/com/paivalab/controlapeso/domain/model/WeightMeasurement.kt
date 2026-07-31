package br.com.paivalab.controlapeso.domain.model

import java.time.Instant

data class WeightMeasurement(
    val id: String,
    val profileId: String?,
    val weightKg: Double,
    val measuredAt: Instant,
    val zoneOffsetSeconds: Int?,
    val source: MeasurementSource,
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
