package br.com.paivalab.controlapeso.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.core.time.MeasurementTimeFormatter
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.demo.DemoBleSourceFactory
import br.com.paivalab.controlapeso.data.export.SharedReportFile
import br.com.paivalab.controlapeso.ui.dashboard.DashboardScreen
import br.com.paivalab.controlapeso.ui.dashboard.DashboardViewModel
import br.com.paivalab.controlapeso.ui.about.AboutScreen
import br.com.paivalab.controlapeso.ui.devices.DevicesScreen
import br.com.paivalab.controlapeso.ui.devices.DevicesViewModel
import br.com.paivalab.controlapeso.ui.goals.GoalsScreen
import br.com.paivalab.controlapeso.ui.goals.GoalsViewModel
import br.com.paivalab.controlapeso.ui.history.HistoryScreen
import br.com.paivalab.controlapeso.ui.history.HistoryViewModel
import br.com.paivalab.controlapeso.ui.measurement.detail.MeasurementDetailScreen
import br.com.paivalab.controlapeso.ui.measurement.detail.MeasurementDetailViewModel
import br.com.paivalab.controlapeso.ui.scanner.ScannerScreen
import br.com.paivalab.controlapeso.ui.scanner.ScannerViewModel
import br.com.paivalab.controlapeso.ui.measurement.edit.ManualMeasurementScreen
import br.com.paivalab.controlapeso.ui.measurement.edit.ManualMeasurementViewModel
import br.com.paivalab.controlapeso.ui.measurement.live.BleMeasurementViewModel
import br.com.paivalab.controlapeso.ui.measurement.live.LiveMeasurementScreen
import br.com.paivalab.controlapeso.ui.profiles.ProfilesScreen
import br.com.paivalab.controlapeso.ui.profiles.ProfilesViewModel
import br.com.paivalab.controlapeso.ui.privacy.PrivacyScreen
import br.com.paivalab.controlapeso.ui.privacy.PrivacyViewModel
import br.com.paivalab.controlapeso.ui.reports.ReportsScreen
import br.com.paivalab.controlapeso.ui.reports.ReportsViewModel
import br.com.paivalab.controlapeso.ui.settings.SettingsScreen
import br.com.paivalab.controlapeso.ui.settings.SettingsViewModel
import java.time.format.FormatStyle

@Composable
fun ControlaPesoNavHost(
    container: AppContainer,
    scannerViewModel: ScannerViewModel,
    onRequestBlePermissions: () -> Unit,
    onShareText: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShareFile: (SharedReportFile) -> Unit,
    modifier: Modifier = Modifier,
    requestedDestination: String? = null,
    onDestinationConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val navigateToPrimaryDestination: (AppDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }
    LaunchedEffect(requestedDestination) {
        if (requestedDestination in internalNotificationDestinations) {
            navController.navigate(requireNotNull(requestedDestination)) {
                launchSingleTop = true
            }
            onDestinationConsumed()
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val expanded = maxWidth >= 600.dp
        Scaffold(
            bottomBar = {
                if (!expanded) {
                    NavigationBar {
                        primaryDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                modifier = Modifier.testTag(
                                    "primary_navigation_${destination.route}"
                                ),
                                onClick = { navigateToPrimaryDestination(destination) },
                                icon = {
                                    destination.icon?.let {
                                        Icon(
                                            it,
                                            contentDescription =
                                                stringResource(destination.labelRes)
                                        )
                                    }
                                },
                                label = { Text(stringResource(destination.labelRes)) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (expanded) {
                    NavigationRail {
                        primaryDestinations.forEach { destination ->
                            NavigationRailItem(
                                selected = currentRoute == destination.route,
                                modifier = Modifier.testTag(
                                    "primary_navigation_${destination.route}"
                                ),
                                onClick = { navigateToPrimaryDestination(destination) },
                                icon = {
                                    destination.icon?.let {
                                        Icon(
                                            it,
                                            contentDescription =
                                                stringResource(destination.labelRes)
                                        )
                                    }
                                },
                                label = { Text(stringResource(destination.labelRes)) }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = AppDestination.Dashboard.route,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(AppDestination.Dashboard.route) {
                        val dashboardViewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModel.Factory(container)
                        )
                        val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                        DashboardScreen(
                            state = state,
                            onMeasure = {
                                navController.navigate(AppDestination.LiveMeasurement.route)
                            },
                            onManual = {
                                navController.navigate(AppDestination.ManualMeasurement.route)
                            },
                            onHistory = {
                                navController.navigate(AppDestination.History.route)
                            },
                            onProfiles = {
                                navController.navigate(AppDestination.Profiles.route)
                            },
                            onGoals = {
                                navController.navigate(AppDestination.Goals.route)
                            }
                        )
                    }
                    composable(AppDestination.History.route) {
                        val historyViewModel: HistoryViewModel = viewModel(
                            factory = HistoryViewModel.Factory(container)
                        )
                        val state by historyViewModel.uiState.collectAsStateWithLifecycle()
                        HistoryScreen(
                            state = state,
                            onRangeChange = historyViewModel::setRange,
                            onCustomStartChange = historyViewModel::setCustomStart,
                            onCustomEndChange = historyViewModel::setCustomEnd,
                            onToggleSource = historyViewModel::toggleSource,
                            onMeasurementClick = { id ->
                                navController.navigate(
                                    AppDestination.MeasurementDetail.route(id)
                                )
                            }
                        )
                    }
                    composable(AppDestination.Measure.route) {
                        MeasureMenu(
                            onLive = {
                                navController.navigate(AppDestination.LiveMeasurement.route)
                            },
                            onManual = {
                                navController.navigate(AppDestination.ManualMeasurement.route)
                            },
                            onDiagnostic = {
                                navController.navigate(AppDestination.Diagnostic.route)
                            }
                        )
                    }
                    composable(AppDestination.Reports.route) {
                        val reportsViewModel: ReportsViewModel = viewModel(
                            factory = ReportsViewModel.Factory(container)
                        )
                        val state by reportsViewModel.uiState.collectAsStateWithLifecycle()
                        val createDocument = rememberLauncherForActivityResult(
                            ActivityResultContracts.CreateDocument(
                                state.generatedFile?.mimeType ?: "application/octet-stream"
                            )
                        ) { uri ->
                            uri?.let(reportsViewModel::saveGeneratedTo)
                        }
                        val openDocument = rememberLauncherForActivityResult(
                            ActivityResultContracts.OpenDocument()
                        ) { uri ->
                            uri?.let(reportsViewModel::importFrom)
                        }
                        ReportsScreen(
                            state = state,
                            onProfileChange = reportsViewModel::setProfile,
                            onPeriodChange = reportsViewModel::setPeriod,
                            onCustomStartChange = reportsViewModel::setCustomStart,
                            onCustomEndChange = reportsViewModel::setCustomEnd,
                            onFormatChange = reportsViewModel::setFormat,
                            onIncludeChartChange = reportsViewModel::setIncludeChart,
                            onIncludeTableChange = reportsViewModel::setIncludeTable,
                            onIncludeNotesChange = reportsViewModel::setIncludeNotes,
                            onIncludeMetricsChange =
                                reportsViewModel::setIncludeAdditionalMetrics,
                            onUnitChange = reportsViewModel::setUnit,
                            onGenerate = reportsViewModel::generate,
                            onShare = {
                                state.generatedFile?.let(onShareFile)
                            },
                            onShareSummary = {
                                state.generatedSummaryText?.let(onShareText)
                            },
                            onSaveFile = {
                                state.generatedFile?.let { file ->
                                    createDocument.launch(file.displayName)
                                }
                            },
                            onImport = {
                                openDocument.launch(
                                    arrayOf(
                                        "application/json",
                                        "text/json",
                                        "application/octet-stream"
                                    )
                                )
                            },
                            onClearTemporaryFiles =
                                reportsViewModel::clearTemporaryFiles,
                            onDismissImport = reportsViewModel::dismissImportPreview,
                            onRestore = reportsViewModel::restore,
                            onDismissMessage = reportsViewModel::dismissMessage
                        )
                    }
                    composable(AppDestination.Settings.route) {
                        val settingsViewModel: SettingsViewModel = viewModel(
                            factory = SettingsViewModel.Factory(container)
                        )
                        val settingsState by settingsViewModel.uiState
                            .collectAsStateWithLifecycle()
                        val context = LocalContext.current
                        val notificationPermissionLauncher =
                            rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                settingsViewModel.setReminderEnabled(granted)
                            }
                        val healthPermissionLauncher =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                rememberLauncherForActivityResult(
                                    container.healthConnectWeightWriter.permissionContract()
                                ) { granted ->
                                    settingsViewModel.onHealthPermissionResult(granted)
                                }
                            } else {
                                null
                            }
                        LifecycleResumeEffect(Unit) {
                            settingsViewModel.refreshHealthConnect()
                            onPauseOrDispose { }
                        }
                        SettingsScreen(
                            state = settingsState,
                            onThemeChange = settingsViewModel::setTheme,
                            onDynamicColorsChange = settingsViewModel::setDynamicColors,
                            onEffectsChange = settingsViewModel::setVisualEffects,
                            onDefaultUnitChange =
                                settingsViewModel::setDefaultWeightUnit,
                            onDefaultProfileChange =
                                settingsViewModel::setDefaultProfile,
                            onAutoSaveChange = settingsViewModel::setAutoSave,
                            onConfirmSaveChange =
                                settingsViewModel::setConfirmBeforeSaving,
                            onVibrationChange = settingsViewModel::setVibration,
                            onSoundChange = settingsViewModel::setSound,
                            onHistoryPeriodChange =
                                settingsViewModel::setDefaultHistoryPeriod,
                            onMovingAverageChange =
                                settingsViewModel::setMovingAverage,
                            onGroupingChange = settingsViewModel::setHistoryGrouping,
                            onChartSizeChange = settingsViewModel::setChartSize,
                            onHighContrastChange = settingsViewModel::setHighContrast,
                            onDetailedLogsChange =
                                settingsViewModel::setDetailedBleLogs,
                            onReminderEnabledChange = { enabled ->
                                if (
                                    enabled &&
                                    Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                } else {
                                    settingsViewModel.setReminderEnabled(enabled)
                                }
                            },
                            onReminderDayToggle =
                                settingsViewModel::toggleReminderDay,
                            onReminderTimeChange =
                                settingsViewModel::setReminderTime,
                            onRequestHealthPermission = {
                                healthPermissionLauncher?.launch(
                                    container.healthConnectWeightWriter.requiredPermissions
                                )
                            },
                            onHealthEnabledChange =
                                settingsViewModel::setHealthConnectEnabled,
                            onHealthSync = settingsViewModel::syncActiveProfile,
                            diagnosticLoggingAvailable =
                                br.com.paivalab.controlapeso.BuildConfig.DEBUG,
                            demoAvailable = DemoBleSourceFactory.isAvailable,
                            onOpenDemoMeasurement = {
                                navController.navigate(
                                    AppDestination.DemoMeasurement.route
                                )
                            },
                            onClearDemoData = settingsViewModel::clearDemoData,
                            onDismissMessage = settingsViewModel::dismissMessage,
                            onProfiles = {
                                navController.navigate(AppDestination.Profiles.route)
                            },
                            onGoals = { navController.navigate(AppDestination.Goals.route) },
                            onDevices = {
                                navController.navigate(AppDestination.Devices.route)
                            },
                            onDiagnostic = {
                                navController.navigate(AppDestination.Diagnostic.route)
                            },
                            onReports = {
                                navController.navigate(AppDestination.Reports.route)
                            },
                            onPrivacy = {
                                navController.navigate(AppDestination.Privacy.route)
                            },
                            onAbout = { navController.navigate(AppDestination.About.route) }
                        )
                    }
                    composable(AppDestination.Diagnostic.route) {
                        val scannerState by scannerViewModel.uiState.collectAsStateWithLifecycle()
                        ScannerScreen(
                            uiState = scannerState,
                            onRequestPermissions = onRequestBlePermissions,
                            onStartScan = scannerViewModel::startScan,
                            onStopScan = scannerViewModel::stopScan,
                            onClearResults = scannerViewModel::clearResults,
                            onDismissError = scannerViewModel::dismissError,
                            onCopyText = onCopyText,
                            onShareText = onShareText
                        )
                    }
                    composable(AppDestination.Profiles.route) {
                        val profilesViewModel: ProfilesViewModel = viewModel(
                            factory = ProfilesViewModel.Factory(container)
                        )
                        val state by profilesViewModel.uiState.collectAsStateWithLifecycle()
                        ProfilesScreen(
                            state = state,
                            onCreate = profilesViewModel::startCreate,
                            onEdit = profilesViewModel::startEdit,
                            onSetActive = profilesViewModel::setActive,
                            onDelete = profilesViewModel::requestDelete,
                            onDismissForm = profilesViewModel::dismissForm,
                            onNameChange = profilesViewModel::setName,
                            onAvatarChange = profilesViewModel::setAvatar,
                            onHeightChange = profilesViewModel::setHeight,
                            onBirthDateChange = profilesViewModel::setBirthDate,
                            onUnitChange = profilesViewModel::setUnit,
                            onSave = profilesViewModel::save,
                            onDismissDelete = profilesViewModel::dismissDelete,
                            onConfirmDelete = profilesViewModel::confirmDelete,
                            onExportBeforeDelete = {
                                navController.navigate(AppDestination.Reports.route)
                            }
                        )
                    }
                    composable(AppDestination.Goals.route) {
                        val goalsViewModel: GoalsViewModel = viewModel(
                            factory = GoalsViewModel.Factory(container)
                        )
                        val state by goalsViewModel.uiState.collectAsStateWithLifecycle()
                        GoalsScreen(
                            state = state,
                            onCreate = goalsViewModel::startCreate,
                            onEdit = goalsViewModel::startEdit,
                            onSetStatus = goalsViewModel::setStatus,
                            onDelete = goalsViewModel::requestDelete,
                            onDismissForm = goalsViewModel::dismissForm,
                            onStartChange = goalsViewModel::setStartWeight,
                            onTargetChange = goalsViewModel::setTargetWeight,
                            onDateChange = goalsViewModel::setTargetDate,
                            onSave = goalsViewModel::save,
                            onDismissDelete = goalsViewModel::dismissDelete,
                            onConfirmDelete = goalsViewModel::confirmDelete
                        )
                    }
                    composable(AppDestination.LiveMeasurement.route) {
                        val liveViewModel: BleMeasurementViewModel = viewModel(
                            factory = BleMeasurementViewModel.Factory(container)
                        )
                        val state by liveViewModel.uiState.collectAsStateWithLifecycle()
                        LifecycleResumeEffect(Unit) {
                            liveViewModel.refreshEnvironment()
                            onPauseOrDispose { }
                        }
                        val saved = state.savedMeasurement
                        val unit = state.preferences.confirmedBleUnit
                        val shareText = if (saved != null && unit != null) {
                            stringResource(
                                R.string.share_measurement_template,
                                unit.fromKilograms(saved.weightKg),
                                unit.symbol,
                                MeasurementTimeFormatter.dateTime(
                                    saved,
                                    dateStyle = FormatStyle.SHORT
                                )
                            )
                        } else {
                            ""
                        }
                        LiveMeasurementScreen(
                            state = state,
                            onRequestPermissions = onRequestBlePermissions,
                            onStart = liveViewModel::startScan,
                            onStop = liveViewModel::stopScan,
                            onProfileChange = liveViewModel::setProfile,
                            onConfirmUnit = liveViewModel::confirmAdvertisedUnit,
                            onClearUnit = liveViewModel::clearConfirmedUnit,
                            onNoteChange = liveViewModel::setNote,
                            onSave = { liveViewModel.save() },
                            onDismissDuplicate = liveViewModel::dismissDuplicate,
                            onConfirmDuplicate = liveViewModel::confirmDuplicate,
                            onUpdateNote = liveViewModel::updateSavedNote,
                            onUpdateSavedProfile =
                                liveViewModel::updateSavedProfile,
                            onRequestDeleteSaved =
                                liveViewModel::requestDeleteSaved,
                            onDismissDeleteSaved =
                                liveViewModel::dismissDeleteSaved,
                            onConfirmDeleteSaved =
                                liveViewModel::confirmDeleteSaved,
                            onUndoDeleteSaved = liveViewModel::undoDeleteSaved,
                            onConsumeDeletedMeasurement =
                                liveViewModel::consumeDeletedMeasurement,
                            onShare = { if (shareText.isNotEmpty()) onShareText(shareText) }
                        )
                    }
                    if (DemoBleSourceFactory.isAvailable) {
                        composable(AppDestination.DemoMeasurement.route) {
                            val context = LocalContext.current
                            val demoSource = androidx.compose.runtime.remember {
                                DemoBleSourceFactory.create(context)
                            }
                            val demoViewModel: BleMeasurementViewModel = viewModel(
                                factory = BleMeasurementViewModel.Factory(
                                    container = container,
                                    sourceOverride = demoSource,
                                    measurementSource = MeasurementSource.DEMO
                                )
                            )
                            val state by demoViewModel.uiState
                                .collectAsStateWithLifecycle()
                            LiveMeasurementScreen(
                                state = state,
                                onRequestPermissions = {},
                                onStart = demoViewModel::startScan,
                                onStop = demoViewModel::stopScan,
                                onProfileChange = demoViewModel::setProfile,
                                onConfirmUnit = demoViewModel::confirmAdvertisedUnit,
                                onClearUnit = demoViewModel::clearConfirmedUnit,
                                onNoteChange = demoViewModel::setNote,
                                onSave = { demoViewModel.save() },
                                onDismissDuplicate = demoViewModel::dismissDuplicate,
                                onConfirmDuplicate = demoViewModel::confirmDuplicate,
                                onUpdateNote = demoViewModel::updateSavedNote,
                                onUpdateSavedProfile =
                                    demoViewModel::updateSavedProfile,
                                onRequestDeleteSaved =
                                    demoViewModel::requestDeleteSaved,
                                onDismissDeleteSaved =
                                    demoViewModel::dismissDeleteSaved,
                                onConfirmDeleteSaved =
                                    demoViewModel::confirmDeleteSaved,
                                onUndoDeleteSaved = demoViewModel::undoDeleteSaved,
                                onConsumeDeletedMeasurement =
                                    demoViewModel::consumeDeletedMeasurement,
                                onShare = {},
                                demoMode = true
                            )
                        }
                    }
                    composable(AppDestination.ManualMeasurement.route) {
                        val manualViewModel: ManualMeasurementViewModel = viewModel(
                            factory = ManualMeasurementViewModel.Factory(container)
                        )
                        val state by manualViewModel.uiState.collectAsStateWithLifecycle()
                        ManualMeasurementScreen(
                            state = state,
                            editing = false,
                            onProfileChange = manualViewModel::setProfile,
                            onWeightChange = manualViewModel::setWeight,
                            onUnitChange = manualViewModel::setUnit,
                            onDateChange = manualViewModel::setDate,
                            onTimeChange = manualViewModel::setTime,
                            onNoteChange = manualViewModel::setNote,
                            onSave = { manualViewModel.save() },
                            onDismissDuplicate = manualViewModel::dismissDuplicate,
                            onConfirmDuplicate = manualViewModel::confirmDuplicate,
                            onSaved = { id ->
                                navController.navigate(
                                    AppDestination.MeasurementDetail.route(id)
                                )
                            },
                            onConsumeSaved = manualViewModel::consumeSaved
                        )
                    }
                    composable(
                        route = AppDestination.EditMeasurement.route,
                        arguments = listOf(
                            navArgument("measurementId") { type = NavType.StringType }
                        )
                    ) { entry ->
                        val id = entry.arguments?.getString("measurementId")
                        val manualViewModel: ManualMeasurementViewModel = viewModel(
                            factory = ManualMeasurementViewModel.Factory(container, id)
                        )
                        val state by manualViewModel.uiState.collectAsStateWithLifecycle()
                        ManualMeasurementScreen(
                            state = state,
                            editing = true,
                            onProfileChange = manualViewModel::setProfile,
                            onWeightChange = manualViewModel::setWeight,
                            onUnitChange = manualViewModel::setUnit,
                            onDateChange = manualViewModel::setDate,
                            onTimeChange = manualViewModel::setTime,
                            onNoteChange = manualViewModel::setNote,
                            onSave = { manualViewModel.save() },
                            onDismissDuplicate = manualViewModel::dismissDuplicate,
                            onConfirmDuplicate = manualViewModel::confirmDuplicate,
                            onSaved = { navController.popBackStack() },
                            onConsumeSaved = manualViewModel::consumeSaved
                        )
                    }
                    composable(
                        route = AppDestination.MeasurementDetail.route,
                        arguments = listOf(
                            navArgument("measurementId") { type = NavType.StringType }
                        )
                    ) { entry ->
                        val id = entry.arguments?.getString("measurementId").orEmpty()
                        val detailViewModel: MeasurementDetailViewModel = viewModel(
                            factory = MeasurementDetailViewModel.Factory(container, id)
                        )
                        val state by detailViewModel.uiState.collectAsStateWithLifecycle()
                        val measurement = state.measurement
                        val unit = state.profile?.preferredWeightUnit
                            ?: WeightUnit.KILOGRAM
                        val shareText = if (measurement != null) {
                            stringResource(
                                R.string.share_measurement_template,
                                unit.fromKilograms(measurement.weightKg),
                                unit.symbol,
                                MeasurementTimeFormatter.dateTime(
                                    measurement,
                                    dateStyle = FormatStyle.SHORT
                                )
                            )
                        } else {
                            ""
                        }
                        MeasurementDetailScreen(
                            state = state,
                            onEdit = { measurementId ->
                                navController.navigate(
                                    AppDestination.EditMeasurement.route(measurementId)
                                )
                            },
                            onRequestDelete = detailViewModel::requestDelete,
                            onDismissDelete = detailViewModel::dismissDelete,
                            onConfirmDelete = detailViewModel::confirmDelete,
                            onUndoDelete = detailViewModel::undoDelete,
                            onConsumeDeleteNotice = detailViewModel::consumeDeleteNotice,
                            onShare = { if (shareText.isNotEmpty()) onShareText(shareText) }
                        )
                    }
                    composable(AppDestination.Devices.route) {
                        val devicesViewModel: DevicesViewModel = viewModel(
                            factory = DevicesViewModel.Factory(container)
                        )
                        val state by devicesViewModel.uiState.collectAsStateWithLifecycle()
                        DevicesScreen(
                            state = state,
                            onPreferred = devicesViewModel::setPreferred,
                            onForget = devicesViewModel::requestForget,
                            onDismissForget = devicesViewModel::dismissForget,
                            onConfirmForget = devicesViewModel::confirmForget,
                            onMeasure = {
                                navController.navigate(AppDestination.LiveMeasurement.route)
                            },
                            onDiagnostic = {
                                navController.navigate(AppDestination.Diagnostic.route)
                            }
                        )
                    }
                    composable(AppDestination.Privacy.route) {
                        val privacyViewModel: PrivacyViewModel = viewModel(
                            factory = PrivacyViewModel.Factory(container)
                        )
                        val state by privacyViewModel.uiState.collectAsStateWithLifecycle()
                        PrivacyScreen(
                            state = state,
                            onExport = {
                                navController.navigate(AppDestination.Reports.route)
                            },
                            onClearTemporaryFiles =
                                privacyViewModel::clearTemporaryFiles,
                            onConsumeTemporaryNotice =
                                privacyViewModel::consumeTemporaryNotice,
                            onRequestDelete = privacyViewModel::requestDelete,
                            onDismissDelete = privacyViewModel::dismissDelete,
                            onContinueDelete = privacyViewModel::continueDelete,
                            onConfirmationTextChange =
                                privacyViewModel::setConfirmationText,
                            onConfirmDelete = privacyViewModel::confirmDelete
                        )
                    }
                    composable(AppDestination.About.route) {
                        AboutScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasureMenu(
    onLive: () -> Unit,
    onManual: () -> Unit,
    onDiagnostic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.measure_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Button(onClick = onLive) { Text(stringResource(R.string.measure_with_scale)) }
        Button(onClick = onManual) { Text(stringResource(R.string.add_manual_weight)) }
        Button(onClick = onDiagnostic) { Text(stringResource(R.string.open_diagnostic)) }
    }
}
