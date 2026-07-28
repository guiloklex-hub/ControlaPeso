package br.com.paivalab.controlapeso.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import br.com.paivalab.controlapeso.app.ControlaPesoApplication
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.dashboard.DashboardScreen
import br.com.paivalab.controlapeso.ui.dashboard.DashboardUiState
import br.com.paivalab.controlapeso.ui.history.HistoryScreen
import br.com.paivalab.controlapeso.ui.history.HistoryUiState
import br.com.paivalab.controlapeso.ui.measurement.edit.ManualMeasurementScreen
import br.com.paivalab.controlapeso.ui.measurement.edit.ManualMeasurementUiState
import br.com.paivalab.controlapeso.ui.navigation.ControlaPesoNavHost
import br.com.paivalab.controlapeso.ui.scanner.ScannerViewModel
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class UiSmokeInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun manualFormInvokesSaveAndRemainsUsableAtLargeFont() {
        val saved = AtomicBoolean(false)
        val profile = profile()
        composeRule.setContent {
            val density = LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f)
            ) {
                ControlaPesoTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    Box(Modifier.width(320.dp)) {
                        ManualMeasurementScreen(
                            state = ManualMeasurementUiState(
                                profiles = listOf(profile),
                                selectedProfileId = profile.id,
                                weightText = "75,4",
                                dateText = "2026-07-27",
                                timeText = "18:30"
                            ),
                            editing = false,
                            onProfileChange = {},
                            onWeightChange = {},
                            onUnitChange = {},
                            onDateChange = {},
                            onTimeChange = {},
                            onNoteChange = {},
                            onSave = { saved.set(true) },
                            onDismissDuplicate = {},
                            onConfirmDuplicate = {},
                            onSaved = {},
                            onConsumeSaved = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("Salvar medição").performScrollTo().performClick()

        assertTrue(saved.get())
    }

    @Test
    fun dashboardRendersAtExpandedWidthInDarkTheme() {
        composeRule.setContent {
            ControlaPesoTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                Box(Modifier.width(840.dp)) {
                    DashboardScreen(
                        state = DashboardUiState(isLoading = false),
                        onMeasure = {},
                        onManual = {},
                        onHistory = {},
                        onProfiles = {},
                        onGoals = {}
                    )
                }
            }
        }

        composeRule.onNodeWithText("Crie ou selecione um perfil para começar.")
            .assertIsDisplayed()
    }

    @Test
    fun expandedHistoryShowsListDetailWithoutTechnicalBleData() {
        val profile = profile()
        val measurement = measurement()
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                Box(Modifier.width(840.dp)) {
                    HistoryScreen(
                        state = HistoryUiState(
                            isLoading = false,
                            profile = profile,
                            measurements = listOf(measurement),
                            statistics = MeasurementStatistics.calculate(
                                listOf(measurement)
                            )
                        ),
                        onRangeChange = {},
                        onCustomStartChange = {},
                        onCustomEndChange = {},
                        onToggleSource = {},
                        onMeasurementClick = {}
                    )
                }
            }
        }

        composeRule.onNodeWithText("Medição selecionada").assertIsDisplayed()
        composeRule.onNodeWithText("Abrir detalhe completo").assertIsDisplayed()
        composeRule.onAllNodesWithText("AA:BB:CC:DD:EE:FF").fetchSemanticsNodes()
            .also { assertTrue(it.isEmpty()) }
    }

    @Test
    fun primaryNavigationOpensHistory() {
        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as ControlaPesoApplication
        val scannerViewModel = ScannerViewModel(application)
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                ControlaPesoNavHost(
                    container = application.container,
                    scannerViewModel = scannerViewModel,
                    onRequestBlePermissions = {},
                    onShareText = {},
                    onCopyText = {},
                    onShareFile = {}
                )
            }
        }

        composeRule.onNodeWithTag("primary_navigation_history").performClick()

        composeRule.onNodeWithText("Filtrar por origem").assertIsDisplayed()
    }

    private fun profile(): Profile {
        val instant = Instant.parse("2026-07-27T12:00:00Z")
        return Profile(
            id = "profile",
            name = "Pessoa",
            avatarKey = "ocean",
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = instant,
            updatedAt = instant
        )
    }

    private fun measurement(): WeightMeasurement {
        val instant = Instant.parse("2026-07-27T12:00:00Z")
        return WeightMeasurement(
            id = "measurement",
            profileId = "profile",
            weightKg = 75.4,
            measuredAt = instant,
            zoneOffsetSeconds = -10_800,
            source = MeasurementSource.BLE,
            isStable = true,
            deviceId = "device",
            deviceName = "Yoda1",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            note = "Teste",
            rawPayloadHex = "C0 68 1D 74",
            impedanceOne = null,
            impedanceTwo = null,
            bodyFatPercent = null,
            muscleMassKg = null,
            bodyWaterPercent = null,
            boneMassKg = null,
            visceralFatLevel = null,
            metabolicAge = null,
            createdAt = instant,
            updatedAt = instant
        )
    }
}
