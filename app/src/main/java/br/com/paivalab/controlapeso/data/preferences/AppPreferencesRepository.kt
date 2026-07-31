package br.com.paivalab.controlapeso.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.controlaPesoPreferences by preferencesDataStore(
    name = "controla_peso_preferences"
)

class AppPreferencesRepository(context: Context) {
    private val dataStore = (context.applicationContext ?: context).controlaPesoPreferences

    val preferences: Flow<AppPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw exception
        }
        .map(::mapPreferences)

    suspend fun setOnboardingCompleted(value: Boolean) =
        set(Keys.ONBOARDING_COMPLETED, value)

    suspend fun setThemeMode(value: ThemeMode) = set(Keys.THEME_MODE, value.name)
    suspend fun setDynamicColors(value: Boolean) = set(Keys.DYNAMIC_COLORS, value)
    suspend fun setVisualEffects(value: VisualEffects) = set(Keys.VISUAL_EFFECTS, value.name)
    suspend fun setDefaultWeightUnit(value: WeightUnit) =
        set(Keys.DEFAULT_WEIGHT_UNIT, value.name)
    suspend fun setAutoSave(value: Boolean) = set(Keys.AUTO_SAVE, value)
    suspend fun setConfirmBeforeSaving(value: Boolean) = set(Keys.CONFIRM_SAVE, value)
    suspend fun setVibration(value: Boolean) = set(Keys.VIBRATION, value)
    suspend fun setSound(value: Boolean) = set(Keys.SOUND, value)
    suspend fun setDefaultHistoryPeriod(value: HistoryPeriod) =
        set(Keys.HISTORY_PERIOD, value.name)
    suspend fun setMovingAverage(value: Boolean) = set(Keys.MOVING_AVERAGE, value)
    suspend fun setHistoryGrouping(value: HistoryGrouping) =
        set(Keys.HISTORY_GROUPING, value.name)
    suspend fun setChartSize(value: ChartSize) = set(Keys.CHART_SIZE, value.name)
    suspend fun setHighContrast(value: Boolean) = set(Keys.HIGH_CONTRAST, value)
    suspend fun setDetailedBleLogs(value: Boolean) = set(Keys.DETAILED_BLE_LOGS, value)

    suspend fun setHealthLastSyncAt(value: Instant?) {
        dataStore.edit { preferences ->
            if (value == null) preferences.remove(Keys.HEALTH_LAST_SYNC_AT)
            else preferences[Keys.HEALTH_LAST_SYNC_AT] = value.toString()
        }
    }

    suspend fun setLastLocalBackupAt(value: Instant?) {
        dataStore.edit { preferences ->
            if (value == null) preferences.remove(Keys.LAST_LOCAL_BACKUP_AT)
            else preferences[Keys.LAST_LOCAL_BACKUP_AT] = value.toString()
        }
    }

    suspend fun setLocalBackupFrequency(value: LocalBackupFrequency) =
        set(Keys.LOCAL_BACKUP_FREQUENCY, value.name)

    /**
     * Persists only the public GitHub release response paired with its ETag.
     * Keeping the pair lets a subsequent 304 response still display an update
     * that was deferred in a previous application process.
     */
    suspend fun setReleaseUpdateCache(eTag: String?, cachedRelease: String?) {
        dataStore.edit { preferences ->
            if (eTag.isNullOrBlank()) preferences.remove(Keys.RELEASE_UPDATE_ETAG)
            else preferences[Keys.RELEASE_UPDATE_ETAG] = eTag
            if (cachedRelease.isNullOrBlank()) {
                preferences.remove(Keys.RELEASE_UPDATE_CACHED_RELEASE)
            } else {
                preferences[Keys.RELEASE_UPDATE_CACHED_RELEASE] = cachedRelease
            }
        }
    }

    suspend fun setConfirmedBleUnit(value: WeightUnit?) {
        dataStore.edit { preferences ->
            if (value == null) preferences.remove(Keys.CONFIRMED_BLE_UNIT)
            else preferences[Keys.CONFIRMED_BLE_UNIT] = value.name
        }
    }

    suspend fun setPendingGoalTargetKg(value: Double?) {
        dataStore.edit { preferences ->
            if (value == null) preferences.remove(Keys.PENDING_GOAL_TARGET_KG)
            else preferences[Keys.PENDING_GOAL_TARGET_KG] = value
        }
    }

    suspend fun setReminder(
        enabled: Boolean,
        daysMask: Int,
        hour: Int,
        minute: Int
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.REMINDERS_ENABLED] = enabled
            preferences[Keys.REMINDER_DAYS_MASK] = daysMask and 0b1111111
            preferences[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            preferences[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun restoreExportable(value: AppPreferences) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = value.themeMode.name
            preferences[Keys.DYNAMIC_COLORS] = value.dynamicColors
            preferences[Keys.VISUAL_EFFECTS] = value.visualEffects.name
            preferences[Keys.DEFAULT_WEIGHT_UNIT] = value.defaultWeightUnit.name
            preferences[Keys.AUTO_SAVE] = value.autoSaveStableMeasurement
            preferences[Keys.CONFIRM_SAVE] = value.confirmBeforeSaving
            preferences[Keys.VIBRATION] = value.vibrationEnabled
            preferences[Keys.SOUND] = value.soundEnabled
            preferences[Keys.HISTORY_PERIOD] = value.defaultHistoryPeriod.name
            preferences[Keys.MOVING_AVERAGE] = value.movingAverageEnabled
            preferences[Keys.HISTORY_GROUPING] = value.historyGrouping.name
            preferences[Keys.CHART_SIZE] = value.chartSize.name
            preferences[Keys.HIGH_CONTRAST] = value.highContrast
            preferences[Keys.REMINDERS_ENABLED] = value.remindersEnabled
            preferences[Keys.REMINDER_DAYS_MASK] = value.reminderDaysMask
            preferences[Keys.REMINDER_HOUR] = value.reminderHour
            preferences[Keys.REMINDER_MINUTE] = value.reminderMinute
            preferences[Keys.LOCAL_BACKUP_FREQUENCY] = value.localBackupFrequency.name
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences -> preferences[key] = value }
    }

    private fun mapPreferences(values: Preferences): AppPreferences =
        AppPreferences(
            onboardingCompleted = values[Keys.ONBOARDING_COMPLETED] ?: false,
            themeMode = enumValue(values[Keys.THEME_MODE], ThemeMode.SYSTEM),
            dynamicColors = values[Keys.DYNAMIC_COLORS] ?: true,
            visualEffects = enumValue(values[Keys.VISUAL_EFFECTS], VisualEffects.FULL),
            defaultWeightUnit =
                enumValue(values[Keys.DEFAULT_WEIGHT_UNIT], WeightUnit.KILOGRAM),
            autoSaveStableMeasurement = values[Keys.AUTO_SAVE] ?: false,
            confirmBeforeSaving = values[Keys.CONFIRM_SAVE] ?: true,
            vibrationEnabled = values[Keys.VIBRATION] ?: true,
            soundEnabled = values[Keys.SOUND] ?: false,
            defaultHistoryPeriod =
                enumValue(values[Keys.HISTORY_PERIOD], HistoryPeriod.DAYS_30),
            movingAverageEnabled = values[Keys.MOVING_AVERAGE] ?: true,
            historyGrouping =
                enumValue(values[Keys.HISTORY_GROUPING], HistoryGrouping.NONE),
            chartSize = enumValue(values[Keys.CHART_SIZE], ChartSize.COMFORTABLE),
            highContrast = values[Keys.HIGH_CONTRAST] ?: false,
            detailedBleLogs = values[Keys.DETAILED_BLE_LOGS] ?: false,
            confirmedBleUnit =
                values[Keys.CONFIRMED_BLE_UNIT]?.let { enumValueOrNull<WeightUnit>(it) },
            pendingGoalTargetKg = values[Keys.PENDING_GOAL_TARGET_KG],
            remindersEnabled = values[Keys.REMINDERS_ENABLED] ?: false,
            reminderDaysMask = values[Keys.REMINDER_DAYS_MASK] ?: 0b1111111,
            reminderHour = (values[Keys.REMINDER_HOUR] ?: 9).coerceIn(0, 23),
            reminderMinute = (values[Keys.REMINDER_MINUTE] ?: 0).coerceIn(0, 59),
            releaseUpdateEtag = values[Keys.RELEASE_UPDATE_ETAG],
            releaseUpdateCachedRelease = values[Keys.RELEASE_UPDATE_CACHED_RELEASE],
            healthLastSyncAt = values[Keys.HEALTH_LAST_SYNC_AT]
                ?.let { value -> runCatching { Instant.parse(value) }.getOrNull() },
            localBackupFrequency = enumValue(
                values[Keys.LOCAL_BACKUP_FREQUENCY],
                LocalBackupFrequency.OFF
            ),
            lastLocalBackupAt = values[Keys.LAST_LOCAL_BACKUP_AT]
                ?.let { value -> runCatching { Instant.parse(value) }.getOrNull() },
        )

    private inline fun <reified T : Enum<T>> enumValue(value: String?, default: T): T =
        value?.let { enumValueOrNull<T>(it) } ?: default

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { it.name == value }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val VISUAL_EFFECTS = stringPreferencesKey("visual_effects")
        val DEFAULT_WEIGHT_UNIT = stringPreferencesKey("default_weight_unit")
        val AUTO_SAVE = booleanPreferencesKey("auto_save_stable_measurement")
        val CONFIRM_SAVE = booleanPreferencesKey("confirm_before_saving")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HISTORY_PERIOD = stringPreferencesKey("history_period")
        val MOVING_AVERAGE = booleanPreferencesKey("moving_average")
        val HISTORY_GROUPING = stringPreferencesKey("history_grouping")
        val CHART_SIZE = stringPreferencesKey("chart_size")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val DETAILED_BLE_LOGS = booleanPreferencesKey("detailed_ble_logs")
        val CONFIRMED_BLE_UNIT = stringPreferencesKey("confirmed_ble_unit")
        val PENDING_GOAL_TARGET_KG = doublePreferencesKey("pending_goal_target_kg")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_DAYS_MASK = intPreferencesKey("reminder_days_mask")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val RELEASE_UPDATE_ETAG = stringPreferencesKey("release_update_etag")
        val RELEASE_UPDATE_CACHED_RELEASE = stringPreferencesKey("release_update_cached_release")
        val HEALTH_LAST_SYNC_AT = stringPreferencesKey("health_last_sync_at")
        val LOCAL_BACKUP_FREQUENCY = stringPreferencesKey("local_backup_frequency")
        val LAST_LOCAL_BACKUP_AT = stringPreferencesKey("last_local_backup_at")
    }
}
