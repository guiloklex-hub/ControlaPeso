package br.com.paivalab.controlapeso.data.local.mapper

import br.com.paivalab.controlapeso.data.local.entity.GoalEntity
import br.com.paivalab.controlapeso.data.local.entity.ProfileEntity
import br.com.paivalab.controlapeso.data.local.entity.ScaleDeviceEntity
import br.com.paivalab.controlapeso.data.local.entity.WeightMeasurementEntity
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit

fun ProfileEntity.toDomain(): Profile = Profile(
    id = id,
    name = name,
    avatarKey = avatarKey,
    heightCm = heightCm,
    birthDate = birthDate,
    preferredWeightUnit = enumValueOrDefault(preferredWeightUnit, WeightUnit.KILOGRAM),
    healthConnectEnabled = healthConnectEnabled,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Profile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    avatarKey = avatarKey,
    heightCm = heightCm,
    birthDate = birthDate,
    preferredWeightUnit = preferredWeightUnit.name,
    healthConnectEnabled = healthConnectEnabled,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WeightMeasurementEntity.toDomain(): WeightMeasurement = WeightMeasurement(
    id = id,
    profileId = profileId,
    weightKg = weightKg,
    measuredAt = measuredAt,
    zoneOffsetSeconds = zoneOffsetSeconds,
    source = enumValueOrDefault(source, MeasurementSource.IMPORT),
    isStable = isStable,
    deviceId = deviceId,
    deviceName = deviceName,
    deviceAddress = deviceAddress,
    note = note,
    rawPayloadHex = rawPayloadHex,
    impedanceOne = impedanceOne,
    impedanceTwo = impedanceTwo,
    bodyFatPercent = bodyFatPercent,
    muscleMassKg = muscleMassKg,
    bodyWaterPercent = bodyWaterPercent,
    boneMassKg = boneMassKg,
    visceralFatLevel = visceralFatLevel,
    metabolicAge = metabolicAge,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WeightMeasurement.toEntity(): WeightMeasurementEntity = WeightMeasurementEntity(
    id = id,
    profileId = profileId,
    weightKg = weightKg,
    measuredAt = measuredAt,
    zoneOffsetSeconds = zoneOffsetSeconds,
    source = source.name,
    isStable = isStable,
    deviceId = deviceId,
    deviceName = deviceName,
    deviceAddress = deviceAddress,
    note = note,
    rawPayloadHex = rawPayloadHex,
    impedanceOne = impedanceOne,
    impedanceTwo = impedanceTwo,
    bodyFatPercent = bodyFatPercent,
    muscleMassKg = muscleMassKg,
    bodyWaterPercent = bodyWaterPercent,
    boneMassKg = boneMassKg,
    visceralFatLevel = visceralFatLevel,
    metabolicAge = metabolicAge,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun GoalEntity.toDomain(): WeightGoal = WeightGoal(
    id = id,
    profileId = profileId,
    startWeightKg = startWeightKg,
    targetWeightKg = targetWeightKg,
    startDate = startDate,
    targetDate = targetDate,
    status = enumValueOrDefault(status, GoalStatus.PAUSED),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WeightGoal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    profileId = profileId,
    startWeightKg = startWeightKg,
    targetWeightKg = targetWeightKg,
    startDate = startDate,
    targetDate = targetDate,
    status = status.name,
    isActive = status == GoalStatus.ACTIVE,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ScaleDeviceEntity.toDomain(): ScaleDevice = ScaleDevice(
    id = id,
    displayName = displayName,
    bluetoothAddress = bluetoothAddress,
    protocolName = protocolName,
    lastConnectedAt = lastConnectedAt,
    lastSeenAt = lastSeenAt,
    isPreferred = isPreferred,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ScaleDevice.toEntity(): ScaleDeviceEntity = ScaleDeviceEntity(
    id = id,
    displayName = displayName,
    bluetoothAddress = bluetoothAddress,
    protocolName = protocolName,
    lastConnectedAt = lastConnectedAt,
    lastSeenAt = lastSeenAt,
    isPreferred = isPreferred,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private inline fun <reified T : Enum<T>> enumValueOrDefault(
    value: String,
    default: T
): T = enumValues<T>().firstOrNull { it.name == value } ?: default
