package br.com.paivalab.controlapeso.bluetooth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlePermissionManifestTest {
    @Test
    fun manifestContainsOnlyTheExpectedRuntimeBluetoothContract() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val requested = packageInfo.requestedPermissions.orEmpty().toSet()

        assertFalse(Manifest.permission.INTERNET in requested)
        assertTrue(Manifest.permission.BLUETOOTH_SCAN in requested)
        assertTrue(Manifest.permission.BLUETOOTH_CONNECT in requested)

        val runtime = BlePermissionHelper.requiredRuntimePermissions().toSet()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            assertTrue(Manifest.permission.BLUETOOTH_SCAN in runtime)
            assertTrue(Manifest.permission.BLUETOOTH_CONNECT in runtime)
            assertFalse(Manifest.permission.ACCESS_FINE_LOCATION in runtime)
        } else {
            assertTrue(Manifest.permission.BLUETOOTH in requested)
            assertTrue(Manifest.permission.BLUETOOTH_ADMIN in requested)
            assertTrue(Manifest.permission.ACCESS_FINE_LOCATION in requested)
            assertTrue(Manifest.permission.ACCESS_FINE_LOCATION in runtime)
            assertFalse(Manifest.permission.BLUETOOTH_SCAN in runtime)
        }
    }
}
