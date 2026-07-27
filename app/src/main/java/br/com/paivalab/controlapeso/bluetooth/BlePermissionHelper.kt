package br.com.paivalab.controlapeso.bluetooth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object BlePermissionHelper {
    fun requiredRuntimePermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    fun missingPermissions(context: Context): List<String> =
        requiredRuntimePermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) !=
                PackageManager.PERMISSION_GRANTED
        }

    fun hasRequiredPermissions(context: Context): Boolean =
        missingPermissions(context).isEmpty()

    fun hasConnectPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

    /**
     * Deve ser chamado somente depois que uma solicitação de permissões retornou.
     * Antes da primeira solicitação, shouldShowRequestPermissionRationale também
     * retorna false e não distingue o estado inicial de uma negação permanente.
     */
    fun isPermanentlyDeniedAfterRequest(activity: Activity): Boolean =
        missingPermissions(activity).any { permission ->
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
}
