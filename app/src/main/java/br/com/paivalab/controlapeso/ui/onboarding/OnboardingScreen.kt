package br.com.paivalab.controlapeso.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.domain.model.WeightUnit

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
    onRequestBlePermissions: () -> Unit,
    onNameChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    onTargetChange: (String) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            progress = { (state.step + 1f) / state.totalSteps },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(
                R.string.onboarding_step,
                state.step + 1,
                state.totalSteps
            ),
            style = MaterialTheme.typography.labelLarge
        )

        when (state.step) {
            0 -> IntroStep(
                title = stringResource(R.string.onboarding_welcome_title),
                body = stringResource(R.string.onboarding_welcome_body)
            )
            1 -> IntroStep(
                title = stringResource(R.string.onboarding_local_title),
                body = stringResource(R.string.onboarding_local_body)
            )
            2 -> PermissionStep(onRequestBlePermissions)
            3 -> ProfileStep(
                state,
                onNameChange,
                onHeightChange,
                onBirthDateChange,
                onUnitChange
            )
            4 -> TargetStep(state, onTargetChange)
            5 -> ThemeStep(state.themeMode, onThemeChange)
            6 -> IntroStep(
                title = stringResource(R.string.onboarding_scale_title),
                body = stringResource(R.string.onboarding_scale_body)
            )
            else -> IntroStep(
                title = stringResource(R.string.onboarding_health_title),
                body = stringResource(R.string.onboarding_health_body)
            )
        }

        state.error?.let {
            Text(
                text = onboardingErrorText(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.weight(1f, fill = false))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.step > 0) {
                OutlinedButton(onClick = onPrevious, enabled = !state.isSaving) {
                    Text(stringResource(R.string.back))
                }
            } else {
                TextButton(onClick = onSkip, enabled = !state.isSaving) {
                    Text(stringResource(R.string.skip_onboarding))
                }
            }
            Button(
                onClick = if (state.step == state.totalSteps - 1) onFinish else onNext,
                enabled = !state.isSaving
            ) {
                Text(
                    stringResource(
                        if (state.step == state.totalSteps - 1) {
                            R.string.finish
                        } else {
                            R.string.next
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun onboardingErrorText(error: OnboardingError): String = stringResource(
    when (error) {
        OnboardingError.NAME_TOO_LONG -> R.string.error_profile_name_long
        OnboardingError.INVALID_HEIGHT -> R.string.error_profile_height
        OnboardingError.INVALID_TARGET -> R.string.error_goal_target
        OnboardingError.INVALID_BIRTH_DATE -> R.string.error_profile_birth_date
        OnboardingError.FUTURE_BIRTH_DATE -> R.string.error_profile_future_birth
        OnboardingError.SAVE_FAILED -> R.string.error_onboarding_save
    }
)

@Composable
private fun IntroStep(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PermissionStep(onRequest: () -> Unit) {
    IntroStep(
        title = stringResource(R.string.onboarding_ble_title),
        body = stringResource(R.string.onboarding_ble_body)
    )
    OutlinedButton(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.request_permissions))
    }
}

@Composable
private fun ProfileStep(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onUnitChange: (WeightUnit) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.onboarding_profile_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            stringResource(R.string.onboarding_profile_optional),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = state.profileName,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.profile_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.heightText,
            onValueChange = onHeightChange,
            label = { Text(stringResource(R.string.height_cm_optional)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.birthDateText,
            onValueChange = onBirthDateChange,
            label = { Text(stringResource(R.string.birth_date_optional)) },
            supportingText = { Text(stringResource(R.string.iso_date_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        UnitSelector(state.unit, onUnitChange)
    }
}

@Composable
private fun UnitSelector(selected: WeightUnit, onSelected: (WeightUnit) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WeightUnit.entries.forEach { unit ->
            FilterChip(
                selected = selected == unit,
                onClick = { onSelected(unit) },
                label = { Text(unit.symbol) }
            )
        }
    }
}

@Composable
private fun TargetStep(state: OnboardingUiState, onTargetChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.onboarding_goal_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            stringResource(R.string.onboarding_goal_body),
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedTextField(
            value = state.targetWeightText,
            onValueChange = onTargetChange,
            label = {
                Text(stringResource(R.string.target_weight_optional, state.unit.symbol))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ThemeStep(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.onboarding_theme_title),
            style = MaterialTheme.typography.headlineSmall
        )
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                label = {
                    Text(
                        stringResource(
                            when (mode) {
                                ThemeMode.SYSTEM -> R.string.theme_system
                                ThemeMode.LIGHT -> R.string.theme_light
                                ThemeMode.DARK -> R.string.theme_dark
                            }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
