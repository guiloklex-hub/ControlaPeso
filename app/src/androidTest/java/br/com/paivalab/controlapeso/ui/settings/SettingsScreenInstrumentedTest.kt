package br.com.paivalab.controlapeso.ui.settings

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectAvailability
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun healthHistoryPreviewShowsCountPeriodAndRequiresConfirmation() {
        val confirmed = AtomicBoolean(false)
        val dismissed = AtomicBoolean(false)
        val profile = Profile(
            id = "profile",
            name = "Pessoa",
            avatarKey = "ocean",
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = true,
            isActive = true,
            createdAt = Instant.parse("2026-07-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-07-01T00:00:00Z")
        )

        composeRule.setContent {
            ControlaPesoTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                SettingsScreen(
                    state = SettingsUiState(
                        preferences = AppPreferences(),
                        profiles = listOf(profile),
                        activeProfile = profile,
                        healthAvailability = HealthConnectAvailability.AVAILABLE,
                        healthPermissionGranted = true,
                        healthSyncConfirmationVisible = true,
                        healthSyncMeasurementCount = 2,
                        healthSyncFirstAt = Instant.parse("2026-07-01T10:00:00Z"),
                        healthSyncLastAt = Instant.parse("2026-07-03T10:00:00Z")
                    ),
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
                    onConfirmHealthSync = { confirmed.set(true) },
                    onDismissHealthSync = { dismissed.set(true) },
                    diagnosticLoggingAvailable = true,
                    demoAvailable = false,
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

        composeRule.onNodeWithText("Aparência")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        composeRule.onNodeWithText("Revisar sincronização do histórico")
            .assertIsDisplayed()
        composeRule.onNodeWithText("2 medições serão enviadas", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("01/07/2026", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("03/07/2026", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Nada será enviado antes da sua confirmação.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Enviar histórico")
            .performClick()

        assertTrue(confirmed.get())
        assertTrue(!dismissed.get())
    }
}
