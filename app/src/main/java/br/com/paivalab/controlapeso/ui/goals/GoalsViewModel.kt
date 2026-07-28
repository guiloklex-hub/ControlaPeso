package br.com.paivalab.controlapeso.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.repository.GoalRepository
import br.com.paivalab.controlapeso.domain.usecase.goals.CalculateGoalProgress
import br.com.paivalab.controlapeso.domain.usecase.goals.GoalProgress
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GoalFormError {
    INVALID_START,
    INVALID_TARGET,
    INVALID_DATE,
    SAVE_FAILED
}

data class GoalForm(
    val editingId: String? = null,
    val startWeightText: String = "",
    val targetWeightText: String = "",
    val targetDateText: String = ""
)

data class GoalItem(
    val goal: WeightGoal,
    val progress: GoalProgress?
)

data class GoalsUiState(
    val profile: Profile? = null,
    val goals: List<GoalItem> = emptyList(),
    val latestWeightKg: Double? = null,
    val form: GoalForm? = null,
    val deleteCandidate: WeightGoal? = null,
    val error: GoalFormError? = null,
    val isSaving: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModel(
    private val repository: GoalRepository,
    private val container: AppContainer,
    private val clock: AppClock,
    private val idGenerator: IdGenerator,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {
    private val local = MutableStateFlow(GoalsUiState())
    private val data = container.profileRepository.observeActive().flatMapLatest { profile ->
        if (profile == null) {
            flowOf(Triple(null, emptyList(), emptyList()))
        } else {
            combine(
                repository.observeForProfile(profile.id),
                container.measurementRepository.observeForProfile(profile.id)
            ) { goals, measurements -> Triple(profile, goals, measurements) }
        }
    }

    val uiState = combine(data, local) { (profile, goals, measurements), state ->
        val latest = measurements.maxByOrNull(WeightMeasurement::measuredAt)?.weightKg
        state.copy(
            profile = profile,
            latestWeightKg = latest,
            goals = goals.map { goal ->
                GoalItem(
                    goal = goal,
                    progress = latest?.let { CalculateGoalProgress(goal, it) }
                )
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        GoalsUiState()
    )

    fun startCreate() {
        val latest = uiState.value.latestWeightKg
        local.update {
            it.copy(
                form = GoalForm(startWeightText = latest?.toString().orEmpty()),
                error = null
            )
        }
    }

    fun startEdit(goal: WeightGoal) = local.update {
        it.copy(
            form = GoalForm(
                editingId = goal.id,
                startWeightText = goal.startWeightKg.toString(),
                targetWeightText = goal.targetWeightKg.toString(),
                targetDateText = goal.targetDate?.toString().orEmpty()
            ),
            error = null
        )
    }

    fun dismissForm() = local.update { it.copy(form = null, error = null) }
    fun setStartWeight(value: String) = updateForm { copy(startWeightText = value) }
    fun setTargetWeight(value: String) = updateForm { copy(targetWeightText = value) }
    fun setTargetDate(value: String) = updateForm { copy(targetDateText = value) }

    fun save() {
        val state = uiState.value
        val profile = state.profile ?: return
        val form = state.form ?: return
        val unit = profile.preferredWeightUnit
        val start = form.startWeightText.localizedDouble()?.let(unit::toKilograms)
        val target = form.targetWeightText.localizedDouble()?.let(unit::toKilograms)
        val targetDate = try {
            form.targetDateText.trim().takeIf(String::isNotEmpty)?.let(LocalDate::parse)
        } catch (_: DateTimeException) {
            local.update { it.copy(error = GoalFormError.INVALID_DATE) }
            return
        }
        val error = when {
            start == null || start !in 2.0..500.0 -> GoalFormError.INVALID_START
            target == null || target !in 2.0..500.0 -> GoalFormError.INVALID_TARGET
            else -> null
        }
        if (error != null) {
            local.update { it.copy(error = error) }
            return
        }
        val validStart = requireNotNull(start)
        val validTarget = requireNotNull(target)
        local.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val existing = state.goals.firstOrNull {
                    it.goal.id == form.editingId
                }?.goal
                val now = clock.now()
                val goal = WeightGoal(
                    id = existing?.id ?: idGenerator.newId(),
                    profileId = profile.id,
                    startWeightKg = validStart,
                    targetWeightKg = validTarget,
                    startDate = existing?.startDate
                        ?: now.atZone(zoneId).toLocalDate(),
                    targetDate = targetDate,
                    status = existing?.status ?: GoalStatus.ACTIVE,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
                if (existing == null) repository.insert(goal) else repository.update(goal)
            }.onSuccess {
                local.update { it.copy(form = null, isSaving = false) }
            }.onFailure {
                local.update {
                    it.copy(isSaving = false, error = GoalFormError.SAVE_FAILED)
                }
            }
        }
    }

    fun setStatus(goal: WeightGoal, status: GoalStatus) {
        viewModelScope.launch {
            repository.update(goal.copy(status = status, updatedAt = clock.now()))
        }
    }

    fun requestDelete(goal: WeightGoal) =
        local.update { it.copy(deleteCandidate = goal) }
    fun dismissDelete() = local.update { it.copy(deleteCandidate = null) }
    fun confirmDelete() {
        val goal = local.value.deleteCandidate ?: return
        viewModelScope.launch {
            repository.delete(goal)
            local.update { it.copy(deleteCandidate = null) }
        }
    }

    private fun updateForm(transform: GoalForm.() -> GoalForm) {
        local.update { it.copy(form = it.form?.transform(), error = null) }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GoalsViewModel(
                repository = container.goalRepository,
                container = container,
                clock = container.clock,
                idGenerator = container.idGenerator
            ) as T
    }
}

private fun String.localizedDouble(): Double? =
    trim().replace(',', '.').toDoubleOrNull()?.takeIf(Double::isFinite)
