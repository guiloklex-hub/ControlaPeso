package br.com.paivalab.controlapeso

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import br.com.paivalab.controlapeso.bluetooth.BlePermissionHelper
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.ui.scanner.ScannerScreen
import br.com.paivalab.controlapeso.ui.scanner.ScannerViewModel
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme

class MainActivity : ComponentActivity() {
    private val scannerViewModel: ScannerViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scannerViewModel.onPermissionsResult(
            permanentlyDenied = BlePermissionHelper.isPermanentlyDeniedAfterRequest(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlaPesoTheme {
                val uiState by scannerViewModel.uiState.collectAsState()
                ScannerScreen(
                    uiState = uiState,
                    onRequestPermissions = ::requestBlePermissions,
                    onStartScan = scannerViewModel::startScan,
                    onStopScan = scannerViewModel::stopScan,
                    onClearResults = scannerViewModel::clearResults,
                    onDismissError = scannerViewModel::dismissError
                )
            }
        }
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
}
