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

@Composable
fun ControlaPesoApp(
    uiState: AppUiState,
    container: AppContainer,
    scannerViewModel: ScannerViewModel,
    onRequestBlePermissions: () -> Unit,
    onShareText: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShareFile: (SharedReportFile) -> Unit,
    requestedDestination: String? = null,
    onDestinationConsumed: () -> Unit = {}
) {
    ControlaPesoTheme(
        themeMode = uiState.preferences.themeMode,
        dynamicColor = uiState.preferences.dynamicColors,
        highContrast = uiState.preferences.highContrast,
        visualEffects = uiState.preferences.visualEffects
    ) {
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
                OnboardingScreen(
                    state = onboardingState,
                    onNext = onboardingViewModel::next,
                    onPrevious = onboardingViewModel::previous,
                    onSkip = onboardingViewModel::skipOnboarding,
                    onFinish = onboardingViewModel::finish,
                    onRequestBlePermissions = onRequestBlePermissions,
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
                onRequestBlePermissions = onRequestBlePermissions,
                onShareText = onShareText,
                onCopyText = onCopyText,
                onShareFile = onShareFile,
                requestedDestination = requestedDestination,
                onDestinationConsumed = onDestinationConsumed
            )
        }
    }
}
