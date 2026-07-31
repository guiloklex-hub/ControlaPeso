package br.com.paivalab.controlapeso.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectAvailability
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectWriteResult
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SettingsMessage {
    HEALTH_SYNC_COMPLETE,
    HEALTH_PERMISSION_REQUIRED,
    HEALTH_SYNC_FAILED,
    DEMO_DATA_CLEARED
}

enum class HealthSyncError {
    PERMISSION_REQUIRED,
    UNAVAILABLE,
    FAILED
}

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val healthAvailability: HealthConnectAvailability =
        HealthConnectAvailability.ANDROID_VERSION_UNSUPPORTED,
    val healthPermissionGranted: Boolean = false,
    val isHealthSyncing: Boolean = false,
    val healthSyncedCount: Int = 0,
    val healthSyncConfirmationVisible: Boolean = false,
    val healthSyncMeasurementCount: Int = 0,
    val healthSyncFirstAt: Instant? = null,
    val healthSyncLastAt: Instant? = null,
    val healthSyncError: HealthSyncError? = null,
    val message: SettingsMessage? = null
)

internal object HealthSyncPreviewCalculator {
    fun eligibleMeasurements(
        measurements: List<WeightMeasurement>,
        profileId: String
    ): List<WeightMeasurement> = measurements
        .asSequence()
        .filter { it.profileId == profileId }
        .filter {
            it.source != MeasurementSource.HEALTH_CONNECT &&
                it.source != MeasurementSource.DEMO
        }
        .sortedWith(compareBy(WeightMeasurement::measuredAt, WeightMeasurement::id))
        .toList()

    fun preview(
        measurements: List<WeightMeasurement>,
        profileId: String
    ): HealthSyncPreview {
        val eligible = eligibleMeasurements(measurements, profileId)
        return HealthSyncPreview(
            count = eligible.size,
            firstMeasuredAt = eligible.firstOrNull()?.measuredAt,
            lastMeasuredAt = eligible.lastOrNull()?.measuredAt
        )
    }
}

internal data class HealthSyncPreview(
    val count: Int,
    val firstMeasuredAt: Instant?,
    val lastMeasuredAt: Instant?
)

class SettingsViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val local = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        container.preferencesRepository.preferences,
        container.profileRepository.observeAll(),
        container.measurementRepository.observeAll(),
        local
    ) { preferences, profiles, measurements, state ->
        val activeProfile = profiles.singleOrNull(Profile::isActive)
        val preview = activeProfile?.let {
            HealthSyncPreviewCalculator.preview(measurements, it.id)
        } ?: HealthSyncPreview(0, null, null)
        state.copy(
            preferences = preferences,
            profiles = profiles,
            activeProfile = activeProfile,
            healthSyncMeasurementCount = preview.count,
            healthSyncFirstAt = preview.firstMeasuredAt,
            healthSyncLastAt = preview.lastMeasuredAt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    init {
        refreshHealthConnect()
    }

    fun setTheme(value: ThemeMode) = launch {
        container.preferencesRepository.setThemeMode(value)
    }

    fun setDynamicColors(value: Boolean) = launch {
        container.preferencesRepository.setDynamicColors(value)
    }

    fun setVisualEffects(value: VisualEffects) = launch {
        container.preferencesRepository.setVisualEffects(value)
    }

    fun setDefaultWeightUnit(value: WeightUnit) = launch {
        container.preferencesRepository.setDefaultWeightUnit(value)
    }

    fun setDefaultProfile(profileId: String) = launch {
        container.profileRepository.setActive(profileId)
    }

    fun setAutoSave(value: Boolean) = launch {
        container.preferencesRepository.setAutoSave(value)
    }

    fun setConfirmBeforeSaving(value: Boolean) = launch {
        container.preferencesRepository.setConfirmBeforeSaving(value)
    }

    fun setVibration(value: Boolean) = launch {
        container.preferencesRepository.setVibration(value)
    }

    fun setSound(value: Boolean) = launch {
        container.preferencesRepository.setSound(value)
    }

    fun setDefaultHistoryPeriod(value: HistoryPeriod) = launch {
        container.preferencesRepository.setDefaultHistoryPeriod(value)
    }

    fun setMovingAverage(value: Boolean) = launch {
        container.preferencesRepository.setMovingAverage(value)
    }

    fun setHistoryGrouping(value: HistoryGrouping) = launch {
        container.preferencesRepository.setHistoryGrouping(value)
    }

    fun setChartSize(value: ChartSize) = launch {
        container.preferencesRepository.setChartSize(value)
    }

    fun setHighContrast(value: Boolean) = launch {
        container.preferencesRepository.setHighContrast(value)
    }

    fun setDetailedBleLogs(value: Boolean) = launch {
        container.preferencesRepository.setDetailedBleLogs(value)
    }

    fun setReminderEnabled(value: Boolean) {
        val preferences = uiState.value.preferences.copy(remindersEnabled = value)
        launch {
            container.preferencesRepository.setReminder(
                enabled = value,
                daysMask = preferences.reminderDaysMask,
                hour = preferences.reminderHour,
                minute = preferences.reminderMinute
            )
            container.reminderScheduler.update(preferences)
        }
    }

    fun toggleReminderDay(dayIndex: Int) {
        if (dayIndex !in 0..6) return
        val current = uiState.value.preferences
        val mask = current.reminderDaysMask xor (1 shl dayIndex)
        val updated = current.copy(reminderDaysMask = mask)
        launch {
            container.preferencesRepository.setReminder(
                enabled = updated.remindersEnabled,
                daysMask = mask,
                hour = updated.reminderHour,
                minute = updated.reminderMinute
            )
            container.reminderScheduler.update(updated)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val current = uiState.value.preferences
        val updated = current.copy(
            reminderHour = hour.coerceIn(0, 23),
            reminderMinute = minute.coerceIn(0, 59)
        )
        launch {
            container.preferencesRepository.setReminder(
                enabled = updated.remindersEnabled,
                daysMask = updated.reminderDaysMask,
                hour = updated.reminderHour,
                minute = updated.reminderMinute
            )
            container.reminderScheduler.update(updated)
        }
    }

    fun refreshHealthConnect() {
        val availability = container.healthConnectWeightWriter.availability()
        local.update {
            it.copy(
                healthAvailability = availability,
                healthPermissionGranted = if (
                    availability == HealthConnectAvailability.AVAILABLE
                ) it.healthPermissionGranted else false
            )
        }
        if (availability != HealthConnectAvailability.AVAILABLE) return
        viewModelScope.launch {
            val granted = container.healthConnectWeightWriter.hasWritePermission()
            local.update { it.copy(healthPermissionGranted = granted) }
        }
    }

    fun onHealthPermissionResult(grantedPermissions: Set<String>) {
        val granted = grantedPermissions.containsAll(
            container.healthConnectWeightWriter.requiredPermissions
        )
        local.update {
            it.copy(
                healthPermissionGranted = granted,
                healthSyncError = if (granted) null else HealthSyncError.PERMISSION_REQUIRED,
                message = if (granted) null else SettingsMessage.HEALTH_PERMISSION_REQUIRED
            )
        }
    }

    fun setHealthConnectEnabled(enabled: Boolean) {
        val profile = uiState.value.activeProfile ?: return
        launch {
            if (
                enabled &&
                container.healthConnectWeightWriter.availability() !=
                HealthConnectAvailability.AVAILABLE
            ) {
                local.update {
                    it.copy(
                        healthSyncError = HealthSyncError.UNAVAILABLE,
                        message = SettingsMessage.HEALTH_SYNC_FAILED
                    )
                }
                return@launch
            }
            if (enabled && !container.healthConnectWeightWriter.hasWritePermission()) {
                local.update {
                    it.copy(
                        healthSyncError = HealthSyncError.PERMISSION_REQUIRED,
                        message = SettingsMessage.HEALTH_PERMISSION_REQUIRED
                    )
                }
                return@launch
            }
            container.profileRepository.update(
                profile.copy(
                    healthConnectEnabled = enabled,
                    updatedAt = container.clock.now()
                )
            )
            local.update { it.copy(healthSyncError = null) }
        }
    }

    fun requestHealthSync() {
        val state = uiState.value
        if (
            state.activeProfile?.healthConnectEnabled != true ||
            !state.healthPermissionGranted ||
            state.isHealthSyncing ||
            state.healthSyncMeasurementCount == 0
        ) return
        local.update {
            it.copy(
                healthSyncConfirmationVisible = true,
                message = null
            )
        }
    }

    fun dismissHealthSyncConfirmation() = local.update {
        it.copy(healthSyncConfirmationVisible = false)
    }

    fun confirmHealthSync() {
        if (!uiState.value.healthSyncConfirmationVisible) return
        local.update { it.copy(healthSyncConfirmationVisible = false) }
        syncActiveProfile()
    }

    private fun syncActiveProfile() {
        val profile = uiState.value.activeProfile ?: return
        if (!profile.healthConnectEnabled) return
        local.update {
            it.copy(
                isHealthSyncing = true,
                healthSyncConfirmationVisible = false,
                healthSyncedCount = 0,
                message = null
            )
        }
        viewModelScope.launch {
            try {
                if (
                    container.healthConnectWeightWriter.availability() !=
                    HealthConnectAvailability.AVAILABLE
                ) {
                    local.update {
                        it.copy(
                            isHealthSyncing = false,
                            healthSyncError = HealthSyncError.UNAVAILABLE,
                            message = SettingsMessage.HEALTH_SYNC_FAILED
                        )
                    }
                    return@launch
                }
                if (!container.healthConnectWeightWriter.hasWritePermission()) {
                    local.update {
                        it.copy(
                            isHealthSyncing = false,
                            healthPermissionGranted = false,
                            healthSyncError = HealthSyncError.PERMISSION_REQUIRED,
                            message = SettingsMessage.HEALTH_PERMISSION_REQUIRED
                        )
                    }
                    return@launch
                }
                var synced = 0
                var syncError: HealthSyncError? = null
                val measurements = HealthSyncPreviewCalculator.eligibleMeasurements(
                    measurements = container.measurementRepository.getAll(),
                    profileId = profile.id
                )
                for (measurement in measurements) {
                    when (container.healthConnectWeightWriter.write(measurement)) {
                        HealthConnectWriteResult.Written -> synced++
                        HealthConnectWriteResult.PermissionRequired -> {
                            syncError = HealthSyncError.PERMISSION_REQUIRED
                            break
                        }
                        HealthConnectWriteResult.Unavailable -> {
                            syncError = HealthSyncError.UNAVAILABLE
                            break
                        }
                        is HealthConnectWriteResult.Failed -> {
                            syncError = HealthSyncError.FAILED
                            break
                        }
                    }
                }
                if (syncError == null) {
                    container.preferencesRepository.setHealthLastSyncAt(container.clock.now())
                }
                local.update {
                    it.copy(
                        isHealthSyncing = false,
                        healthSyncedCount = synced,
                        healthPermissionGranted =
                            if (syncError == HealthSyncError.PERMISSION_REQUIRED) false
                            else it.healthPermissionGranted,
                        healthSyncError = syncError,
                        message = when (syncError) {
                            HealthSyncError.PERMISSION_REQUIRED ->
                                SettingsMessage.HEALTH_PERMISSION_REQUIRED
                            null -> SettingsMessage.HEALTH_SYNC_COMPLETE
                            else -> SettingsMessage.HEALTH_SYNC_FAILED
                        }
                    )
                }
            } catch (cancellation: kotlinx.coroutines.CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                local.update {
                    it.copy(
                        isHealthSyncing = false,
                        healthSyncError = HealthSyncError.FAILED,
                        message = SettingsMessage.HEALTH_SYNC_FAILED
                    )
                }
            }
        }
    }

    fun dismissMessage() = local.update { it.copy(message = null) }

    fun reviewOnboarding() = launch {
        container.preferencesRepository.setOnboardingCompleted(false)
    }

    fun clearDemoData() {
        launch {
            container.measurementRepository.deleteDemoData()
            local.update { it.copy(message = SettingsMessage.DEMO_DATA_CLEARED) }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(container) as T
    }
}
