package br.com.paivalab.controlapeso.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.data.export.SharedReportFile
import br.com.paivalab.controlapeso.ui.navigation.ControlaPesoNavHost
import br.com.paivalab.controlapeso.ui.onboarding.OnboardingScreen
import br.com.paivalab.controlapeso.ui.onboarding.OnboardingViewModel
import br.com.paivalab.controlapeso.ui.scanner.ScannerViewModel
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import br.com.paivalab.controlapeso.ui.update.ReleaseUpdateDialog
import br.com.paivalab.controlapeso.ui.update.ReleaseUpdateUiState

@Composable
fun ControlaPesoApp(
    uiState: AppUiState,
    container: AppContainer,
    scannerViewModel: ScannerViewModel,
    onRequestBlePermissions: () -> Unit,
    onShareText: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShareFile: (SharedReportFile) -> Unit,
    updateState: ReleaseUpdateUiState,
    onCheckForUpdates: () -> Unit,
    onViewReleaseNotes: () -> Unit = {},
    onDismissUpdate: () -> Unit,
    onDownloadAndInstallUpdate: () -> Unit,
    onDismissUpdateInstallFeedback: () -> Unit,
    requestedDestination: String? = null,
    onDestinationConsumed: () -> Unit = {}
) {
    ControlaPesoTheme(
        themeMode = uiState.preferences.themeMode,
        dynamicColor = uiState.preferences.dynamicColors,
        highContrast = uiState.preferences.highContrast,
        visualEffects = uiState.preferences.visualEffects
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                !uiState.preferences.onboardingCompleted -> {
                    val onboardingViewModel: OnboardingViewModel = viewModel(
                        factory = OnboardingViewModel.Factory(container)
                    )
                    val onboardingState by onboardingViewModel.uiState
                        .collectAsStateWithLifecycle()
                    val scannerState by scannerViewModel.uiState
                        .collectAsStateWithLifecycle()
                    OnboardingScreen(
                        state = onboardingState,
                        onNext = onboardingViewModel::next,
                        onPrevious = onboardingViewModel::previous,
                        onSkip = onboardingViewModel::skipOnboarding,
                        onFinish = onboardingViewModel::finish,
                        onRequestBlePermissions = onRequestBlePermissions,
                        blePermissionStatus = scannerState.permissionStatus,
                        onNameChange = onboardingViewModel::setName,
                        onHeightChange = onboardingViewModel::setHeight,
                        onBirthDateChange = onboardingViewModel::setBirthDate,
                        onUnitChange = onboardingViewModel::setUnit,
                        onTargetChange = onboardingViewModel::setTargetWeight,
                        onThemeChange = onboardingViewModel::setTheme
                    )
                }

                else -> ControlaPesoNavHost(
                    container = container,
                    scannerViewModel = scannerViewModel,
                    activeProfile = uiState.activeProfile,
                    onRequestBlePermissions = onRequestBlePermissions,
                    onShareText = onShareText,
                    onCopyText = onCopyText,
                    onShareFile = onShareFile,
                    updateState = updateState,
                    onCheckForUpdates = onCheckForUpdates,
                    onViewReleaseNotes = onViewReleaseNotes,
                    requestedDestination = requestedDestination,
                    onDestinationConsumed = onDestinationConsumed
                )
            }
            ReleaseUpdateDialog(
                state = updateState,
                onDismiss = onDismissUpdate,
                onDownloadAndInstall = onDownloadAndInstallUpdate,
                onDismissInstallFeedback = onDismissUpdateInstallFeedback
            )
        }
    }
}
