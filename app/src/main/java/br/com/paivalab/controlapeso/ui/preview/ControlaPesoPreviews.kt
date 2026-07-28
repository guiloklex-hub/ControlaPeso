package br.com.paivalab.controlapeso.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.paivalab.controlapeso.ui.dashboard.DashboardScreen
import br.com.paivalab.controlapeso.ui.devices.DevicesScreen
import br.com.paivalab.controlapeso.ui.goals.GoalsScreen
import br.com.paivalab.controlapeso.ui.history.HistoryScreen
import br.com.paivalab.controlapeso.ui.measurement.edit.ManualMeasurementScreen
import br.com.paivalab.controlapeso.ui.measurement.live.BleMeasurementUiState
import br.com.paivalab.controlapeso.ui.measurement.live.LiveMeasurementScreen
import br.com.paivalab.controlapeso.ui.profiles.ProfilesScreen
import br.com.paivalab.controlapeso.ui.reports.ReportsScreen
import br.com.paivalab.controlapeso.ui.settings.SettingsScreen
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme

@Preview(
    name = "Compact claro",
    widthDp = 360,
    heightDp = 800,
    locale = "pt-rBR",
    showBackground = true
)
@Preview(
    name = "Compact escuro",
    widthDp = 360,
    heightDp = 800,
    locale = "pt-rBR",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
private annotation class CompactThemePreviews

@Preview(
    name = "Medium fonte 150%",
    widthDp = 600,
    heightDp = 900,
    fontScale = 1.5f,
    locale = "pt-rBR",
    showBackground = true
)
@Preview(
    name = "Expanded",
    widthDp = 1_000,
    heightDp = 800,
    locale = "pt-rBR",
    showBackground = true
)
private annotation class AdaptiveRiskPreviews

@CompactThemePreviews
@AdaptiveRiskPreviews
@Composable
private fun DashboardContentPreview() {
    PreviewTheme {
        DashboardScreen(
            state = PreviewStates.dashboard,
            onMeasure = {},
            onManual = {},
            onHistory = {},
            onProfiles = {},
            onGoals = {}
        )
    }
}

@Preview(name = "Dashboard vazio", widthDp = 360, heightDp = 800)
@Composable
private fun DashboardEmptyPreview() {
    PreviewTheme {
        DashboardScreen(
            state = PreviewStates.dashboardEmpty,
            onMeasure = {},
            onManual = {},
            onHistory = {},
            onProfiles = {},
            onGoals = {}
        )
    }
}

@Preview(name = "Medição aguardando", widthDp = 360, heightDp = 800)
@Composable
private fun LiveWaitingPreview() = LivePreview(PreviewStates.liveWaiting)

@Preview(name = "Medição variando", widthDp = 360, heightDp = 800)
@Composable
private fun LiveVaryingPreview() = LivePreview(PreviewStates.liveVarying)

@Preview(name = "Medição estável", widthDp = 360, heightDp = 800)
@Composable
private fun LiveStablePreview() = LivePreview(PreviewStates.liveStable)

@Preview(name = "Histórico compacto", widthDp = 360, heightDp = 800)
@Preview(name = "Histórico expanded", widthDp = 1_000, heightDp = 800)
@Composable
private fun HistoryContentPreview() {
    PreviewTheme {
        HistoryScreen(
            state = PreviewStates.history,
            onRangeChange = {},
            onCustomStartChange = {},
            onCustomEndChange = {},
            onToggleSource = {},
            onMeasurementClick = {}
        )
    }
}

@Preview(name = "Histórico vazio", widthDp = 360, heightDp = 800)
@Composable
private fun HistoryEmptyPreview() {
    PreviewTheme {
        HistoryScreen(
            state = PreviewStates.historyEmpty,
            onRangeChange = {},
            onCustomStartChange = {},
            onCustomEndChange = {},
            onToggleSource = {},
            onMeasurementClick = {}
        )
    }
}

@CompactThemePreviews
@Composable
private fun SettingsPreview() {
    PreviewTheme {
        SettingsScreen(
            state = PreviewStates.settings,
            onThemeChange = {},
            onDynamicColorsChange = {},
            onEffectsChange = {},
            onDefaultUnitChange = {},
            onDefaultProfileChange = {},
            onAutoSaveChange = {},
            onConfirmSaveChange = {},
            onVibrationChange = {},
            onSoundChange = {},
            onHistoryPeriodChange = {},
            onMovingAverageChange = {},
            onGroupingChange = {},
            onChartSizeChange = {},
            onHighContrastChange = {},
            onDetailedLogsChange = {},
            onReminderEnabledChange = {},
            onReminderDayToggle = {},
            onReminderTimeChange = { _, _ -> },
            onRequestHealthPermission = {},
            onHealthEnabledChange = {},
            onHealthSync = {},
            diagnosticLoggingAvailable = true,
            demoAvailable = true,
            onOpenDemoMeasurement = {},
            onClearDemoData = {},
            onDismissMessage = {},
            onProfiles = {},
            onGoals = {},
            onDevices = {},
            onDiagnostic = {},
            onReports = {},
            onPrivacy = {},
            onAbout = {}
        )
    }
}

@Preview(
    name = "Formulário fonte 200%",
    widthDp = 360,
    heightDp = 900,
    fontScale = 2f,
    locale = "pt-rBR",
    showBackground = true
)
@Composable
private fun ManualFormLargeFontPreview() {
    PreviewTheme {
        ManualMeasurementScreen(
            state = PreviewStates.manual,
            editing = false,
            onProfileChange = {},
            onWeightChange = {},
            onUnitChange = {},
            onDateChange = {},
            onTimeChange = {},
            onNoteChange = {},
            onSave = {},
            onDismissDuplicate = {},
            onConfirmDuplicate = {},
            onSaved = {},
            onConsumeSaved = {}
        )
    }
}

@Preview(name = "Perfis", widthDp = 360, heightDp = 800, locale = "pt-rBR")
@Composable
private fun ProfilesPreview() {
    PreviewTheme {
        ProfilesScreen(
            state = PreviewStates.profiles,
            onCreate = {},
            onEdit = {},
            onSetActive = {},
            onDelete = {},
            onDismissForm = {},
            onNameChange = {},
            onAvatarChange = {},
            onHeightChange = {},
            onBirthDateChange = {},
            onUnitChange = {},
            onSave = {},
            onDismissDelete = {},
            onConfirmDelete = {},
            onExportBeforeDelete = {}
        )
    }
}

@Preview(name = "Metas", widthDp = 360, heightDp = 800, locale = "pt-rBR")
@Composable
private fun GoalsPreview() {
    PreviewTheme {
        GoalsScreen(
            state = PreviewStates.goals,
            onCreate = {},
            onEdit = {},
            onSetStatus = { _, _ -> },
            onDelete = {},
            onDismissForm = {},
            onStartChange = {},
            onTargetChange = {},
            onDateChange = {},
            onSave = {},
            onDismissDelete = {},
            onConfirmDelete = {}
        )
    }
}

@Preview(name = "Balanças", widthDp = 600, heightDp = 900, locale = "pt-rBR")
@Composable
private fun DevicesPreview() {
    PreviewTheme {
        DevicesScreen(
            state = PreviewStates.devices,
            onPreferred = {},
            onForget = {},
            onDismissForget = {},
            onConfirmForget = {},
            onMeasure = {},
            onDiagnostic = {}
        )
    }
}

@Preview(name = "Relatórios", widthDp = 600, heightDp = 900, locale = "pt-rBR")
@Composable
private fun ReportsPreview() {
    PreviewTheme {
        ReportsScreen(
            state = PreviewStates.reports,
            onProfileChange = {},
            onPeriodChange = {},
            onCustomStartChange = {},
            onCustomEndChange = {},
            onFormatChange = {},
            onIncludeChartChange = {},
            onIncludeTableChange = {},
            onIncludeNotesChange = {},
            onIncludeMetricsChange = {},
            onUnitChange = {},
            onGenerate = {},
            onShare = {},
            onShareSummary = {},
            onSaveFile = {},
            onImport = {},
            onClearTemporaryFiles = {},
            onDismissImport = {},
            onRestore = {},
            onDismissMessage = {}
        )
    }
}

@Composable
private fun LivePreview(state: BleMeasurementUiState) {
    PreviewTheme {
        LiveMeasurementScreen(
            state = state,
            onRequestPermissions = {},
            onStart = {},
            onStop = {},
            onProfileChange = {},
            onConfirmUnit = {},
            onClearUnit = {},
            onNoteChange = {},
            onSave = {},
            onDismissDuplicate = {},
            onConfirmDuplicate = {},
            onUpdateNote = {},
            onUpdateSavedProfile = {},
            onRequestDeleteSaved = {},
            onDismissDeleteSaved = {},
            onConfirmDeleteSaved = {},
            onUndoDeleteSaved = {},
            onConsumeDeletedMeasurement = {},
            onShare = {}
        )
    }
}

@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    ControlaPesoTheme(dynamicColor = false, content = content)
}
