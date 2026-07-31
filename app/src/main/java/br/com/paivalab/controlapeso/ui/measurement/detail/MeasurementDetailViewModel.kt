package br.com.paivalab.controlapeso.ui.measurement.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import kotlinx.coroutines.CancellationException
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
    val profilePhotoPath: String? = null,
    val profileAvatarKey: String? = null,
    val unit: WeightUnit = WeightUnit.KILOGRAM,
    val profiles: List<Profile> = emptyList(),
    val previousMeasurement: WeightMeasurement? = null,
    val deleteConfirmationVisible: Boolean = false,
    val deletedMeasurement: WeightMeasurement? = null,
    val assignProfileDialogVisible: Boolean = false,
    val assignmentError: Boolean = false
)

private data class MeasurementDetailData(
    val measurement: WeightMeasurement?,
    val profile: Profile?,
    val profilePhotoPath: String?,
    val profileAvatarKey: String?,
    val profiles: List<Profile>,
    val previousMeasurement: WeightMeasurement?
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
                flowOf(
                    MeasurementDetailData(
                        measurement = null,
                        profile = null,
                        profilePhotoPath = null,
                        profileAvatarKey = null,
                        profiles = profiles,
                        previousMeasurement = null
                    )
                )
            } else {
                val history = measurement.profileId?.let {
                    container.measurementRepository.observeForProfile(it)
                } ?: container.measurementRepository.observeUnassigned()
                history
                    .map { history ->
                        val profile = profiles.firstOrNull { it.id == measurement.profileId }
                        val previous = history
                            .filter { it.measuredAt < measurement.measuredAt }
                            .maxByOrNull(WeightMeasurement::measuredAt)
                        MeasurementDetailData(
                            measurement = measurement,
                            profile = profile,
                            profilePhotoPath = profile?.let {
                                container.profilePhotoStore.pathFor(it.id)
                            },
                            profileAvatarKey = profile?.avatarKey,
                            profiles = profiles,
                            previousMeasurement = previous
                        )
                    }
            }
        }

    val uiState = combine(
        data,
        container.preferencesRepository.preferences,
        local
    ) { detail, preferences, state ->
        state.copy(
            isLoading = false,
            measurement = detail.measurement,
            profile = detail.profile,
            profilePhotoPath = detail.profilePhotoPath,
            profileAvatarKey = detail.profileAvatarKey,
            unit = preferences.defaultWeightUnit,
            profiles = detail.profiles,
            previousMeasurement = detail.previousMeasurement
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

    fun requestAssignProfile() {
        if (uiState.value.measurement?.profileId == null &&
            uiState.value.profiles.isNotEmpty()
        ) {
            local.update { it.copy(assignProfileDialogVisible = true, assignmentError = false) }
        }
    }

    fun dismissAssignProfile() = local.update {
        it.copy(assignProfileDialogVisible = false, assignmentError = false)
    }

    fun assignProfile(profileId: String) {
        val measurement = uiState.value.measurement ?: return
        if (measurement.profileId != null) return
        viewModelScope.launch {
            try {
                val assignedCount = container.measurementRepository.assignToProfile(
                    measurementIds = listOf(measurement.id),
                    profileId = profileId,
                    updatedAt = container.clock.now()
                )
                check(assignedCount == 1) {
                    "A medição já foi atribuída ou não está mais disponível."
                }
                local.update {
                    it.copy(assignProfileDialogVisible = false, assignmentError = false)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                local.update { it.copy(assignmentError = true) }
            }
        }
    }

    class Factory(
        private val container: AppContainer,
        private val measurementId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MeasurementDetailViewModel(container, measurementId) as T
    }
}
