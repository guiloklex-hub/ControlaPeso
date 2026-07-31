package br.com.paivalab.controlapeso.ui.preview

import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectAvailability
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.goals.CalculateGoalProgress
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import br.com.paivalab.controlapeso.ui.dashboard.DashboardUiState
import br.com.paivalab.controlapeso.ui.devices.DevicesUiState
import br.com.paivalab.controlapeso.ui.goals.GoalItem
import br.com.paivalab.controlapeso.ui.goals.GoalsUiState
import br.com.paivalab.controlapeso.ui.history.HistoryUiState
import br.com.paivalab.controlapeso.ui.measurement.edit.ManualMeasurementUiState
import br.com.paivalab.controlapeso.ui.measurement.live.BleMeasurementUiState
import br.com.paivalab.controlapeso.ui.measurement.live.LiveMeasurementStatus
import br.com.paivalab.controlapeso.ui.profiles.ProfilesUiState
import br.com.paivalab.controlapeso.ui.reports.ReportsUiState
import br.com.paivalab.controlapeso.ui.settings.SettingsUiState

object PreviewStates {
    private val preferences = AppPreferences(
        onboardingCompleted = true,
        dynamicColors = false,
        visualEffects = VisualEffects.FULL,
        confirmedBleUnit = WeightUnit.KILOGRAM
    )

    val dashboard = DashboardUiState(
        isLoading = false,
        profile = PreviewProfiles.primary,
        preferences = preferences,
        measurements = PreviewMeasurements.history,
        statistics = MeasurementStatistics.calculate(PreviewMeasurements.history),
        activeGoal = PreviewGoals.active,
        goalProgress = CalculateGoalProgress(
            PreviewGoals.active,
            PreviewMeasurements.history.last().weightKg
        ),
        currentHour = 9
    )

    val dashboardEmpty = dashboard.copy(
        measurements = emptyList(),
        statistics = null,
        activeGoal = null,
        goalProgress = null
    )

    val history = HistoryUiState(
        isLoading = false,
        profile = PreviewProfiles.primary,
        preferences = preferences,
        measurements = PreviewMeasurements.history,
        statistics = MeasurementStatistics.calculate(PreviewMeasurements.history)
    )

    val historyEmpty = history.copy(measurements = emptyList(), statistics = null)

    val liveWaiting = BleMeasurementUiState(
        profiles = PreviewProfiles.all,
        selectedProfileId = PreviewProfiles.primary.id,
        preferences = preferences,
        bluetoothSupport = BleSupportStatus.SUPPORTED,
        bluetoothPower = BluetoothPowerStatus.ON,
        permissionStatus = BlePermissionStatus.GRANTED,
        status = LiveMeasurementStatus.WAITING_FOR_WEIGHT,
        isScanning = true,
        secondsRemaining = 12
    )

    val liveFirstMeasurement = liveWaiting.copy(
        preferences = preferences.copy(confirmedBleUnit = null),
        isScanning = false,
        status = LiveMeasurementStatus.READY,
        secondsRemaining = 0
    )

    val liveVarying = liveWaiting.copy(
        status = LiveMeasurementStatus.RECEIVING,
        latestReading = PreviewMeasurements.reading,
        stability = PreviewMeasurements.varyingProgress,
        secondsRemaining = 8
    )

    val liveStable = liveVarying.copy(
        status = LiveMeasurementStatus.STABLE,
        stability = PreviewMeasurements.stableProgress,
        stableEvent = PreviewMeasurements.stableEvent,
        secondsRemaining = 0,
        isScanning = false
    )

    val settings = SettingsUiState(
        preferences = preferences.copy(remindersEnabled = true),
        profiles = PreviewProfiles.all,
        activeProfile = PreviewProfiles.primary,
        healthAvailability = HealthConnectAvailability.AVAILABLE,
        healthPermissionGranted = true
    )

    val profiles = ProfilesUiState(profiles = PreviewProfiles.all)

    val goals = GoalsUiState(
        profile = PreviewProfiles.primary,
        goals = listOf(
            GoalItem(
                goal = PreviewGoals.active,
                progress = CalculateGoalProgress(
                    PreviewGoals.active,
                    PreviewMeasurements.history.last().weightKg
                )
            )
        ),
        latestWeightKg = PreviewMeasurements.history.last().weightKg
    )

    val devices = DevicesUiState(
        isLoading = false,
        devices = PreviewDevices.known
    )

    val reports = ReportsUiState(
        profiles = PreviewProfiles.all,
        selectedProfileId = PreviewProfiles.primary.id
    )

    val manual = ManualMeasurementUiState(
        profiles = PreviewProfiles.all,
        selectedProfileId = PreviewProfiles.primary.id,
        weightText = "78,4",
        unit = WeightUnit.KILOGRAM,
        dateText = "28072026",
        timeText = "07:30",
        note = "Texto longo de teste para conferir o reflow do formulário."
    )
}
