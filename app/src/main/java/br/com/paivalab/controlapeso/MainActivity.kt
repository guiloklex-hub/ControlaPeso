package br.com.paivalab.controlapeso

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageInstaller
import android.os.Build
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
import androidx.lifecycle.lifecycleScope
import br.com.paivalab.controlapeso.app.ControlaPesoApplication
import br.com.paivalab.controlapeso.bluetooth.BlePermissionHelper
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.ui.app.AppViewModel
import br.com.paivalab.controlapeso.ui.app.ControlaPesoApp
import br.com.paivalab.controlapeso.data.export.SharedReportFile
import br.com.paivalab.controlapeso.ui.scanner.ScannerViewModel
import br.com.paivalab.controlapeso.ui.update.ReleaseUpdateEvent
import br.com.paivalab.controlapeso.ui.update.ReleaseUpdateViewModel
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val container
        get() = (application as ControlaPesoApplication).container
    private val appViewModel: AppViewModel by viewModels {
        AppViewModel.Factory(container)
    }
    private val releaseUpdateViewModel: ReleaseUpdateViewModel by viewModels {
        ReleaseUpdateViewModel.Factory(container)
    }
    private val scannerViewModel: ScannerViewModel by viewModels()
    private val requestedDestination = MutableStateFlow<String?>(null)
    private var pendingUpdateApk: File? = null
    private var activePackageInstallSessionId: Int? = null
    private var activePackageInstallNonce: String? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scannerViewModel.onPermissionsResult(
            permanentlyDenied = BlePermissionHelper.isPermanentlyDeniedAfterRequest(this)
        )
    }

    private val unknownSourcesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        resumePendingUpdateInstallation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        activePackageInstallSessionId = savedInstanceState?.getInt(EXTRA_INSTALL_SESSION_ID)
            ?.takeIf { it >= 0 }
        activePackageInstallNonce = savedInstanceState?.getString(EXTRA_INSTALL_NONCE)
        requestedDestination.value = intent.getStringExtra(EXTRA_DESTINATION)
        enableEdgeToEdge()
        observeActivityOwnedFlows()
        setContent {
            val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
            val updateState by releaseUpdateViewModel.uiState.collectAsStateWithLifecycle()
            val destination by requestedDestination.collectAsStateWithLifecycle()
            ControlaPesoApp(
                uiState = uiState,
                container = container,
                scannerViewModel = scannerViewModel,
                onRequestBlePermissions = ::requestBlePermissions,
                onShareText = ::shareText,
                onCopyText = ::copyText,
                onShareFile = ::shareFile,
                updateState = updateState,
                onCheckForUpdates = releaseUpdateViewModel::checkNow,
                onViewReleaseNotes = releaseUpdateViewModel::showReleaseDetails,
                onDismissUpdate = releaseUpdateViewModel::dismissForSession,
                onDownloadAndInstallUpdate = releaseUpdateViewModel::downloadAndInstall,
                onDismissUpdateInstallFeedback =
                    releaseUpdateViewModel::dismissInstallFeedback,
                requestedDestination = destination,
                onDestinationConsumed = { requestedDestination.value = null }
            )
        }
        handlePackageInstallerResult(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedDestination.value = intent.getStringExtra(EXTRA_DESTINATION)
        handlePackageInstallerResult(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        activePackageInstallSessionId?.let {
            outState.putInt(EXTRA_INSTALL_SESSION_ID, it)
        }
        activePackageInstallNonce?.let {
            outState.putString(EXTRA_INSTALL_NONCE, it)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        scannerViewModel.refreshEnvironment()
        resumePendingUpdateInstallation()
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

    private fun observeActivityOwnedFlows() {
        lifecycleScope.launch {
            releaseUpdateViewModel.events.collect { event ->
                when (event) {
                    is ReleaseUpdateEvent.StartPackageInstallation -> {
                        requestPackageInstallation(event.apkFile)
                    }
                }
            }
        }
    }

    private fun requestPackageInstallation(apkFile: File) {
        if (!apkFile.isFile) {
            releaseUpdateViewModel.onInstallationFinished(success = false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !packageManager.canRequestPackageInstalls()
        ) {
            pendingUpdateApk = apkFile
            releaseUpdateViewModel.onUnknownSourcesRequired()
            unknownSourcesLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    "package:$packageName".toUri()
                )
            )
            return
        }
        installVerifiedApk(apkFile)
    }

    private fun resumePendingUpdateInstallation() {
        val apkFile = pendingUpdateApk ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !packageManager.canRequestPackageInstalls()
        ) {
            releaseUpdateViewModel.onUnknownSourcesRequired()
            return
        }
        pendingUpdateApk = null
        installVerifiedApk(apkFile)
    }

    private fun installVerifiedApk(apkFile: File) {
        val installer = packageManager.packageInstaller
        var sessionId = -1
        try {
            val parameters = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            ).apply {
                setAppPackageName(packageName)
            }
            sessionId = installer.createSession(parameters)
            val callbackNonce = UUID.randomUUID().toString()
            activePackageInstallSessionId = sessionId
            activePackageInstallNonce = callbackNonce
            installer.openSession(sessionId).use { session ->
                apkFile.inputStream().buffered().use { input ->
                    session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }
                val callbackIntent = Intent(this, MainActivity::class.java).apply {
                    action = ACTION_PACKAGE_INSTALL_RESULT
                    putExtra(EXTRA_INSTALL_SESSION_ID, sessionId)
                    putExtra(EXTRA_INSTALL_NONCE, callbackNonce)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                val callback = PendingIntent.getActivity(
                    this,
                    sessionId,
                    callbackIntent,
                    // PackageInstaller must fill the status extras into this sender on
                    // targetSdk 35+; the received callback is still checked below.
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            PendingIntent.FLAG_MUTABLE
                        } else {
                            0
                        }
                )
                session.commit(callback.intentSender)
            }
            releaseUpdateViewModel.onInstallationStarted()
        } catch (_: Exception) {
            if (sessionId >= 0) installer.abandonSession(sessionId)
            activePackageInstallSessionId = null
            activePackageInstallNonce = null
            releaseUpdateViewModel.onInstallationFinished(success = false)
        }
    }

    @SuppressLint("UnsafeIntentLaunch")
    private fun handlePackageInstallerResult(intent: Intent?) {
        if (intent?.action != ACTION_PACKAGE_INSTALL_RESULT) return
        val expectedSessionId = activePackageInstallSessionId ?: return
        val expectedNonce = activePackageInstallNonce ?: return
        if (intent.getIntExtra(EXTRA_INSTALL_SESSION_ID, -1) != expectedSessionId ||
            intent.getStringExtra(EXTRA_INSTALL_NONCE) != expectedNonce
        ) {
            return
        }
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION")
                val confirmationIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                if (confirmationIntent == null) {
                    finishPackageInstallation(success = false)
                } else if (!isTrustedPackageInstallerIntent(confirmationIntent)) {
                    finishPackageInstallation(success = false)
                } else {
                    val resolved = packageManager.resolveActivity(
                        confirmationIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                    val explicitIntent = Intent(confirmationIntent).setComponent(
                        ComponentName(
                            requireNotNull(resolved).activityInfo.packageName,
                            resolved.activityInfo.name
                        )
                    )
                    startActivity(explicitIntent)
                }
            }
            PackageInstaller.STATUS_SUCCESS -> finishPackageInstallation(success = true)
            else -> finishPackageInstallation(success = false)
        }
    }

    private fun finishPackageInstallation(success: Boolean) {
        activePackageInstallSessionId = null
        activePackageInstallNonce = null
        releaseUpdateViewModel.onInstallationFinished(success)
    }

    private fun isTrustedPackageInstallerIntent(intent: Intent): Boolean {
        val resolved = packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ) ?: return false
        val activityInfo = resolved.activityInfo ?: return false
        val applicationInfo = activityInfo.applicationInfo ?: return false
        return activityInfo.packageName != packageName &&
            applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    companion object {
        const val EXTRA_DESTINATION = "br.com.paivalab.controlapeso.DESTINATION"
        const val DESTINATION_LIVE_MEASUREMENT = "measure/live"
        private const val ACTION_PACKAGE_INSTALL_RESULT =
            "br.com.paivalab.controlapeso.PACKAGE_INSTALL_RESULT"
        private const val EXTRA_INSTALL_SESSION_ID =
            "br.com.paivalab.controlapeso.INSTALL_SESSION_ID"
        private const val EXTRA_INSTALL_NONCE =
            "br.com.paivalab.controlapeso.INSTALL_NONCE"
    }
}
