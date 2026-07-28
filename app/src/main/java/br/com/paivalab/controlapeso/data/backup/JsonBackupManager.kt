package br.com.paivalab.controlapeso.data.backup

import androidx.room.withTransaction
import br.com.paivalab.controlapeso.data.local.ControlaPesoDatabase
import br.com.paivalab.controlapeso.data.local.entity.GoalEntity
import br.com.paivalab.controlapeso.data.local.entity.ProfileEntity
import br.com.paivalab.controlapeso.data.local.entity.ScaleDeviceEntity
import br.com.paivalab.controlapeso.data.local.entity.WeightMeasurementEntity
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.repository.GoalRepository
import br.com.paivalab.controlapeso.domain.repository.MeasurementRepository
import br.com.paivalab.controlapeso.domain.repository.ProfileRepository
import br.com.paivalab.controlapeso.domain.repository.ScaleDeviceRepository
import br.com.paivalab.controlapeso.core.time.AppClock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first

enum class RestoreMode {
    MERGE,
    REPLACE
}

data class BackupPreview(
    val document: BackupDocument,
    val profileCount: Int,
    val measurementCount: Int,
    val goalCount: Int,
    val deviceCount: Int,
    val duplicateIdCount: Int
)

sealed interface BackupPreviewResult {
    data class Valid(val preview: BackupPreview) : BackupPreviewResult
    data class Invalid(val errors: List<String>) : BackupPreviewResult
}

sealed interface BackupRestoreResult {
    data class Success(
        val mode: RestoreMode,
        val importedMeasurements: Int,
        val skippedMeasurements: Int,
        val safetyBackupJson: String?
    ) : BackupRestoreResult
    data class Invalid(val errors: List<String>) : BackupRestoreResult
    data class Failure(val reason: String) : BackupRestoreResult
}

class JsonBackupManager(
    private val database: ControlaPesoDatabase,
    private val profileRepository: ProfileRepository,
    private val measurementRepository: MeasurementRepository,
    private val goalRepository: GoalRepository,
    private val deviceRepository: ScaleDeviceRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val clock: AppClock
) {
    suspend fun exportJson(exportedAt: Instant): String =
        BackupCodec.encode(createDocument(exportedAt))

    suspend fun preview(content: String): BackupPreviewResult {
        val decoded = BackupCodec.decodeAndValidate(content)
        if (decoded is BackupDecodeResult.Invalid) {
            return BackupPreviewResult.Invalid(decoded.errors)
        }
        val document = (decoded as BackupDecodeResult.Valid).document
        val existingIds = buildSet {
            addAll(profileRepository.getAll().map(Profile::id))
            addAll(measurementRepository.getAll().map(WeightMeasurement::id))
            addAll(goalRepository.getAll().map(WeightGoal::id))
            addAll(deviceRepository.getAll().map(ScaleDevice::id))
        }
        val importedIds = document.profiles.map(BackupProfile::id) +
            document.measurements.map(BackupMeasurement::id) +
            document.goals.map(BackupGoal::id) +
            document.devices.map(BackupDevice::id)
        return BackupPreviewResult.Valid(
            BackupPreview(
                document = document,
                profileCount = document.profiles.size,
                measurementCount = document.measurements.size,
                goalCount = document.goals.size,
                deviceCount = document.devices.size,
                duplicateIdCount = importedIds.count { it in existingIds }
            )
        )
    }

    suspend fun restore(content: String, mode: RestoreMode): BackupRestoreResult {
        val decoded = BackupCodec.decodeAndValidate(content)
        if (decoded is BackupDecodeResult.Invalid) {
            return BackupRestoreResult.Invalid(decoded.errors)
        }
        val document = (decoded as BackupDecodeResult.Valid).document
        return runCatching {
            val safetyBackup = if (mode == RestoreMode.REPLACE) {
                exportJson(clock.now())
            } else {
                null
            }
            val imported = if (mode == RestoreMode.REPLACE) {
                replace(document)
                document.measurements.size
            } else {
                merge(document)
            }
            preferencesRepository.restoreExportable(document.preferences.toPreferences())
            BackupRestoreResult.Success(
                mode = mode,
                importedMeasurements = imported,
                skippedMeasurements = document.measurements.size - imported,
                safetyBackupJson = safetyBackup
            )
        }.getOrElse { failure ->
            BackupRestoreResult.Failure(
                failure.message ?: "Falha ao restaurar o backup."
            )
        }
    }

    private suspend fun createDocument(exportedAt: Instant): BackupDocument {
        val preferences = preferencesRepository.preferences.first()
        return BackupDocument(
            exportedAt = exportedAt.toString(),
            profiles = profileRepository.getAll().map(Profile::toBackup),
            measurements = measurementRepository.getAll()
                .map(WeightMeasurement::toBackup),
            goals = goalRepository.getAll().map(WeightGoal::toBackup),
            devices = deviceRepository.getAll().map(ScaleDevice::toBackup),
            preferences = preferences.toBackup()
        )
    }

    private suspend fun replace(document: BackupDocument) {
        database.withTransaction {
            database.measurementDao().deleteAll()
            database.goalDao().deleteAll()
            database.profileDao().deleteAll()
            database.scaleDeviceDao().deleteAll()

            val devices = document.devices.map(BackupDevice::toEntity)
            val validDeviceIds = devices.mapTo(mutableSetOf(), ScaleDeviceEntity::id)
            database.profileDao().upsertAll(document.profiles.map(BackupProfile::toEntity))
            database.scaleDeviceDao().upsertAll(devices)
            database.measurementDao().upsertAll(
                document.measurements.map { it.toEntity(validDeviceIds) }
            )
            database.goalDao().upsertAll(document.goals.map(BackupGoal::toEntity))
        }
    }

    private suspend fun merge(document: BackupDocument): Int {
        var importedMeasurements = 0
        database.withTransaction {
            val existingProfiles = database.profileDao().getAll()
            val profilesById = existingProfiles.associateByTo(linkedMapOf(), ProfileEntity::id)
            document.profiles.map(BackupProfile::toEntity).forEach { imported ->
                val current = profilesById[imported.id]
                if (current == null || imported.updatedAt > current.updatedAt) {
                    profilesById[imported.id] = imported
                }
            }
            val activeId = existingProfiles.firstOrNull { it.isActive }?.id
                ?: profilesById.values.firstOrNull { it.isActive }?.id
                ?: profilesById.keys.firstOrNull()
            val normalizedProfiles = profilesById.values.map { profile ->
                profile.copy(isActive = profile.id == activeId)
            }
            database.profileDao().upsertAll(normalizedProfiles)

            val existingDevices = database.scaleDeviceDao().getAll()
            val devicesById = existingDevices.associateByTo(linkedMapOf(), ScaleDeviceEntity::id)
            val addressOwners = existingDevices.associate { it.bluetoothAddress to it.id }
                .toMutableMap()
            document.devices.map(BackupDevice::toEntity).forEach { imported ->
                val owner = addressOwners[imported.bluetoothAddress]
                val current = devicesById[imported.id]
                if (owner == null || owner == imported.id) {
                    if (current == null || imported.updatedAt > current.updatedAt) {
                        devicesById[imported.id] = imported
                        addressOwners[imported.bluetoothAddress] = imported.id
                    }
                }
            }
            database.scaleDeviceDao().upsertAll(devicesById.values.toList())
            val validDeviceIds = devicesById.keys

            val existingMeasurements = database.measurementDao().getAll()
                .associateByTo(linkedMapOf(), WeightMeasurementEntity::id)
            document.measurements.forEach { backup ->
                val imported = backup.toEntity(validDeviceIds)
                val current = existingMeasurements[imported.id]
                if (current == null) {
                    existingMeasurements[imported.id] = imported
                    importedMeasurements += 1
                } else if (imported.updatedAt > current.updatedAt) {
                    existingMeasurements[imported.id] = imported
                }
            }
            database.measurementDao().upsertAll(existingMeasurements.values.toList())

            val goalsById = database.goalDao().getAll()
                .associateByTo(linkedMapOf(), GoalEntity::id)
            document.goals.map(BackupGoal::toEntity).forEach { imported ->
                val current = goalsById[imported.id]
                if (current == null || imported.updatedAt > current.updatedAt) {
                    goalsById[imported.id] = imported
                }
            }
            val activeGoalByProfile = goalsById.values
                .filter(GoalEntity::isActive)
                .groupBy(GoalEntity::profileId)
                .mapValues { (_, goals) -> goals.maxBy(GoalEntity::updatedAt).id }
            database.goalDao().upsertAll(
                goalsById.values.map { goal ->
                    goal.copy(
                        isActive = activeGoalByProfile[goal.profileId] == goal.id,
                        status = if (
                            activeGoalByProfile[goal.profileId] == goal.id
                        ) {
                            GoalStatus.ACTIVE.name
                        } else if (goal.status == GoalStatus.ACTIVE.name) {
                            GoalStatus.PAUSED.name
                        } else {
                            goal.status
                        }
                    )
                }
            )
        }
        return importedMeasurements
    }
}

private fun Profile.toBackup() = BackupProfile(
    id = id,
    name = name,
    avatarKey = avatarKey,
    heightCm = heightCm,
    birthDate = birthDate?.toString(),
    preferredWeightUnit = preferredWeightUnit.name,
    healthConnectEnabled = healthConnectEnabled,
    isActive = isActive,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private fun WeightMeasurement.toBackup() = BackupMeasurement(
    id = id,
    profileId = profileId,
    weightKg = weightKg,
    measuredAt = measuredAt.toString(),
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
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private fun WeightGoal.toBackup() = BackupGoal(
    id = id,
    profileId = profileId,
    startWeightKg = startWeightKg,
    targetWeightKg = targetWeightKg,
    startDate = startDate.toString(),
    targetDate = targetDate?.toString(),
    status = status.name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private fun ScaleDevice.toBackup() = BackupDevice(
    id = id,
    displayName = displayName,
    bluetoothAddress = bluetoothAddress,
    protocolName = protocolName,
    lastConnectedAt = lastConnectedAt?.toString(),
    lastSeenAt = lastSeenAt?.toString(),
    isPreferred = isPreferred,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private fun AppPreferences.toBackup() = BackupPreferences(
    themeMode = themeMode.name,
    dynamicColors = dynamicColors,
    visualEffects = visualEffects.name,
    defaultWeightUnit = defaultWeightUnit.name,
    autoSaveStableMeasurement = autoSaveStableMeasurement,
    confirmBeforeSaving = confirmBeforeSaving,
    vibrationEnabled = vibrationEnabled,
    soundEnabled = soundEnabled,
    defaultHistoryPeriod = defaultHistoryPeriod.name,
    movingAverageEnabled = movingAverageEnabled,
    historyGrouping = historyGrouping.name,
    chartSize = chartSize.name,
    highContrast = highContrast,
    remindersEnabled = remindersEnabled,
    reminderDaysMask = reminderDaysMask,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute
)

private fun BackupProfile.toEntity() = ProfileEntity(
    id = id,
    name = name,
    avatarKey = avatarKey,
    heightCm = heightCm,
    birthDate = birthDate?.let(LocalDate::parse),
    preferredWeightUnit = preferredWeightUnit,
    healthConnectEnabled = healthConnectEnabled,
    isActive = isActive,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt)
)

private fun BackupMeasurement.toEntity(validDeviceIds: Set<String>) =
    WeightMeasurementEntity(
        id = id,
        profileId = profileId,
        weightKg = weightKg,
        measuredAt = Instant.parse(measuredAt),
        zoneOffsetSeconds = zoneOffsetSeconds,
        source = source,
        isStable = isStable,
        deviceId = deviceId?.takeIf { it in validDeviceIds },
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
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )

private fun BackupGoal.toEntity() = GoalEntity(
    id = id,
    profileId = profileId,
    startWeightKg = startWeightKg,
    targetWeightKg = targetWeightKg,
    startDate = LocalDate.parse(startDate),
    targetDate = targetDate?.let(LocalDate::parse),
    status = status,
    isActive = status == GoalStatus.ACTIVE.name,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt)
)

private fun BackupDevice.toEntity() = ScaleDeviceEntity(
    id = id,
    displayName = displayName,
    bluetoothAddress = bluetoothAddress,
    protocolName = protocolName,
    lastConnectedAt = lastConnectedAt?.let(Instant::parse),
    lastSeenAt = lastSeenAt?.let(Instant::parse),
    isPreferred = isPreferred,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt)
)

private fun BackupPreferences.toPreferences() = AppPreferences(
    onboardingCompleted = true,
    themeMode = ThemeMode.valueOf(themeMode),
    dynamicColors = dynamicColors,
    visualEffects = VisualEffects.valueOf(visualEffects),
    defaultWeightUnit = WeightUnit.valueOf(defaultWeightUnit),
    autoSaveStableMeasurement = autoSaveStableMeasurement,
    confirmBeforeSaving = confirmBeforeSaving,
    vibrationEnabled = vibrationEnabled,
    soundEnabled = soundEnabled,
    defaultHistoryPeriod = HistoryPeriod.valueOf(defaultHistoryPeriod),
    movingAverageEnabled = movingAverageEnabled,
    historyGrouping = HistoryGrouping.valueOf(historyGrouping),
    chartSize = ChartSize.valueOf(chartSize),
    highContrast = highContrast,
    remindersEnabled = remindersEnabled,
    reminderDaysMask = reminderDaysMask,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute
)
