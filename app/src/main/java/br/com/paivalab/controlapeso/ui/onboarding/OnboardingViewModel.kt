package br.com.paivalab.controlapeso.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.repository.ProfileRepository
import java.time.LocalDate
import java.time.DateTimeException
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingError {
    NAME_TOO_LONG,
    INVALID_HEIGHT,
    INVALID_TARGET,
    INVALID_BIRTH_DATE,
    FUTURE_BIRTH_DATE,
    SAVE_FAILED
}

data class OnboardingUiState(
    val step: Int = 0,
    val profileName: String = "",
    val heightText: String = "",
    val birthDateText: String = "",
    val unit: WeightUnit = WeightUnit.KILOGRAM,
    val targetWeightText: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isSaving: Boolean = false,
    val error: OnboardingError? = null
) {
    val totalSteps: Int = 8
}

class OnboardingViewModel(
    private val profileRepository: ProfileRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun next() = _uiState.update {
        it.copy(step = (it.step + 1).coerceAtMost(it.totalSteps - 1), error = null)
    }

    fun previous() = _uiState.update {
        it.copy(step = (it.step - 1).coerceAtLeast(0), error = null)
    }

    fun setName(value: String) =
        _uiState.update { it.copy(profileName = value, error = null) }
    fun setHeight(value: String) =
        _uiState.update { it.copy(heightText = value, error = null) }
    fun setBirthDate(value: String) =
        _uiState.update {
            it.copy(
                birthDateText = BrazilianDateFormatter.inputDigits(value),
                error = null
            )
        }
    fun setUnit(value: WeightUnit) =
        _uiState.update { it.copy(unit = value, error = null) }
    fun setTargetWeight(value: String) =
        _uiState.update { it.copy(targetWeightText = value, error = null) }
    fun setTheme(value: ThemeMode) =
        _uiState.update { it.copy(themeMode = value, error = null) }

    fun skipOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun finish() {
        val current = _uiState.value
        if (current.isSaving) return
        val normalizedName = current.profileName.trim()
        val height = current.heightText.parseLocalizedDoubleOrNull()
        val birthDate = try {
            current.birthDateText.trim()
                .takeIf(String::isNotEmpty)
                ?.let(BrazilianDateFormatter::parse)
        } catch (_: DateTimeException) {
            _uiState.update { it.copy(error = OnboardingError.INVALID_BIRTH_DATE) }
            return
        }
        val targetInSelectedUnit =
            current.targetWeightText.parseLocalizedDoubleOrNull()
        val targetKg = targetInSelectedUnit?.let(current.unit::toKilograms)
        val today = clock.now().atZone(ZoneId.systemDefault()).toLocalDate()
        val validationError = when {
            normalizedName.length > 80 -> OnboardingError.NAME_TOO_LONG
            current.heightText.isNotBlank() && (height == null || height !in 80.0..250.0) ->
                OnboardingError.INVALID_HEIGHT
            current.targetWeightText.isNotBlank() &&
                (targetKg == null || targetKg !in 2.0..500.0) ->
                OnboardingError.INVALID_TARGET
            birthDate?.isAfter(today) == true -> OnboardingError.FUTURE_BIRTH_DATE
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            runCatching {
                if (normalizedName.isNotEmpty()) {
                    val now = clock.now()
                    profileRepository.insert(
                        Profile(
                            id = idGenerator.newId(),
                            name = normalizedName.take(80),
                            avatarKey = "ocean",
                            heightCm = height,
                            birthDate = birthDate,
                            preferredWeightUnit = current.unit,
                            healthConnectEnabled = false,
                            isActive = true,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                    preferencesRepository.setPendingGoalTargetKg(targetKg)
                }
                preferencesRepository.setDefaultWeightUnit(current.unit)
                preferencesRepository.setThemeMode(current.themeMode)
                preferencesRepository.setOnboardingCompleted(true)
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = OnboardingError.SAVE_FAILED
                    )
                }
            }
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OnboardingViewModel(
                profileRepository = container.profileRepository,
                preferencesRepository = container.preferencesRepository,
                clock = container.clock,
                idGenerator = container.idGenerator
            ) as T
    }
}

private fun String.parseLocalizedDoubleOrNull(): Double? =
    trim().takeIf(String::isNotEmpty)?.replace(',', '.')?.toDoubleOrNull()
