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

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val healthAvailability: HealthConnectAvailability =
        HealthConnectAvailability.ANDROID_VERSION_UNSUPPORTED,
    val healthPermissionGranted: Boolean = false,
    val isHealthSyncing: Boolean = false,
    val healthSyncedCount: Int = 0,
    val message: SettingsMessage? = null
)

class SettingsViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val local = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        container.preferencesRepository.preferences,
        container.profileRepository.observeAll(),
        local
    ) { preferences, profiles, state ->
        state.copy(
            preferences = preferences,
            profiles = profiles,
            activeProfile = profiles.singleOrNull(Profile::isActive)
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
        local.update { it.copy(healthAvailability = availability) }
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
                message = if (granted) null else SettingsMessage.HEALTH_PERMISSION_REQUIRED
            )
        }
        if (granted) setHealthConnectEnabled(true)
    }

    fun setHealthConnectEnabled(enabled: Boolean) {
        val profile = uiState.value.activeProfile ?: return
        launch {
            if (enabled && !container.healthConnectWeightWriter.hasWritePermission()) {
                local.update {
                    it.copy(message = SettingsMessage.HEALTH_PERMISSION_REQUIRED)
                }
                return@launch
            }
            container.profileRepository.update(
                profile.copy(
                    healthConnectEnabled = enabled,
                    updatedAt = container.clock.now()
                )
            )
        }
    }

    fun syncActiveProfile() {
        val profile = uiState.value.activeProfile ?: return
        if (!profile.healthConnectEnabled) return
        local.update {
            it.copy(isHealthSyncing = true, healthSyncedCount = 0, message = null)
        }
        viewModelScope.launch {
            var synced = 0
            var failed = false
            val measurements = container.measurementRepository.getAll()
                .filter {
                    it.profileId == profile.id &&
                        it.source != MeasurementSource.HEALTH_CONNECT &&
                        it.source != MeasurementSource.DEMO
                }
            for (measurement in measurements) {
                when (container.healthConnectWeightWriter.write(measurement)) {
                    HealthConnectWriteResult.Written -> synced++
                    HealthConnectWriteResult.PermissionRequired -> {
                        local.update {
                            it.copy(
                                healthPermissionGranted = false,
                                message = SettingsMessage.HEALTH_PERMISSION_REQUIRED
                            )
                        }
                        failed = true
                        break
                    }
                    else -> {
                        failed = true
                        break
                    }
                }
            }
            local.update {
                it.copy(
                    isHealthSyncing = false,
                    healthSyncedCount = synced,
                    message = when {
                        it.message == SettingsMessage.HEALTH_PERMISSION_REQUIRED ->
                            it.message
                        failed -> SettingsMessage.HEALTH_SYNC_FAILED
                        else -> SettingsMessage.HEALTH_SYNC_COMPLETE
                    }
                )
            }
        }
    }

    fun dismissMessage() = local.update { it.copy(message = null) }

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
