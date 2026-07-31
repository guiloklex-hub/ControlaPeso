package br.com.paivalab.controlapeso.data.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.security.MessageDigest

data class ApkPackageMetadata(
    val packageName: String,
    val versionCode: Long,
    val signerDigests: Set<String>,
    /** Android does not support signer rotation for a multiple-signer package. */
    val hasMultipleSigners: Boolean = false
)

enum class ApkVerificationFailure {
    PACKAGE,
    SIGNATURE,
    VERSION,
    ARCHIVE
}

sealed interface ApkVerificationResult {
    data object Valid : ApkVerificationResult
    data class Invalid(val reason: ApkVerificationFailure) : ApkVerificationResult
}

/** Pure policy so package, signer and version protections are JVM-testable. */
object ApkVerificationPolicy {
    fun verify(
        expectedApplicationId: String,
        archive: ApkPackageMetadata?,
        installed: ApkPackageMetadata?
    ): ApkVerificationResult {
        if (archive == null || installed == null) {
            return ApkVerificationResult.Invalid(ApkVerificationFailure.ARCHIVE)
        }
        if (archive.packageName != expectedApplicationId ||
            installed.packageName != expectedApplicationId
        ) {
            return ApkVerificationResult.Invalid(ApkVerificationFailure.PACKAGE)
        }
        if (archive.versionCode <= installed.versionCode) {
            return ApkVerificationResult.Invalid(ApkVerificationFailure.VERSION)
        }
        if (!signersAreCompatible(archive, installed)) {
            return ApkVerificationResult.Invalid(ApkVerificationFailure.SIGNATURE)
        }
        return ApkVerificationResult.Valid
    }

    private fun signersAreCompatible(
        archive: ApkPackageMetadata,
        installed: ApkPackageMetadata
    ): Boolean {
        if (archive.signerDigests.isEmpty() || installed.signerDigests.isEmpty()) return false
        if (archive.hasMultipleSigners || installed.hasMultipleSigners) {
            return archive.hasMultipleSigners == installed.hasMultipleSigners &&
                archive.signerDigests == installed.signerDigests
        }
        // A single signer can rotate its certificate. Android reports its signing
        // history, so one shared digest represents a compatible lineage.
        return archive.signerDigests.intersect(installed.signerDigests).isNotEmpty()
    }
}

interface ApkArchiveVerifier {
    fun verify(file: File): ApkVerificationResult
}

class AndroidApkArchiveVerifier(
    private val context: Context,
    private val expectedApplicationId: String
) : ApkArchiveVerifier {
    override fun verify(file: File): ApkVerificationResult {
        val packageManager = context.packageManager
        val archive = packageManager.getArchivePackageInfo(file.absolutePath)
        val installed = runCatching {
            packageManager.getInstalledPackageInfo(expectedApplicationId)
        }.getOrNull()
        return ApkVerificationPolicy.verify(
            expectedApplicationId = expectedApplicationId,
            archive = archive?.toMetadata(),
            installed = installed?.toMetadata()
        )
    }

    @Suppress("DEPRECATION")
    private fun PackageManager.getArchivePackageInfo(path: String): PackageInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageArchiveInfo(
                path,
                PackageManager.PackageInfoFlags.of(
                    PackageManager.GET_SIGNING_CERTIFICATES.toLong()
                )
            )
        } else {
            getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES)
        }

    @Suppress("DEPRECATION")
    private fun PackageManager.getInstalledPackageInfo(packageName: String): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(
                    PackageManager.GET_SIGNING_CERTIFICATES.toLong()
                )
            )
        } else {
            getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }

    @Suppress("DEPRECATION")
    private fun PackageInfo.toMetadata(): ApkPackageMetadata = ApkPackageMetadata(
        packageName = packageName,
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            versionCode.toLong()
        },
        signerDigests = signingCertificates().mapTo(linkedSetOf(), Signature::sha256),
        hasMultipleSigners = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            signingInfo?.hasMultipleSigners() == true
    )

    @Suppress("DEPRECATION")
    private fun PackageInfo.signingCertificates(): Array<Signature> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingCertificatesApi28()
        } else {
            signatures ?: emptyArray()
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun PackageInfo.signingCertificatesApi28(): Array<Signature> {
        val info = signingInfo ?: return emptyArray()
        return if (info.hasMultipleSigners()) {
            info.apkContentsSigners
        } else {
            info.signingCertificateHistory ?: info.apkContentsSigners
        }
    }
}

private fun Signature.sha256(): String = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray())
    .joinToString(separator = "") { byte -> "%02x".format(byte) }
