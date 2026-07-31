package br.com.paivalab.controlapeso.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.data.export.SharedReportFile
import br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LocalBackupMessage {
    CREATED,
    SHARED
}

enum class LocalBackupError {
    CREATE_FAILED,
    SHARE_UNAVAILABLE
}

data class LocalBackupUiState(
    val frequency: LocalBackupFrequency = LocalBackupFrequency.OFF,
    val latestAt: Instant? = null,
    val latestAvailable: Boolean = false,
    val isWorking: Boolean = false,
    val message: LocalBackupMessage? = null,
    val error: LocalBackupError? = null,
    val sharedFile: SharedReportFile? = null
)

class LocalBackupViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val local = MutableStateFlow(LocalBackupUiState())

    val uiState: StateFlow<LocalBackupUiState> = combine(
        container.preferencesRepository.preferences,
        local
    ) { preferences, state ->
        state.copy(
            frequency = preferences.localBackupFrequency,
            latestAt = preferences.lastLocalBackupAt,
            latestAvailable = container.localBackupService.latestFile() != null
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LocalBackupUiState()
    )

    fun setFrequency(value: LocalBackupFrequency) {
        viewModelScope.launch {
            container.preferencesRepository.setLocalBackupFrequency(value)
        }
    }

    fun createNow() {
        if (uiState.value.isWorking) return
        local.update { it.copy(isWorking = true, message = null, error = null) }
        viewModelScope.launch {
            try {
                container.localBackupService.createLatest()
                local.update {
                    it.copy(
                        isWorking = false,
                        message = LocalBackupMessage.CREATED,
                        error = null
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                local.update {
                    it.copy(isWorking = false, error = LocalBackupError.CREATE_FAILED)
                }
            }
        }
    }

    fun shareLatest() {
        if (uiState.value.isWorking) return
        local.update { it.copy(isWorking = true, message = null, error = null) }
        viewModelScope.launch {
            try {
                val file = container.localBackupService.shareLatest()
                if (file == null) {
                    local.update {
                        it.copy(
                            isWorking = false,
                            error = LocalBackupError.SHARE_UNAVAILABLE
                        )
                    }
                } else {
                    local.update {
                        it.copy(
                            isWorking = false,
                            message = LocalBackupMessage.SHARED,
                            error = null,
                            sharedFile = file
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                local.update {
                    it.copy(
                        isWorking = false,
                        error = LocalBackupError.SHARE_UNAVAILABLE
                    )
                }
            }
        }
    }

    fun consumeSharedFile() = local.update { it.copy(sharedFile = null) }
    fun dismissFeedback() = local.update { it.copy(message = null, error = null) }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LocalBackupViewModel(container) as T
    }
}
