package br.com.paivalab.controlapeso.ui.measurement.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeasurementDetailUiState(
    val isLoading: Boolean = true,
    val measurement: WeightMeasurement? = null,
    val profile: Profile? = null,
    val previousMeasurement: WeightMeasurement? = null,
    val deleteConfirmationVisible: Boolean = false,
    val deletedMeasurement: WeightMeasurement? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MeasurementDetailViewModel(
    private val container: AppContainer,
    private val measurementId: String
) : ViewModel() {
    private val local = MutableStateFlow(MeasurementDetailUiState())
    private val data = combine(
        container.measurementRepository.observeById(measurementId),
        container.profileRepository.observeAll()
    ) { measurement, profiles -> measurement to profiles }
        .flatMapLatest { (measurement, profiles) ->
            if (measurement == null) {
                flowOf(Triple(null, null, null))
            } else {
                container.measurementRepository.observeForProfile(measurement.profileId)
                    .map { history ->
                        val previous = history
                            .filter { it.measuredAt < measurement.measuredAt }
                            .maxByOrNull(WeightMeasurement::measuredAt)
                        Triple(
                            measurement,
                            profiles.firstOrNull { it.id == measurement.profileId },
                            previous
                        )
                    }
            }
        }

    val uiState = combine(data, local) { (measurement, profile, previous), state ->
        state.copy(
            isLoading = false,
            measurement = measurement,
            profile = profile,
            previousMeasurement = previous
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MeasurementDetailUiState()
    )

    fun requestDelete() = local.update { it.copy(deleteConfirmationVisible = true) }
    fun dismissDelete() = local.update { it.copy(deleteConfirmationVisible = false) }

    fun confirmDelete() {
        val measurement = uiState.value.measurement ?: return
        viewModelScope.launch {
            container.measurementRepository.delete(measurement)
            local.update {
                it.copy(
                    deleteConfirmationVisible = false,
                    deletedMeasurement = measurement
                )
            }
        }
    }

    fun undoDelete() {
        val deleted = local.value.deletedMeasurement ?: return
        viewModelScope.launch {
            container.measurementRepository.insert(deleted)
            local.update { it.copy(deletedMeasurement = null) }
        }
    }

    fun consumeDeleteNotice() = local.update { it.copy(deletedMeasurement = null) }

    class Factory(
        private val container: AppContainer,
        private val measurementId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MeasurementDetailViewModel(container, measurementId) as T
    }
}
