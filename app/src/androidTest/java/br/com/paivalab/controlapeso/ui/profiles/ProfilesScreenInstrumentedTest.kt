package br.com.paivalab.controlapeso.ui.profiles

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfilesScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileCardKeepsExplicitActionsAsSeparateAccessibleTargets() {
        val edited = AtomicBoolean(false)
        val profile = Profile(
            id = "profile",
            name = "Pessoa",
            avatarKey = "ocean",
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )

        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                ProfilesScreen(
                    state = ProfilesUiState(
                        profiles = listOf(profile)
                    ),
                    onCreate = {},
                    onEdit = { edited.set(true) },
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

        composeRule.onNodeWithText("Pessoa").assertIsDisplayed()
        composeRule.onNodeWithText("Editar")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()

        assertTrue(edited.get())
    }
}
