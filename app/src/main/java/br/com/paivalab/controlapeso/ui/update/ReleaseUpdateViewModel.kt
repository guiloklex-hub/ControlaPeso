package br.com.paivalab.controlapeso.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.BuildConfig
import br.com.paivalab.controlapeso.data.update.ReleaseCheckResult
import br.com.paivalab.controlapeso.data.update.ReleaseInfo
import br.com.paivalab.controlapeso.data.update.UpdateDownloadFailure
import br.com.paivalab.controlapeso.data.update.UpdateDownloadResult
import java.io.File
import java.time.Instant
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UpdateInstallFeedback {
    UNKNOWN_SOURCES,
    INSTALL_STARTED,
    INSTALL_FAILED
}

enum class ReleaseCheckStatus {
    NOT_CHECKED,
    CHECKING,
    UP_TO_DATE,
    UPDATE_AVAILABLE,
    FAILED
}

data class ReleaseUpdateUiState(
    val release: ReleaseInfo? = null,
    val isDialogVisible: Boolean = true,
    val checkStatus: ReleaseCheckStatus = ReleaseCheckStatus.NOT_CHECKED,
    val lastCheckAt: Instant? = null,
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadFailure: UpdateDownloadFailure? = null,
    val installFeedback: UpdateInstallFeedback? = null
)

sealed interface ReleaseUpdateEvent {
    data class StartPackageInstallation(val apkFile: File) : ReleaseUpdateEvent
}

/** Observes the application-process check and keeps dismissal scoped to that process only. */
class ReleaseUpdateViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ReleaseUpdateUiState())
    val uiState: StateFlow<ReleaseUpdateUiState> = mutableUiState.asStateFlow()
    private val mutableEvents = MutableSharedFlow<ReleaseUpdateEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ReleaseUpdateEvent> = mutableEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            container.releaseUpdateCoordinator.result
                .filterNotNull()
                .collect(::consumeCheckResult)
        }
    }

    fun dismissForSession() {
        if (mutableUiState.value.isDownloading) return
        mutableUiState.update {
            it.copy(isDialogVisible = false, downloadFailure = null, installFeedback = null)
        }
    }

    fun showReleaseDetails() {
        if (mutableUiState.value.release == null) return
        mutableUiState.update { it.copy(isDialogVisible = true, installFeedback = null) }
    }

    fun checkNow() {
        if (mutableUiState.value.isChecking || mutableUiState.value.isDownloading) return
        mutableUiState.update {
            it.copy(isChecking = true, checkStatus = ReleaseCheckStatus.CHECKING)
        }
        viewModelScope.launch {
            val result = try {
                container.releaseUpdateRepository.checkForUpdate(BuildConfig.VERSION_NAME)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                ReleaseCheckResult.Failure(failure)
            }
            val status = when (result) {
                ReleaseCheckResult.NoUpdate -> ReleaseCheckStatus.UP_TO_DATE
                is ReleaseCheckResult.UpdateAvailable -> ReleaseCheckStatus.UPDATE_AVAILABLE
                is ReleaseCheckResult.Failure -> ReleaseCheckStatus.FAILED
            }
            mutableUiState.update {
                it.copy(
                    release = (result as? ReleaseCheckResult.UpdateAvailable)?.release,
                    isDialogVisible = result is ReleaseCheckResult.UpdateAvailable,
                    isChecking = false,
                    checkStatus = status,
                    lastCheckAt = container.clock.now()
                )
            }
        }
    }

    fun downloadAndInstall() {
        val release = mutableUiState.value.release ?: return
        if (mutableUiState.value.isDownloading) return
        mutableUiState.update {
            it.copy(isDownloading = true, downloadFailure = null, installFeedback = null)
        }
        viewModelScope.launch {
            val result = try {
                container.releaseUpdateRepository.downloadUpdate(release)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                UpdateDownloadResult.Failure(UpdateDownloadFailure.NETWORK)
            } catch (_: SecurityException) {
                UpdateDownloadResult.Failure(UpdateDownloadFailure.STORAGE)
            } catch (_: Throwable) {
                UpdateDownloadResult.Failure(UpdateDownloadFailure.APK_INVALID)
            }
            when (result) {
                is UpdateDownloadResult.Success -> {
                    mutableUiState.update { it.copy(isDownloading = false) }
                    mutableEvents.emit(ReleaseUpdateEvent.StartPackageInstallation(result.apkFile))
                }
                is UpdateDownloadResult.Failure -> mutableUiState.update {
                    it.copy(isDownloading = false, downloadFailure = result.reason)
                }
            }
        }
    }

    fun onUnknownSourcesRequired() = mutableUiState.update {
        it.copy(isDownloading = false, installFeedback = UpdateInstallFeedback.UNKNOWN_SOURCES)
    }

    fun onInstallationStarted() = mutableUiState.update {
        it.copy(
            release = null,
            isDialogVisible = false,
            downloadFailure = null,
            installFeedback = null
        )
    }

    fun onInstallationFinished(success: Boolean) = mutableUiState.update {
        if (success) {
            it.copy(
                release = null,
                isDialogVisible = false,
                downloadFailure = null,
                installFeedback = null
            )
        } else {
            it.copy(isDialogVisible = true, installFeedback = UpdateInstallFeedback.INSTALL_FAILED)
        }
    }

    fun dismissInstallFeedback() = mutableUiState.update { it.copy(installFeedback = null) }

    private fun consumeCheckResult(result: ReleaseCheckResult) {
        when (result) {
            is ReleaseCheckResult.UpdateAvailable -> mutableUiState.update {
                it.copy(
                    release = result.release,
                    isDialogVisible = true,
                    checkStatus = ReleaseCheckStatus.UPDATE_AVAILABLE,
                    lastCheckAt = container.clock.now()
                )
            }
            ReleaseCheckResult.NoUpdate -> mutableUiState.update {
                it.copy(
                    checkStatus = ReleaseCheckStatus.UP_TO_DATE,
                    lastCheckAt = container.clock.now()
                )
            }
            // A failed/offline check deliberately stays silent and never blocks local use,
            // but About can explain the last status when the person asks.
            is ReleaseCheckResult.Failure -> mutableUiState.update {
                it.copy(
                    checkStatus = ReleaseCheckStatus.FAILED,
                    lastCheckAt = container.clock.now()
                )
            }
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReleaseUpdateViewModel(container) as T
    }
}
