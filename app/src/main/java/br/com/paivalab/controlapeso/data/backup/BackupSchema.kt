package br.com.paivalab.controlapeso.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupDocument(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportedAt: String,
    val profiles: List<BackupProfile>,
    val measurements: List<BackupMeasurement>,
    val goals: List<BackupGoal>,
    val devices: List<BackupDevice> = emptyList(),
    val preferences: BackupPreferences
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

@Serializable
data class BackupProfile(
    val id: String,
    val name: String,
    val avatarKey: String?,
    val heightCm: Double?,
    val birthDate: String?,
    val preferredWeightUnit: String,
    val healthConnectEnabled: Boolean,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BackupMeasurement(
    val id: String,
    val profileId: String,
    val weightKg: Double,
    val measuredAt: String,
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
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BackupGoal(
    val id: String,
    val profileId: String,
    val startWeightKg: Double,
    val targetWeightKg: Double,
    val startDate: String,
    val targetDate: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BackupDevice(
    val id: String,
    val displayName: String?,
    val bluetoothAddress: String,
    val protocolName: String?,
    val lastConnectedAt: String?,
    val lastSeenAt: String?,
    val isPreferred: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BackupPreferences(
    val themeMode: String,
    val dynamicColors: Boolean,
    val visualEffects: String,
    val defaultWeightUnit: String,
    val autoSaveStableMeasurement: Boolean,
    val confirmBeforeSaving: Boolean,
    val vibrationEnabled: Boolean,
    val soundEnabled: Boolean,
    val defaultHistoryPeriod: String,
    val movingAverageEnabled: Boolean,
    val historyGrouping: String,
    val chartSize: String,
    val highContrast: Boolean,
    val remindersEnabled: Boolean,
    val reminderDaysMask: Int,
    val reminderHour: Int,
    val reminderMinute: Int
)
