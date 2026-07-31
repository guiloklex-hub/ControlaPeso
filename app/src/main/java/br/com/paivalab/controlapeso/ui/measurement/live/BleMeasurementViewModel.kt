package br.com.paivalab.controlapeso.ui.measurement.live

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.bluetooth.BlePermissionHelper
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleScanError
import br.com.paivalab.controlapeso.bluetooth.BleScanPhase
import br.com.paivalab.controlapeso.bluetooth.BleScanState
import br.com.paivalab.controlapeso.bluetooth.BleScanStopReason
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BleWeightReading
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import br.com.paivalab.controlapeso.bluetooth.StabilityProgress
import br.com.paivalab.controlapeso.bluetooth.StableMeasurementDetector
import br.com.paivalab.controlapeso.bluetooth.StableWeightEvent
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.measurement.SaveBleMeasurement
import br.com.paivalab.controlapeso.domain.usecase.measurement.SaveBleResult
import br.com.paivalab.controlapeso.domain.usecase.profile.ProfileSelectionPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LiveMeasurementStatus {
    READY,
    BLUETOOTH_OFF,
    PERMISSION_REQUIRED,
    SEARCHING,
    WAITING_FOR_WEIGHT,
    RECEIVING,
    STABLE,
    SAVING,
    SAVED,
    ERROR
}

data class BleMeasurementUiState(
    val profiles: List<Profile> = emptyList(),
    val profilePhotoPaths: Map<String, String> = emptyMap(),
    val selectedProfileId: String? = null,
    val profileSelectionExplicit: Boolean = false,
    val preferences: AppPreferences = AppPreferences(),
    val bluetoothSupport: BleSupportStatus = BleSupportStatus.UNKNOWN,
    val bluetoothPower: BluetoothPowerStatus = BluetoothPowerStatus.UNKNOWN,
    val permissionStatus: BlePermissionStatus = BlePermissionStatus.REQUIRED,
    val status: LiveMeasurementStatus = LiveMeasurementStatus.READY,
    val isScanning: Boolean = false,
    val secondsRemaining: Int = 0,
    val scanPhase: BleScanPhase? = null,
    val latestReading: BleWeightReading? = null,
    val stability: StabilityProgress? = null,
    val stableEvent: StableWeightEvent? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    val savedMeasurement: WeightMeasurement? = null,
    val probableDuplicate: Boolean = false,
    val error: BleScanError? = null,
    val saveError: SaveBleResult? = null,
    val deleteConfirmationVisible: Boolean = false,
    val deletedMeasurement: WeightMeasurement? = null
)

class BleMeasurementViewModel(
    private val container: AppContainer,
    private val sourceOverride: br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource? = null,
    private val measurementSource: MeasurementSource = MeasurementSource.BLE
) : ViewModel() {
    private val source = sourceOverride ?: container.bleMeasurementSource
    private val detector = StableMeasurementDetector()
    private val local = MutableStateFlow(BleMeasurementUiState())
    private val saveBleMeasurement = SaveBleMeasurement(
        measurementRepository = container.measurementRepository,
        deviceRepository = container.scaleDeviceRepository,
        goalRepository = container.goalRepository,
        preferencesRepository = container.preferencesRepository,
        clock = container.clock,
        idGenerator = container.idGenerator
    )
    private var receiverRegistered = false
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            when (
                intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
            ) {
                BluetoothAdapter.STATE_OFF,
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    source.stop(BleScanStopReason.BLUETOOTH_OFF)
                    local.update {
                        it.copy(
                            bluetoothPower = BluetoothPowerStatus.OFF,
                            isScanning = false,
                            status = LiveMeasurementStatus.BLUETOOTH_OFF
                        )
                    }
                }
                BluetoothAdapter.STATE_ON -> refreshEnvironment()
            }
        }
    }

    val uiState: StateFlow<BleMeasurementUiState> = combine(
        container.profileRepository.observeAll(),
        container.preferencesRepository.preferences,
        local
    ) { profiles, preferences, state ->
        val selectedId = if (state.profileSelectionExplicit) {
            state.selectedProfileId
        } else {
            state.selectedProfileId ?: ProfileSelectionPolicy.defaultProfileId(profiles)
        }
        state.copy(
            profiles = profiles,
            profilePhotoPaths = profiles.mapNotNull { profile ->
                container.profilePhotoStore.pathFor(profile.id)
                    ?.let { profile.id to it }
            }.toMap(),
            selectedProfileId = selectedId,
            preferences = preferences
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        BleMeasurementUiState()
    )

    init {
        if (measurementSource != MeasurementSource.DEMO) {
            registerBluetoothStateReceiver()
        }
        refreshEnvironment()
        viewModelScope.launch {
            source.scanState.collect(::onScanState)
        }
        viewModelScope.launch {
            source.latestReading.filterNotNull().collect(::onReading)
        }
    }

    fun refreshEnvironment() {
        val hasPermissions = measurementSource == MeasurementSource.DEMO ||
            BlePermissionHelper.hasRequiredPermissions(container.applicationContext)
        local.update { state ->
            state.copy(
                bluetoothSupport = source.supportStatus(),
                bluetoothPower = source.bluetoothPowerStatus(),
                permissionStatus = if (hasPermissions) {
                    BlePermissionStatus.GRANTED
                } else {
                    state.permissionStatus.takeIf {
                        it == BlePermissionStatus.PERMANENTLY_DENIED ||
                            it == BlePermissionStatus.DENIED
                    } ?: BlePermissionStatus.REQUIRED
                },
                status = when {
                    !hasPermissions -> LiveMeasurementStatus.PERMISSION_REQUIRED
                    source.bluetoothPowerStatus() == BluetoothPowerStatus.OFF ->
                        LiveMeasurementStatus.BLUETOOTH_OFF
                    else -> state.status.takeUnless {
                        it == LiveMeasurementStatus.PERMISSION_REQUIRED ||
                            it == LiveMeasurementStatus.BLUETOOTH_OFF
                    } ?: LiveMeasurementStatus.READY
                }
            )
        }
    }

    fun onPermissionsResult(permanentlyDenied: Boolean) {
        val granted = measurementSource == MeasurementSource.DEMO ||
            BlePermissionHelper.hasRequiredPermissions(container.applicationContext)
        local.update {
            it.copy(
                permissionStatus = when {
                    granted -> BlePermissionStatus.GRANTED
                    permanentlyDenied -> BlePermissionStatus.PERMANENTLY_DENIED
                    else -> BlePermissionStatus.DENIED
                }
            )
        }
        refreshEnvironment()
    }

    fun setProfile(profileId: String) =
        local.update {
            it.copy(
                selectedProfileId = profileId,
                profileSelectionExplicit = true
            )
        }

    fun clearProfile() = local.update {
        it.copy(
            selectedProfileId = null,
            profileSelectionExplicit = true
        )
    }

    fun setNote(value: String) =
        local.update { it.copy(note = value.take(500)) }

    fun confirmAdvertisedUnit(unit: WeightUnit) {
        viewModelScope.launch {
            container.preferencesRepository.setConfirmedBleUnit(unit)
        }
    }

    fun clearConfirmedUnit() {
        viewModelScope.launch {
            container.preferencesRepository.setConfirmedBleUnit(null)
        }
    }

    fun startScan() {
        refreshEnvironment()
        val state = local.value
        if (
            uiState.value.preferences.confirmedBleUnit == null ||
            state.permissionStatus != BlePermissionStatus.GRANTED ||
            state.bluetoothPower != BluetoothPowerStatus.ON ||
            state.bluetoothSupport != BleSupportStatus.SUPPORTED
        ) {
            return
        }
        detector.reset()
        local.update {
            it.copy(
                latestReading = null,
                stability = null,
                stableEvent = null,
                savedMeasurement = null,
                probableDuplicate = false,
                saveError = null,
                error = null,
                status = LiveMeasurementStatus.SEARCHING
            )
        }
        source.start()
    }

    fun stopScan() = source.stop(BleScanStopReason.MANUAL)

    fun dismissError() = local.update { state ->
        val nextStatus = when {
            state.savedMeasurement != null -> LiveMeasurementStatus.SAVED
            state.stableEvent != null -> LiveMeasurementStatus.STABLE
            state.permissionStatus != BlePermissionStatus.GRANTED ->
                LiveMeasurementStatus.PERMISSION_REQUIRED
            state.bluetoothPower == BluetoothPowerStatus.OFF ->
                LiveMeasurementStatus.BLUETOOTH_OFF
            else -> LiveMeasurementStatus.READY
        }
        state.copy(
            error = null,
            saveError = null,
            status = nextStatus
        )
    }

    fun save(acceptDuplicate: Boolean = false) {
        val state = uiState.value
        val event = state.stableEvent ?: return
        if (state.isSaving) return
        local.update {
            it.copy(
                isSaving = true,
                probableDuplicate = false,
                saveError = null,
                status = LiveMeasurementStatus.SAVING
            )
        }
        viewModelScope.launch {
            when (
                val result = saveBleMeasurement(
                    event = event,
                    profileId = state.selectedProfileId,
                    confirmedUnit = state.preferences.confirmedBleUnit,
                    note = state.note,
                    source = measurementSource,
                    acceptProbableDuplicate = acceptDuplicate
                )
            ) {
                is SaveBleResult.Saved -> {
                    syncToHealthConnectIfEnabled(result.measurement)
                    local.update {
                        it.copy(
                            isSaving = false,
                            savedMeasurement = result.measurement,
                            status = LiveMeasurementStatus.SAVED
                        )
                    }
                }
                is SaveBleResult.ProbableDuplicate -> local.update {
                    it.copy(
                        isSaving = false,
                        probableDuplicate = true,
                        status = LiveMeasurementStatus.STABLE
                    )
                }
                else -> local.update {
                    it.copy(
                        isSaving = false,
                        saveError = result,
                        status = LiveMeasurementStatus.ERROR
                    )
                }
            }
        }
    }

    fun dismissDuplicate() = local.update { it.copy(probableDuplicate = false) }
    fun confirmDuplicate() = save(acceptDuplicate = true)

    private suspend fun syncToHealthConnectIfEnabled(
        measurement: WeightMeasurement
    ) {
        if (measurement.source == MeasurementSource.DEMO) return
        val profile = measurement.profileId?.let { profileId ->
            container.profileRepository.findById(profileId)
        }
        if (profile?.healthConnectEnabled == true) {
            container.healthConnectWeightWriter.write(measurement)
        }
    }

    fun updateSavedNote() {
        val state = uiState.value
        val saved = state.savedMeasurement ?: return
        viewModelScope.launch {
            val updated = saved.copy(
                note = state.note.trim().takeIf(String::isNotEmpty),
                updatedAt = container.clock.now()
            )
            container.measurementRepository.update(updated)
            local.update { it.copy(savedMeasurement = updated) }
        }
    }

    fun updateSavedProfile() {
        val state = uiState.value
        val saved = state.savedMeasurement ?: return
        val selectedProfileId = state.selectedProfileId
        if (saved.profileId == selectedProfileId) return
        viewModelScope.launch {
            val updated = saved.copy(
                profileId = selectedProfileId,
                updatedAt = container.clock.now()
            )
            container.measurementRepository.update(updated)
            syncToHealthConnectIfEnabled(updated)
            local.update { it.copy(savedMeasurement = updated) }
        }
    }

    fun requestDeleteSaved() {
        if (uiState.value.savedMeasurement != null) {
            local.update { it.copy(deleteConfirmationVisible = true) }
        }
    }

    fun dismissDeleteSaved() =
        local.update { it.copy(deleteConfirmationVisible = false) }

    fun confirmDeleteSaved() {
        val saved = uiState.value.savedMeasurement ?: return
        viewModelScope.launch {
            container.measurementRepository.delete(saved)
            local.update {
                it.copy(
                    savedMeasurement = null,
                    stableEvent = null,
                    status = LiveMeasurementStatus.READY,
                    deleteConfirmationVisible = false,
                    deletedMeasurement = saved
                )
            }
        }
    }

    fun undoDeleteSaved() {
        val deleted = local.value.deletedMeasurement ?: return
        viewModelScope.launch {
            container.measurementRepository.insert(deleted)
            local.update {
                it.copy(
                    savedMeasurement = deleted,
                    deletedMeasurement = null,
                    status = LiveMeasurementStatus.SAVED
                )
            }
        }
    }

    fun consumeDeletedMeasurement() =
        local.update { it.copy(deletedMeasurement = null) }

    private fun onReading(reading: BleWeightReading) {
        if (!local.value.isScanning) return
        val progress = detector.accept(reading)
        local.update {
            it.copy(
                latestReading = reading,
                stability = progress,
                stableEvent = progress.stableEvent ?: it.stableEvent,
                status = if (progress.stableEvent != null) {
                    LiveMeasurementStatus.STABLE
                } else if (reading.isMeasurementPacket) {
                    LiveMeasurementStatus.RECEIVING
                } else {
                    LiveMeasurementStatus.WAITING_FOR_WEIGHT
                }
            )
        }
        if (progress.stableEvent != null) {
            source.stop(BleScanStopReason.MANUAL)
            val preferences = uiState.value.preferences
            if (
                preferences.autoSaveStableMeasurement &&
                !preferences.confirmBeforeSaving &&
                preferences.confirmedBleUnit != null
            ) {
                save()
            }
        }
    }

    private fun onScanState(state: BleScanState) {
        when (state) {
            BleScanState.Idle -> Unit
            is BleScanState.Scanning -> local.update {
                it.copy(
                    isScanning = true,
                    secondsRemaining = state.secondsRemaining,
                    scanPhase = state.phase,
                    status = if (it.latestReading == null) {
                        LiveMeasurementStatus.SEARCHING
                    } else {
                        it.status
                    },
                    error = null
                )
            }
            is BleScanState.Stopped -> local.update {
                it.copy(
                    isScanning = false,
                    secondsRemaining = 0,
                    scanPhase = null,
                    status = when {
                        it.savedMeasurement != null -> LiveMeasurementStatus.SAVED
                        it.stableEvent != null -> LiveMeasurementStatus.STABLE
                        else -> LiveMeasurementStatus.READY
                    }
                )
            }
            is BleScanState.Failed -> local.update {
                it.copy(
                    isScanning = false,
                    secondsRemaining = 0,
                    scanPhase = null,
                    status = LiveMeasurementStatus.ERROR,
                    error = state.error
                )
            }
        }
    }

    override fun onCleared() {
        if (receiverRegistered) {
            try {
                container.applicationContext.unregisterReceiver(bluetoothStateReceiver)
            } catch (_: IllegalArgumentException) {
                // O sistema já removeu o registro.
            }
            receiverRegistered = false
        }
        source.close()
        super.onCleared()
    }

    private fun registerBluetoothStateReceiver() {
        try {
            ContextCompat.registerReceiver(
                container.applicationContext,
                bluetoothStateReceiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        } catch (_: SecurityException) {
            local.update {
                it.copy(
                    status = LiveMeasurementStatus.ERROR,
                    error = BleScanError.SecurityFailure
                )
            }
        }
    }

    class Factory(
        private val container: AppContainer,
        private val sourceOverride:
            br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource? = null,
        private val measurementSource: MeasurementSource = MeasurementSource.BLE
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BleMeasurementViewModel(
                container,
                sourceOverride,
                measurementSource
            ) as T
    }
}
