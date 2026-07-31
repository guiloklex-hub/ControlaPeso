package br.com.paivalab.controlapeso.ui.measurement.live

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LiveMeasurementScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun firstMeasurementHighlightsUnitAndBlocksStartUntilItIsConfirmed() {
        val confirmed = AtomicReference<WeightUnit?>()
        val started = AtomicBoolean(false)
        setContent(
            state = readyState(confirmedUnit = null),
            onStart = { started.set(true) },
            onConfirmUnit = { confirmed.set(it) }
        )

        composeRule.onNodeWithText("1. Confirme a unidade da balança")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Escolha kg ou lb para liberar a medição.")
            .assertIsDisplayed()
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Iniciar medição"))
        composeRule.onNodeWithText("Iniciar medição")
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("O visor está em kg"))
        composeRule.onNodeWithText("O visor está em kg")
            .performClick()

        assertFalse(started.get())
        assertTrue(confirmed.get() == WeightUnit.KILOGRAM)
    }

    @Test
    fun confirmedUnitUsesCompactSummaryAndEnablesStart() {
        val started = AtomicBoolean(false)
        val reviewRequested = AtomicBoolean(false)
        setContent(
            state = readyState(confirmedUnit = WeightUnit.KILOGRAM),
            onStart = { started.set(true) },
            onClearUnit = { reviewRequested.set(true) }
        )

        composeRule.onNodeWithText("Unidade confirmada: kg")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Rever confirmação")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Iniciar medição")
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithText("Rever confirmação")
            .performClick()

        assertTrue(started.get())
        assertTrue(reviewRequested.get())
    }

    @Test
    fun firstMeasurementStepRemainsVisibleInDarkThemeWithLargeText() {
        setContent(
            state = readyState(confirmedUnit = null),
            themeMode = ThemeMode.DARK,
            fontScale = 1.5f
        )

        composeRule.onNodeWithText("1. Confirme a unidade da balança")
            .assertIsDisplayed()
        composeRule.onNodeWithText("O visor está em kg")
            .assertIsDisplayed()
    }

    private fun setContent(
        state: BleMeasurementUiState,
        themeMode: ThemeMode = ThemeMode.LIGHT,
        fontScale: Float = 1f,
        onStart: () -> Unit = {},
        onConfirmUnit: (WeightUnit) -> Unit = {},
        onClearUnit: () -> Unit = {}
    ) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = fontScale)
            ) {
                ControlaPesoTheme(themeMode = themeMode, dynamicColor = false) {
                    Box(Modifier.fillMaxSize().widthIn(max = 760.dp)) {
                        LiveMeasurementScreen(
                            state = state,
                            onRequestPermissions = {},
                            onStart = onStart,
                            onStop = {},
                            onProfileChange = {},
                            onConfirmUnit = onConfirmUnit,
                            onClearUnit = onClearUnit,
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
            }
        }
    }

    private fun readyState(confirmedUnit: WeightUnit?): BleMeasurementUiState {
        val profile = Profile(
            id = "profile",
            name = "Pessoa",
            avatarKey = "ocean",
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = Instant.parse("2026-07-27T12:00:00Z"),
            updatedAt = Instant.parse("2026-07-27T12:00:00Z")
        )
        return BleMeasurementUiState(
            profiles = listOf(profile),
            selectedProfileId = profile.id,
            preferences = AppPreferences(confirmedBleUnit = confirmedUnit),
            bluetoothSupport = BleSupportStatus.SUPPORTED,
            bluetoothPower = BluetoothPowerStatus.ON,
            permissionStatus = BlePermissionStatus.GRANTED,
            status = LiveMeasurementStatus.READY
        )
    }

}
