package br.com.paivalab.controlapeso.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import br.com.paivalab.controlapeso.domain.usecase.statistics.WeightStatistics
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryRange {
    DAYS_7,
    DAYS_30,
    MONTHS_3,
    MONTHS_6,
    YEAR_1,
    ALL,
    CUSTOM
}

data class HistoryFilters(
    val range: HistoryRange = HistoryRange.DAYS_30,
    val customStartText: String = "",
    val customEndText: String = "",
    val sources: Set<MeasurementSource> = emptySet(),
    val invalidCustomRange: Boolean = false
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val profiles: List<Profile> = emptyList(),
    val profilePhotoPaths: Map<String, String> = emptyMap(),
    val preferences: AppPreferences = AppPreferences(),
    val filters: HistoryFilters = HistoryFilters(),
    val measurements: List<WeightMeasurement> = emptyList(),
    val unassignedMeasurements: List<WeightMeasurement> = emptyList(),
    val selectedUnassignedIds: Set<String> = emptySet(),
    val assignmentProfileId: String? = null,
    val isAssigning: Boolean = false,
    val assignmentError: Boolean = false,
    val statistics: WeightStatistics? = null,
    val hasError: Boolean = false
)

private data class ActiveHistoryData(
    val profile: Profile?,
    val filters: HistoryFilters,
    val measurements: List<WeightMeasurement>
)

private data class HistoryAssignmentState(
    val selectedIds: Set<String> = emptySet(),
    val profileId: String? = null,
    val isAssigning: Boolean = false,
    val hasError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val container: AppContainer,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {
    private val filters = MutableStateFlow(HistoryFilters())
    private val assignment = MutableStateFlow(HistoryAssignmentState())
    private val refreshTrigger = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            val period = container.preferencesRepository.preferences.first()
                .defaultHistoryPeriod
            filters.update {
                if (it == HistoryFilters()) {
                    it.copy(range = period.toHistoryRange())
                } else {
                    it
                }
            }
        }
    }

    private val measurementData = combine(
        container.profileRepository.observeActive(),
        filters
    ) { profile, currentFilters -> profile to currentFilters }
        .flatMapLatest { (profile, currentFilters) ->
            if (profile == null) {
                flowOf(
                    ActiveHistoryData(
                        profile = null,
                        filters = currentFilters,
                        measurements = emptyList()
                    )
                )
            } else {
                val bounds = bounds(currentFilters)
                val invalidCustom =
                    currentFilters.range == HistoryRange.CUSTOM && bounds == null
                val flow = when {
                    invalidCustom -> flowOf(emptyList())
                    bounds == null ->
                        container.measurementRepository.observeForProfile(profile.id)
                    else -> container.measurementRepository.observeInRange(
                        profile.id,
                        bounds.first,
                        bounds.second
                    )
                }
                flow.map { measurements ->
                    val sourceFiltered = if (currentFilters.sources.isEmpty()) {
                        measurements
                    } else {
                        measurements.filter { it.source in currentFilters.sources }
                    }
                    ActiveHistoryData(
                        profile = profile,
                        filters = currentFilters.copy(invalidCustomRange = invalidCustom),
                        measurements = sourceFiltered.sortedBy(WeightMeasurement::measuredAt)
                    )
                }
            }
        }

    private val unassignedData = combine(
        container.measurementRepository.observeUnassigned(),
        filters
    ) { measurements, currentFilters ->
        val bounds = bounds(currentFilters)
        val invalidCustom = currentFilters.range == HistoryRange.CUSTOM && bounds == null
        if (invalidCustom) {
            emptyList()
        } else {
            measurements
                .filter { measurement ->
                    val inRange = bounds == null || (
                        measurement.measuredAt >= bounds.first &&
                            measurement.measuredAt < bounds.second
                        )
                    val sourceMatches = currentFilters.sources.isEmpty() ||
                        measurement.source in currentFilters.sources
                    inRange && sourceMatches
                }
                .sortedWith(
                    compareByDescending<WeightMeasurement> { it.measuredAt }
                        .thenByDescending { it.id }
                )
        }
    }

    val uiState: StateFlow<HistoryUiState> = refreshTrigger.flatMapLatest {
        combine(
            measurementData,
            unassignedData,
            container.profileRepository.observeAll(),
            container.preferencesRepository.preferences,
            assignment
        ) { active, unassigned, profiles, preferences, local ->
            val availableIds = unassigned.map { it.id }.toSet()
            HistoryUiState(
                isLoading = false,
                profile = active.profile,
                profiles = profiles,
                profilePhotoPaths = profiles.mapNotNull { profile ->
                    container.profilePhotoStore.pathFor(profile.id)
                        ?.let { profile.id to it }
                }.toMap(),
                preferences = preferences,
                filters = active.filters,
                measurements = active.measurements,
                unassignedMeasurements = unassigned,
                selectedUnassignedIds = local.selectedIds.filter { it in availableIds }.toSet(),
                assignmentProfileId = local.profileId ?: active.profile?.id,
                isAssigning = local.isAssigning,
                assignmentError = local.hasError,
                statistics = MeasurementStatistics.calculate(active.measurements)
            )
        }.catch {
            emit(HistoryUiState(isLoading = false, hasError = true))
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HistoryUiState()
    )

    fun retry() {
        refreshTrigger.update { it + 1 }
    }

    fun setRange(value: HistoryRange) =
        filters.update { it.copy(range = value, invalidCustomRange = false) }

    fun setCustomStart(value: String) =
        filters.update {
            it.copy(
                customStartText = BrazilianDateFormatter.inputDigits(value),
                invalidCustomRange = false
            )
        }

    fun setCustomEnd(value: String) =
        filters.update {
            it.copy(
                customEndText = BrazilianDateFormatter.inputDigits(value),
                invalidCustomRange = false
            )
        }

    fun toggleSource(source: MeasurementSource) = filters.update { current ->
        current.copy(
            sources = if (source in current.sources) {
                current.sources - source
            } else {
                current.sources + source
            }
        )
    }

    fun resetFilters() {
        viewModelScope.launch {
            val defaultRange = container.preferencesRepository.preferences.first()
                .defaultHistoryPeriod
                .toHistoryRange()
            filters.value = HistoryFilters(range = defaultRange)
        }
    }

    fun toggleUnassignedSelection(measurementId: String) = assignment.update { current ->
        current.copy(
            selectedIds = if (measurementId in current.selectedIds) {
                current.selectedIds - measurementId
            } else {
                current.selectedIds + measurementId
            },
            hasError = false
        )
    }

    fun setAssignmentProfile(profileId: String) = assignment.update {
        it.copy(profileId = profileId, hasError = false)
    }

    fun assignSelectedToProfile() {
        val state = uiState.value
        val profileId = state.assignmentProfileId ?: return
        val selectedIds = state.selectedUnassignedIds
        if (selectedIds.isEmpty() || state.isAssigning) return
        assignment.update { it.copy(isAssigning = true, hasError = false) }
        viewModelScope.launch {
            try {
                val assignedCount = container.measurementRepository.assignToProfile(
                    measurementIds = selectedIds.toList(),
                    profileId = profileId,
                    updatedAt = container.clock.now()
                )
                check(assignedCount == selectedIds.size) {
                    "Nem todas as medições selecionadas continuam sem perfil."
                }
                assignment.update {
                    it.copy(
                        selectedIds = it.selectedIds - selectedIds,
                        isAssigning = false,
                        hasError = false
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                assignment.update { it.copy(isAssigning = false, hasError = true) }
            }
        }
    }

    fun dismissAssignmentError() = assignment.update { it.copy(hasError = false) }

    private fun bounds(filters: HistoryFilters): Pair<Instant, Instant>? =
        HistoryDateRangeCalculator.bounds(
            filters = filters,
            today = container.clock.now().atZone(zoneId).toLocalDate(),
            zoneId = zoneId
        )

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HistoryViewModel(container) as T
    }
}

private fun HistoryPeriod.toHistoryRange(): HistoryRange = when (this) {
    HistoryPeriod.DAYS_7 -> HistoryRange.DAYS_7
    HistoryPeriod.DAYS_30 -> HistoryRange.DAYS_30
    HistoryPeriod.MONTHS_3 -> HistoryRange.MONTHS_3
    HistoryPeriod.MONTHS_6 -> HistoryRange.MONTHS_6
    HistoryPeriod.YEAR_1 -> HistoryRange.YEAR_1
    HistoryPeriod.ALL -> HistoryRange.ALL
}
