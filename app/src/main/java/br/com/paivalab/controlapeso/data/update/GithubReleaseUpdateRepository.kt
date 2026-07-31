package br.com.paivalab.controlapeso.data.update

import br.com.paivalab.controlapeso.data.backup.LimitedTextReader
import java.io.File
import java.io.IOException
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

data class GithubLatestReleaseResponse(
    val statusCode: Int,
    val eTag: String?,
    val body: String?
)

interface GithubReleaseHttpClient {
    suspend fun getLatestRelease(ifNoneMatch: String?): GithubLatestReleaseResponse
}

interface GithubAssetDownloader {
    suspend fun downloadText(url: String): String
    suspend fun downloadFile(url: String, destination: File)
}

interface ReleaseUpdateMetadataStore {
    suspend fun getEtag(): String?
    suspend fun getCachedRelease(): String?
    suspend fun setCachedRelease(eTag: String?, releaseJson: String?)
}

class GithubReleaseUpdateRepository(
    private val metadataStore: ReleaseUpdateMetadataStore,
    private val httpClient: GithubReleaseHttpClient,
    private val assetDownloader: GithubAssetDownloader,
    private val apkVerifier: ApkArchiveVerifier,
    private val cacheDirectory: File
) : ReleaseUpdateRepository {
    override suspend fun checkForUpdate(installedVersionName: String): ReleaseCheckResult {
        val installed = SemanticVersion.parse(installedVersionName) ?: return ReleaseCheckResult.NoUpdate
        return try {
            val cachedRelease = metadataStore.getCachedRelease()
            val response = httpClient.getLatestRelease(
                ifNoneMatch = metadataStore.getEtag().takeIf { cachedRelease != null }
            )
            when (response.statusCode) {
                HttpsURLConnection.HTTP_NOT_MODIFIED -> cachedRelease
                    ?.let(GithubReleaseParser::parseStableRelease)
                    ?.takeIf { it.version > installed }
                    ?.let(ReleaseCheckResult::UpdateAvailable)
                    ?: ReleaseCheckResult.NoUpdate
                HttpsURLConnection.HTTP_NOT_FOUND -> ReleaseCheckResult.NoUpdate
                HttpsURLConnection.HTTP_OK -> {
                    val body = response.body
                    val release = body?.let(GithubReleaseParser::parseStableRelease)
                    release ?: return ReleaseCheckResult.NoUpdate
                    metadataStore.setCachedRelease(
                        eTag = response.eTag,
                        releaseJson = body
                    )
                    if (release.version > installed) {
                        ReleaseCheckResult.UpdateAvailable(release)
                    } else {
                        ReleaseCheckResult.NoUpdate
                    }
                }
                else -> ReleaseCheckResult.Failure(
                    IOException("GitHub respondeu HTTP ${response.statusCode}")
                )
            }
        } catch (failure: Throwable) {
            if (failure is CancellationException) throw failure
            ReleaseCheckResult.Failure(failure)
        }
    }

    override suspend fun downloadUpdate(release: ReleaseInfo): UpdateDownloadResult =
        withContext(Dispatchers.IO) {
            val expectedHash = try {
                Sha256Sums.findHashFor(
                    content = assetDownloader.downloadText(release.checksumFile.downloadUrl),
                    fileName = release.universalApk.name
                )
            } catch (_: IOException) {
                return@withContext UpdateDownloadResult.Failure(UpdateDownloadFailure.NETWORK)
            } catch (failure: Exception) {
                if (failure is CancellationException) throw failure
                return@withContext UpdateDownloadResult.Failure(UpdateDownloadFailure.CHECKSUM_NOT_FOUND)
            } ?: return@withContext UpdateDownloadResult.Failure(
                UpdateDownloadFailure.CHECKSUM_NOT_FOUND
            )
            if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) {
                return@withContext UpdateDownloadResult.Failure(UpdateDownloadFailure.STORAGE)
            }
            val temporary = File(cacheDirectory, "${release.version}-${System.nanoTime()}.part")
            val destination = File(cacheDirectory, "${release.version}-${System.nanoTime()}.apk")
            try {
                assetDownloader.downloadFile(release.universalApk.downloadUrl, temporary)
                val actualHash = temporary.sha256()
                if (!actualHash.equals(expectedHash, ignoreCase = true)) {
                    return@withContext UpdateDownloadResult.Failure(
                        UpdateDownloadFailure.CHECKSUM_MISMATCH
                    )
                }
                when (val verification = apkVerifier.verify(temporary)) {
                    ApkVerificationResult.Valid -> Unit
                    is ApkVerificationResult.Invalid -> {
                        return@withContext UpdateDownloadResult.Failure(
                            verification.reason.toDownloadFailure()
                        )
                    }
                }
                if (!temporary.renameTo(destination)) {
                    return@withContext UpdateDownloadResult.Failure(UpdateDownloadFailure.STORAGE)
                }
                UpdateDownloadResult.Success(destination)
            } catch (_: IOException) {
                UpdateDownloadResult.Failure(UpdateDownloadFailure.NETWORK)
            } catch (_: SecurityException) {
                UpdateDownloadResult.Failure(UpdateDownloadFailure.STORAGE)
            } catch (failure: Exception) {
                if (failure is CancellationException) throw failure
                UpdateDownloadResult.Failure(UpdateDownloadFailure.APK_INVALID)
            } finally {
                if (temporary.exists()) temporary.delete()
            }
        }
}

object Sha256Sums {
    fun findHashFor(content: String, fileName: String): String? = content
        .lineSequence()
        .mapNotNull { line -> parseLine(line) }
        .filter { (_, listedName) -> listedName == fileName }
        .map { (hash) -> hash }
        .toList()
        .singleOrNull()

    private fun parseLine(line: String): Pair<String, String>? {
        val match = LINE.matchEntire(line.trim()) ?: return null
        val hash = match.groupValues[1]
        val name = match.groupValues[2].removePrefix("*")
        return hash to name
    }

    private val LINE = Regex("^([A-Fa-f0-9]{64})\\s+(.+)$")
}

class HttpsGithubReleaseHttpClient : GithubReleaseHttpClient {
    override suspend fun getLatestRelease(ifNoneMatch: String?): GithubLatestReleaseResponse =
        withContext(Dispatchers.IO) {
            val connection = openConnection(LATEST_RELEASE_URL)
            try {
                ifNoneMatch?.let { connection.setRequestProperty("If-None-Match", it) }
                val status = connection.responseCode
                val body = if (status == HttpsURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                        LimitedTextReader.read(it, MAX_RELEASE_RESPONSE_CHARACTERS)
                    }
                } else {
                    null
                }
                GithubLatestReleaseResponse(status, connection.getHeaderField("ETag"), body)
            } finally {
                connection.disconnect()
            }
        }

    private fun openConnection(url: String): HttpsURLConnection =
        (URL(url).openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MILLIS
            readTimeout = TIMEOUT_MILLIS
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", USER_AGENT)
        }

    private companion object {
        const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/guiloklex-hub/ControlaPeso/releases/latest"
        const val USER_AGENT = "ControlaPeso-Android"
        const val TIMEOUT_MILLIS = 15_000
        const val MAX_RELEASE_RESPONSE_CHARACTERS = 1_024 * 1_024
    }
}

class HttpsGithubAssetDownloader : GithubAssetDownloader {
    override suspend fun downloadText(url: String): String = withContext(Dispatchers.IO) {
        openConnection(url).useInput { input ->
            input.bufferedReader(Charsets.UTF_8).use {
                LimitedTextReader.read(it, MAX_CHECKSUM_CHARACTERS)
            }
        }
    }

    override suspend fun downloadFile(url: String, destination: File) {
        withContext(Dispatchers.IO) {
            openConnection(url).useInput { input ->
                destination.outputStream().buffered().use { output -> input.copyTo(output) }
            }
        }
    }

    private fun openConnection(url: String): HttpsURLConnection =
        (URL(url).openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MILLIS
            readTimeout = TIMEOUT_MILLIS
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/octet-stream")
            setRequestProperty("User-Agent", USER_AGENT)
        }

    private inline fun <T> HttpsURLConnection.useInput(block: (java.io.InputStream) -> T): T {
        try {
            val status = responseCode
            if (status !in 200..299) throw IOException("Download respondeu HTTP $status")
            return inputStream.use(block)
        } finally {
            disconnect()
        }
    }

    private companion object {
        const val USER_AGENT = "ControlaPeso-Android"
        const val TIMEOUT_MILLIS = 30_000
        const val MAX_CHECKSUM_CHARACTERS = 256 * 1024
    }
}

private fun File.sha256(): String = inputStream().buffered().use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val bytesRead = input.read(buffer)
        if (bytesRead < 0) break
        digest.update(buffer, 0, bytesRead)
    }
    digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
}

private fun ApkVerificationFailure.toDownloadFailure(): UpdateDownloadFailure = when (this) {
    ApkVerificationFailure.PACKAGE -> UpdateDownloadFailure.APK_PACKAGE_INVALID
    ApkVerificationFailure.SIGNATURE -> UpdateDownloadFailure.APK_SIGNATURE_INVALID
    ApkVerificationFailure.VERSION -> UpdateDownloadFailure.APK_VERSION_INVALID
    ApkVerificationFailure.ARCHIVE -> UpdateDownloadFailure.APK_INVALID
}
