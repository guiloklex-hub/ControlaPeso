package br.com.paivalab.controlapeso.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.BuildConfig
import br.com.paivalab.controlapeso.R

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.about_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body)
        }
    }
}
