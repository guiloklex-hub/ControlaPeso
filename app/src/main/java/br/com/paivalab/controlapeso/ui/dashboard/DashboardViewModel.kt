package br.com.paivalab.controlapeso.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.usecase.goals.CalculateGoalProgress
import br.com.paivalab.controlapeso.domain.usecase.goals.GoalProgress
import br.com.paivalab.controlapeso.domain.usecase.statistics.CalculateBmi
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import br.com.paivalab.controlapeso.domain.usecase.statistics.WeightStatistics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val preferences: AppPreferences = AppPreferences(),
    val measurements: List<WeightMeasurement> = emptyList(),
    val statistics: WeightStatistics? = null,
    val activeGoal: WeightGoal? = null,
    val goalProgress: GoalProgress? = null,
    val bmi: Double? = null,
    val currentHour: Int = 12,
    val hasError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(container: AppContainer) : ViewModel() {
    private val profileData = container.profileRepository.observeActive()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(Triple(null, emptyList(), null))
            } else {
                combine(
                    container.measurementRepository.observeForProfile(profile.id),
                    container.goalRepository.observeActive(profile.id)
                ) { measurements, goal ->
                    Triple(profile, measurements.sortedBy(WeightMeasurement::measuredAt), goal)
                }
            }
        }

    val uiState = combine(
        profileData,
        container.preferencesRepository.preferences
    ) { (profile, measurements, goal), preferences ->
        val statistics = MeasurementStatistics.calculate(measurements)
        val latestWeight = measurements.lastOrNull()?.weightKg
        DashboardUiState(
            isLoading = false,
            profile = profile,
            preferences = preferences,
            measurements = measurements,
            statistics = statistics,
            activeGoal = goal,
            goalProgress = if (goal != null && latestWeight != null) {
                CalculateGoalProgress(goal, latestWeight)
            } else {
                null
            },
            bmi = latestWeight?.let { CalculateBmi(it, profile?.heightCm) },
            currentHour = container.clock.now()
                .atZone(java.time.ZoneId.systemDefault())
                .hour
        )
    }.catch {
        emit(DashboardUiState(isLoading = false, hasError = true))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState()
    )

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(container) as T
    }
}
