package br.com.paivalab.controlapeso.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import br.com.paivalab.controlapeso.domain.usecase.statistics.WeightStatistics
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod

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
    val preferences: AppPreferences = AppPreferences(),
    val filters: HistoryFilters = HistoryFilters(),
    val measurements: List<WeightMeasurement> = emptyList(),
    val statistics: WeightStatistics? = null,
    val hasError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val container: AppContainer,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {
    private val filters = MutableStateFlow(HistoryFilters())

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
                flowOf(Triple<Profile?, HistoryFilters, List<WeightMeasurement>>(
                    null,
                    currentFilters,
                    emptyList()
                ))
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
                    Triple(
                        profile,
                        currentFilters.copy(
                            invalidCustomRange = invalidCustom
                        ),
                        sourceFiltered.sortedBy(WeightMeasurement::measuredAt)
                    )
                }
            }
        }

    val uiState: StateFlow<HistoryUiState> = combine(
        measurementData,
        container.preferencesRepository.preferences
    ) { (profile, currentFilters, measurements), preferences ->
        HistoryUiState(
            isLoading = false,
            profile = profile,
            preferences = preferences,
            filters = currentFilters,
            measurements = measurements,
            statistics = MeasurementStatistics.calculate(measurements)
        )
    }.catch {
        emit(HistoryUiState(isLoading = false, hasError = true))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HistoryUiState()
    )

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

    private fun bounds(filters: HistoryFilters): Pair<Instant, Instant>? {
        return HistoryDateRangeCalculator.bounds(
            filters = filters,
            today = container.clock.now().atZone(zoneId).toLocalDate(),
            zoneId = zoneId
        )
    }

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
