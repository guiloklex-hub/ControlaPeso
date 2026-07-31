package br.com.paivalab.controlapeso.data.update

import java.io.File
import java.time.Instant

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String
)

data class ReleaseInfo(
    val tagName: String,
    val version: SemanticVersion,
    val publishedAt: Instant,
    val body: String,
    val universalApk: ReleaseAsset,
    val checksumFile: ReleaseAsset
)

sealed interface ReleaseCheckResult {
    data object NoUpdate : ReleaseCheckResult
    data class UpdateAvailable(val release: ReleaseInfo) : ReleaseCheckResult
    data class Failure(val cause: Throwable) : ReleaseCheckResult
}

enum class UpdateDownloadFailure {
    NETWORK,
    CHECKSUM_NOT_FOUND,
    CHECKSUM_MISMATCH,
    APK_PACKAGE_INVALID,
    APK_SIGNATURE_INVALID,
    APK_VERSION_INVALID,
    APK_INVALID,
    STORAGE
}

sealed interface UpdateDownloadResult {
    data class Success(val apkFile: File) : UpdateDownloadResult
    data class Failure(val reason: UpdateDownloadFailure) : UpdateDownloadResult
}

interface ReleaseUpdateRepository {
    suspend fun checkForUpdate(installedVersionName: String): ReleaseCheckResult
    suspend fun downloadUpdate(release: ReleaseInfo): UpdateDownloadResult
}
