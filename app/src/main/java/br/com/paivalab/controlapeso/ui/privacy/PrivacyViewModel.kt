package br.com.paivalab.controlapeso.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectAvailability
import br.com.paivalab.controlapeso.data.privacy.DataDeletionService
import br.com.paivalab.controlapeso.worker.ReportCacheCleaner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PrivacyUiState(
    val showFirstConfirmation: Boolean = false,
    val showFinalConfirmation: Boolean = false,
    val confirmationText: String = "",
    val isDeleting: Boolean = false,
    val temporaryFilesCleared: Boolean = false,
    val error: Boolean = false,
    val healthAvailability: HealthConnectAvailability =
        HealthConnectAvailability.ANDROID_VERSION_UNSUPPORTED,
    val healthPermissionGranted: Boolean = false
)

class PrivacyViewModel(private val container: AppContainer) : ViewModel() {
    private val local = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = local.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PrivacyUiState()
    )

    init {
        refreshHealthConnect()
    }

    fun refreshHealthConnect() {
        val availability = container.healthConnectWeightWriter.availability()
        local.update {
            it.copy(
                healthAvailability = availability,
                healthPermissionGranted = false
            )
        }
        if (availability == HealthConnectAvailability.AVAILABLE) {
            viewModelScope.launch {
                local.update {
                    it.copy(
                        healthPermissionGranted =
                            container.healthConnectWeightWriter.hasWritePermission()
                    )
                }
            }
        }
    }

    fun requestDelete() = local.update {
        it.copy(showFirstConfirmation = true, error = false)
    }

    fun dismissDelete() = local.update {
        it.copy(
            showFirstConfirmation = false,
            showFinalConfirmation = false,
            confirmationText = ""
        )
    }

    fun continueDelete() = local.update {
        it.copy(showFirstConfirmation = false, showFinalConfirmation = true)
    }

    fun setConfirmationText(value: String) = local.update {
        it.copy(confirmationText = value.take(20))
    }

    fun confirmDelete() {
        if (local.value.confirmationText.trim().uppercase() != CONFIRMATION_WORD) return
        local.update { it.copy(isDeleting = true, error = false) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    DataDeletionService(container).deleteAllLocalData()
                }
                local.update {
                    it.copy(
                        showFirstConfirmation = false,
                        showFinalConfirmation = false,
                        confirmationText = "",
                        isDeleting = false,
                        error = false
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                local.update {
                    it.copy(isDeleting = false, error = true)
                }
            }
        }
    }

    fun clearTemporaryFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            ReportCacheCleaner.clean(
                directory = container.shareFileService.sharedDirectory,
                nowEpochMillis = Long.MAX_VALUE,
                maxAgeMillis = 0
            )
            local.update { it.copy(temporaryFilesCleared = true) }
        }
    }

    fun consumeTemporaryNotice() =
        local.update { it.copy(temporaryFilesCleared = false) }

    fun dismissError() = local.update { it.copy(error = false) }

    companion object {
        const val CONFIRMATION_WORD = "EXCLUIR"
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PrivacyViewModel(container) as T
    }
}
