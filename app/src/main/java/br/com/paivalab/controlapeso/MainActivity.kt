package br.com.paivalab.controlapeso

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.paivalab.controlapeso.app.ControlaPesoApplication
import br.com.paivalab.controlapeso.bluetooth.BlePermissionHelper
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.ui.app.AppViewModel
import br.com.paivalab.controlapeso.ui.app.ControlaPesoApp
import br.com.paivalab.controlapeso.data.export.SharedReportFile
import br.com.paivalab.controlapeso.ui.scanner.ScannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val container
        get() = (application as ControlaPesoApplication).container
    private val appViewModel: AppViewModel by viewModels {
        AppViewModel.Factory(container)
    }
    private val scannerViewModel: ScannerViewModel by viewModels()
    private val requestedDestination = MutableStateFlow<String?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scannerViewModel.onPermissionsResult(
            permanentlyDenied = BlePermissionHelper.isPermanentlyDeniedAfterRequest(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        requestedDestination.value = intent.getStringExtra(EXTRA_DESTINATION)
        enableEdgeToEdge()
        setContent {
            val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
            val destination by requestedDestination.collectAsStateWithLifecycle()
            ControlaPesoApp(
                uiState = uiState,
                container = container,
                scannerViewModel = scannerViewModel,
                onRequestBlePermissions = ::requestBlePermissions,
                onShareText = ::shareText,
                onCopyText = ::copyText,
                onShareFile = ::shareFile,
                requestedDestination = destination,
                onDestinationConsumed = { requestedDestination.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedDestination.value = intent.getStringExtra(EXTRA_DESTINATION)
    }

    override fun onResume() {
        super.onResume()
        scannerViewModel.refreshEnvironment()
    }

    private fun requestBlePermissions() {
        if (
            scannerViewModel.uiState.value.permissionStatus ==
            BlePermissionStatus.PERMANENTLY_DENIED
        ) {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    "package:$packageName".toUri()
                )
            )
            return
        }

        permissionLauncher.launch(BlePermissionHelper.requiredRuntimePermissions())
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    private fun copyText(text: String) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(
            ClipData.newPlainText(getString(R.string.bluetooth_diagnostic), text)
        )
    }

    private fun shareFile(report: SharedReportFile) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = report.mimeType
            putExtra(Intent.EXTRA_STREAM, report.uri)
            clipData = ClipData.newUri(contentResolver, report.displayName, report.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    companion object {
        const val EXTRA_DESTINATION = "br.com.paivalab.controlapeso.DESTINATION"
        const val DESTINATION_LIVE_MEASUREMENT = "measure/live"
    }
}
