package br.com.paivalab.controlapeso.ui.profiles

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.data.profile.ProfilePhotoStore
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.repository.ProfileRepository
import br.com.paivalab.controlapeso.domain.repository.MeasurementRepository
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProfileFormError {
    NAME_REQUIRED,
    NAME_TOO_LONG,
    INVALID_HEIGHT,
    INVALID_BIRTH_DATE,
    FUTURE_BIRTH_DATE,
    SAVE_FAILED
}

data class ProfileForm(
    val editingId: String? = null,
    val name: String = "",
    val avatarKey: String = "ocean",
    val heightText: String = "",
    val birthDateText: String = "",
    val unit: WeightUnit = WeightUnit.KILOGRAM,
    val existingPhotoPath: String? = null,
    val photoUri: String? = null,
    val removePhoto: Boolean = false
)

data class ProfilesUiState(
    val profiles: List<Profile> = emptyList(),
    val defaultWeightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val measurementCounts: Map<String, Int> = emptyMap(),
    val unassignedMeasurementCount: Int = 0,
    val form: ProfileForm? = null,
    val deleteCandidate: Profile? = null,
    val isSaving: Boolean = false,
    val error: ProfileFormError? = null,
    val photoPaths: Map<String, String> = emptyMap(),
    val photoRevision: Long = 0
)

class ProfilesViewModel(
    private val repository: ProfileRepository,
    private val clock: AppClock,
    private val idGenerator: IdGenerator,
    private val measurementRepository: MeasurementRepository? = null,
    private val photoStore: ProfilePhotoStore? = null,
    private val preferencesRepository: AppPreferencesRepository? = null
) : ViewModel() {
    private val editor = MutableStateFlow(ProfilesUiState())
    private val measurements = measurementRepository?.observeAll() ?: flowOf(emptyList())
    private val defaultUnit = preferencesRepository?.preferences
        ?.map { it.defaultWeightUnit }
        ?: flowOf(WeightUnit.KILOGRAM)

    val uiState: StateFlow<ProfilesUiState> = combine(
        repository.observeAll(),
        measurements,
        editor,
        defaultUnit
    ) { profiles, measurements, local, unit ->
        local.copy(
            profiles = profiles,
            defaultWeightUnit = unit,
            measurementCounts = measurements
                .filter { it.profileId != null }
                .groupingBy { requireNotNull(it.profileId) }
                .eachCount(),
            unassignedMeasurementCount = measurements.count { it.profileId == null },
            photoPaths = profiles.mapNotNull { profile ->
                photoStore?.pathFor(profile.id)?.let { profile.id to it }
            }.toMap()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfilesUiState()
    )

    fun startCreate() = editor.update {
        it.copy(
            form = ProfileForm(unit = uiState.value.defaultWeightUnit),
            error = null
        )
    }

    fun startEdit(profile: Profile) = editor.update {
        it.copy(
            form = ProfileForm(
                editingId = profile.id,
                name = profile.name,
                avatarKey = profile.avatarKey ?: "ocean",
                heightText = profile.heightCm?.toString().orEmpty(),
                birthDateText = profile.birthDate
                    ?.let(BrazilianDateFormatter::inputDigits)
                    .orEmpty(),
                unit = profile.preferredWeightUnit,
                existingPhotoPath = photoStore?.pathFor(profile.id)
            ),
            error = null
        )
    }

    fun dismissForm() = editor.update { it.copy(form = null, error = null) }
    fun setName(value: String) = updateForm { copy(name = value) }
    fun setAvatar(value: String) = updateForm { copy(avatarKey = value) }
    fun setHeight(value: String) = updateForm { copy(heightText = value) }
    fun setBirthDate(value: String) = updateForm {
        copy(birthDateText = BrazilianDateFormatter.inputDigits(value))
    }
    fun setUnit(value: WeightUnit) = updateForm { copy(unit = value) }
    fun setPhoto(uri: Uri) = updateForm {
        copy(photoUri = uri.toString(), removePhoto = false)
    }
    fun setPhotoUri(uri: String) = updateForm {
        copy(photoUri = uri, removePhoto = false)
    }
    fun removePhoto() = updateForm {
        copy(photoUri = null, removePhoto = true)
    }

    fun save() {
        val form = editor.value.form ?: return
        val name = form.name.trim()
        val height = form.heightText.trim().replace(',', '.')
            .takeIf(String::isNotEmpty)?.toDoubleOrNull()
        val birthDate = try {
            form.birthDateText.trim()
                .takeIf(String::isNotEmpty)
                ?.let(BrazilianDateFormatter::parse)
        } catch (_: DateTimeException) {
            editor.update { it.copy(error = ProfileFormError.INVALID_BIRTH_DATE) }
            return
        }
        val error = when {
            name.isEmpty() -> ProfileFormError.NAME_REQUIRED
            name.length > 80 -> ProfileFormError.NAME_TOO_LONG
            form.heightText.isNotBlank() && (height == null || height !in 80.0..250.0) ->
                ProfileFormError.INVALID_HEIGHT
            birthDate?.isAfter(clock.now().atZone(ZoneId.systemDefault()).toLocalDate()) == true ->
                ProfileFormError.FUTURE_BIRTH_DATE
            else -> null
        }
        if (error != null) {
            editor.update { it.copy(error = error) }
            return
        }

        editor.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val existing = form.editingId?.let { repository.findById(it) }
                val now = clock.now()
                val profile = Profile(
                    id = existing?.id ?: idGenerator.newId(),
                    name = name,
                    avatarKey = form.avatarKey,
                    heightCm = height,
                    birthDate = birthDate,
                    preferredWeightUnit = form.unit,
                    healthConnectEnabled = existing?.healthConnectEnabled ?: false,
                    isActive = existing?.isActive ?: false,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
                val persistProfile: suspend () -> Unit = {
                    if (existing == null) {
                        repository.insert(profile)
                        if (uiState.value.profiles.none(Profile::isActive)) {
                            repository.setActive(profile.id)
                        }
                    } else {
                        repository.update(profile)
                    }
                }
                if (photoStore != null) {
                    photoStore.withPhotoChange(
                        profileId = profile.id,
                        sourceUri = form.photoUri,
                        removePhoto = form.removePhoto,
                        block = persistProfile
                    )
                } else {
                    persistProfile()
                }
                editor.update {
                    it.copy(
                        form = null,
                        isSaving = false,
                        photoRevision = it.photoRevision + 1
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                editor.update {
                    it.copy(isSaving = false, error = ProfileFormError.SAVE_FAILED)
                }
            }
        }
    }

    fun setActive(profile: Profile) {
        viewModelScope.launch { repository.setActive(profile.id) }
    }

    fun requestDelete(profile: Profile) =
        editor.update { it.copy(deleteCandidate = profile) }

    fun dismissDelete() = editor.update { it.copy(deleteCandidate = null) }

    fun confirmDelete() {
        val profile = editor.value.deleteCandidate ?: return
        viewModelScope.launch {
            repository.delete(profile)
            photoStore?.delete(profile.id)
            val remaining = uiState.value.profiles.filterNot { it.id == profile.id }
            if (profile.isActive) remaining.firstOrNull()?.let { repository.setActive(it.id) }
            editor.update {
                it.copy(
                    deleteCandidate = null,
                    photoRevision = it.photoRevision + 1
                )
            }
        }
    }

    private fun updateForm(transform: ProfileForm.() -> ProfileForm) {
        editor.update { state ->
            state.copy(form = state.form?.transform(), error = null)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfilesViewModel(
                repository = container.profileRepository,
                clock = container.clock,
                idGenerator = container.idGenerator,
                measurementRepository = container.measurementRepository,
                photoStore = container.profilePhotoStore,
                preferencesRepository = container.preferencesRepository
            ) as T
    }
}
