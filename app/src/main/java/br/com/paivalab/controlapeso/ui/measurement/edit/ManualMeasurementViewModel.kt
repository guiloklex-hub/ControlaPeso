package br.com.paivalab.controlapeso.ui.measurement.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.measurement.CreateManualMeasurement
import br.com.paivalab.controlapeso.domain.usecase.measurement.CreateMeasurementResult
import br.com.paivalab.controlapeso.domain.usecase.measurement.ManualMeasurementInput
import br.com.paivalab.controlapeso.domain.usecase.measurement.ManualMeasurementValidator
import br.com.paivalab.controlapeso.domain.usecase.measurement.ManualValidationError
import br.com.paivalab.controlapeso.domain.usecase.profile.ProfileSelectionPolicy
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManualMeasurementUiState(
    val profiles: List<Profile> = emptyList(),
    val selectedProfileId: String? = null,
    val weightText: String = "",
    val unit: WeightUnit = WeightUnit.KILOGRAM,
    val dateText: String = "",
    val timeText: String = "",
    val note: String = "",
    val errors: List<ManualValidationError> = emptyList(),
    val probableDuplicate: Boolean = false,
    val isSaving: Boolean = false,
    val savedMeasurementId: String? = null
)

class ManualMeasurementViewModel(
    private val container: AppContainer,
    private val measurementId: String?
) : ViewModel() {
    private val local = MutableStateFlow(initialState())
    private val createMeasurement = CreateManualMeasurement(
        validator = ManualMeasurementValidator(container.clock),
        measurementRepository = container.measurementRepository,
        goalRepository = container.goalRepository,
        preferencesRepository = container.preferencesRepository,
        clock = container.clock,
        idGenerator = container.idGenerator
    )

    val uiState: StateFlow<ManualMeasurementUiState> = combine(
        container.profileRepository.observeAll(),
        local
    ) { profiles, state ->
        state.copy(
            profiles = profiles,
            selectedProfileId = state.selectedProfileId
                ?: ProfileSelectionPolicy.defaultProfileId(profiles),
            unit = if (state.selectedProfileId == null) {
                profiles.firstOrNull {
                    it.id == ProfileSelectionPolicy.defaultProfileId(profiles)
                }?.preferredWeightUnit ?: state.unit
            } else {
                state.unit
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ManualMeasurementUiState()
    )

    init {
        if (measurementId != null) loadMeasurement(measurementId)
    }

    private fun initialState(): ManualMeasurementUiState {
        val localDateTime = container.clock.now().atZone(ZoneId.systemDefault())
        return ManualMeasurementUiState(
            dateText = localDateTime.toLocalDate().toString(),
            timeText = localDateTime.toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        )
    }

    fun setProfile(id: String) {
        val profile = uiState.value.profiles.firstOrNull { it.id == id }
        local.update {
            it.copy(
                selectedProfileId = id,
                unit = profile?.preferredWeightUnit ?: it.unit,
                errors = emptyList()
            )
        }
    }

    fun setWeight(value: String) = update { copy(weightText = value) }
    fun setUnit(value: WeightUnit) = update { copy(unit = value) }
    fun setDate(value: String) = update { copy(dateText = value) }
    fun setTime(value: String) = update { copy(timeText = value) }
    fun setNote(value: String) = update { copy(note = value.take(500)) }
    fun dismissDuplicate() = local.update { it.copy(probableDuplicate = false) }
    fun confirmDuplicate() = save(acceptDuplicate = true)
    fun consumeSaved() = local.update { it.copy(savedMeasurementId = null) }

    fun save(acceptDuplicate: Boolean = false) {
        val state = uiState.value
        if (state.isSaving) return
        local.update {
            it.copy(isSaving = true, errors = emptyList(), probableDuplicate = false)
        }
        viewModelScope.launch {
            when (
                val result = createMeasurement(
                    input = ManualMeasurementInput(
                        weightText = state.weightText,
                        dateText = state.dateText,
                        timeText = state.timeText,
                        unit = state.unit,
                        profileId = state.selectedProfileId.orEmpty(),
                        note = state.note
                    ),
                    existingId = measurementId,
                    acceptProbableDuplicate = acceptDuplicate
                )
            ) {
                is CreateMeasurementResult.Invalid -> local.update {
                    it.copy(isSaving = false, errors = result.errors)
                }
                is CreateMeasurementResult.ProbableDuplicate -> local.update {
                    it.copy(isSaving = false, probableDuplicate = true)
                }
                is CreateMeasurementResult.Saved -> {
                    syncToHealthConnectIfEnabled(result.measurement)
                    local.update {
                        it.copy(
                            isSaving = false,
                            savedMeasurementId = result.measurement.id
                        )
                    }
                }
            }
        }
    }

    private suspend fun syncToHealthConnectIfEnabled(
        measurement: br.com.paivalab.controlapeso.domain.model.WeightMeasurement
    ) {
        if (measurement.source == MeasurementSource.HEALTH_CONNECT) return
        val profile = container.profileRepository.findById(measurement.profileId)
        if (profile?.healthConnectEnabled == true) {
            container.healthConnectWeightWriter.write(measurement)
        }
    }

    private fun loadMeasurement(id: String) {
        viewModelScope.launch {
            val measurement = container.measurementRepository.findById(id) ?: return@launch
            val profile = container.profileRepository.findById(measurement.profileId)
            val unit = profile?.preferredWeightUnit ?: WeightUnit.KILOGRAM
            val offset = measurement.zoneOffsetSeconds
                ?.let(ZoneOffset::ofTotalSeconds)
                ?: ZoneId.systemDefault().rules.getOffset(measurement.measuredAt)
            val localDateTime = measurement.measuredAt.atOffset(offset)
            local.update {
                it.copy(
                    selectedProfileId = measurement.profileId,
                    weightText = unit.fromKilograms(measurement.weightKg).toString(),
                    unit = unit,
                    dateText = localDateTime.toLocalDate().toString(),
                    timeText = localDateTime.toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                    note = measurement.note.orEmpty()
                )
            }
        }
    }

    private fun update(transform: ManualMeasurementUiState.() -> ManualMeasurementUiState) {
        local.update { it.transform().copy(errors = emptyList()) }
    }

    class Factory(
        private val container: AppContainer,
        private val measurementId: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ManualMeasurementViewModel(container, measurementId) as T
    }
}
