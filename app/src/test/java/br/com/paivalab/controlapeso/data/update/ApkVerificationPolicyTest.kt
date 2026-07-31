package br.com.paivalab.controlapeso.data.update

import org.junit.Assert.assertEquals
import org.junit.Test

class ApkVerificationPolicyTest {
    private val installed = ApkPackageMetadata(
        packageName = "br.com.paivalab.controlapeso",
        versionCode = 10,
        signerDigests = setOf("release-certificate")
    )

    @Test
    fun `accepts newer APK with compatible application id and signer`() {
        assertEquals(
            ApkVerificationResult.Valid,
            ApkVerificationPolicy.verify(
                expectedApplicationId = installed.packageName,
                archive = installed.copy(versionCode = 11),
                installed = installed
            )
        )
    }

    @Test
    fun `rejects another package signer and non newer version`() {
        assertEquals(
            ApkVerificationResult.Invalid(ApkVerificationFailure.PACKAGE),
            ApkVerificationPolicy.verify(
                installed.packageName,
                installed.copy(packageName = "other.package", versionCode = 11),
                installed
            )
        )
        assertEquals(
            ApkVerificationResult.Invalid(ApkVerificationFailure.SIGNATURE),
            ApkVerificationPolicy.verify(
                installed.packageName,
                installed.copy(versionCode = 11, signerDigests = setOf("other-certificate")),
                installed
            )
        )
        assertEquals(
            ApkVerificationResult.Invalid(ApkVerificationFailure.VERSION),
            ApkVerificationPolicy.verify(installed.packageName, installed, installed)
        )
    }

    @Test
    fun `requires the complete signer set for multiple signer packages`() {
        val multiSignerInstalled = installed.copy(
            signerDigests = setOf("certificate-a", "certificate-b"),
            hasMultipleSigners = true
        )

        assertEquals(
            ApkVerificationResult.Invalid(ApkVerificationFailure.SIGNATURE),
            ApkVerificationPolicy.verify(
                expectedApplicationId = installed.packageName,
                archive = multiSignerInstalled.copy(
                    versionCode = 11,
                    signerDigests = setOf("certificate-a")
                ),
                installed = multiSignerInstalled
            )
        )
    }
}
