package br.com.paivalab.controlapeso.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.paivalab.controlapeso.BuildConfig
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.components.ResponsiveScreenList

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    ResponsiveScreenList(modifier = modifier) {
        item {
            Text(
                stringResource(R.string.about_title),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        item {
            AboutCard(
                stringResource(R.string.app_name),
                stringResource(
                    R.string.about_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                    BuildConfig.BUILD_TYPE
                )
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_privacy_heading),
                stringResource(R.string.about_privacy_body)
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_protocol_heading),
                stringResource(R.string.about_protocol_body)
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_medical_heading),
                stringResource(R.string.about_medical_body)
            )
        }
        item {
            AboutCard(
                stringResource(R.string.about_licenses_heading),
                stringResource(R.string.about_licenses_body)
            )
        }
    }
}

@Composable
private fun AboutCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(ControlaPesoDesignSystem.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.xs
            )
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body)
        }
    }
}
