package br.com.paivalab.controlapeso.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KnownScaleItem(
    val device: ScaleDevice,
    val lastUsedAt: Instant?
)

data class DevicesUiState(
    val isLoading: Boolean = true,
    val devices: List<KnownScaleItem> = emptyList(),
    val pendingForget: ScaleDevice? = null,
    val hasError: Boolean = false
)

class DevicesViewModel(private val container: AppContainer) : ViewModel() {
    private val pendingForget = MutableStateFlow<ScaleDevice?>(null)

    val uiState: StateFlow<DevicesUiState> =
        combine(
            container.scaleDeviceRepository.observeAll(),
            container.measurementRepository.observeAll(),
            pendingForget
        ) { devices, measurements, pending ->
            val usage = measurements
                .filter { it.deviceId != null }
                .groupBy { it.deviceId }
                .mapValues { (_, values) -> values.maxOf { it.measuredAt } }
            DevicesUiState(
                isLoading = false,
                devices = devices.map { KnownScaleItem(it, usage[it.id]) },
                pendingForget = pending
            )
            }
            .catch {
                emit(DevicesUiState(isLoading = false, hasError = true))
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                DevicesUiState()
            )

    fun setPreferred(device: ScaleDevice) {
        viewModelScope.launch {
            container.scaleDeviceRepository.setPreferred(device)
        }
    }

    fun requestForget(device: ScaleDevice) {
        pendingForget.value = device
    }

    fun dismissForget() {
        pendingForget.value = null
    }

    fun confirmForget() {
        val device = pendingForget.value ?: return
        pendingForget.value = null
        viewModelScope.launch {
            container.scaleDeviceRepository.delete(device)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DevicesViewModel(container) as T
    }
}
