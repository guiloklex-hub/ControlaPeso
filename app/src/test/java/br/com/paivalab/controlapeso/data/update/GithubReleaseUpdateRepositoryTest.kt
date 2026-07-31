package br.com.paivalab.controlapeso.data.update

import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GithubReleaseUpdateRepositoryTest {
    @Test
    fun `uses ETag and reuses cached stable release after 304`() = runTest {
        val metadata = MemoryMetadataStore(
            etag = "known-etag",
            cachedRelease = releaseJson()
        )
        val client = FakeHttpClient(GithubLatestReleaseResponse(304, null, null))
        val repository = repository(metadata = metadata, client = client)

        assertTrue(repository.checkForUpdate("1.0.0") is ReleaseCheckResult.UpdateAvailable)
        assertEquals("known-etag", client.receivedEtag)
    }

    @Test
    fun `does not send a stale ETag without a cached release body`() = runTest {
        val metadata = MemoryMetadataStore(etag = "known-etag")
        val client = FakeHttpClient(GithubLatestReleaseResponse(304, null, null))
        val repository = repository(metadata = metadata, client = client)

        assertEquals(ReleaseCheckResult.NoUpdate, repository.checkForUpdate("1.0.0"))
        assertEquals(null, client.receivedEtag)
    }

    @Test
    fun `caches only a validated response together with its ETag`() = runTest {
        val metadata = MemoryMetadataStore()
        val body = releaseJson()
        val repository = repository(
            metadata = metadata,
            client = FakeHttpClient(GithubLatestReleaseResponse(200, "fresh-etag", body))
        )

        assertTrue(repository.checkForUpdate("1.0.0") is ReleaseCheckResult.UpdateAvailable)
        assertEquals("fresh-etag", metadata.getEtag())
        assertEquals(body, metadata.getCachedRelease())
    }

    @Test
    fun `keeps the previous validated cache when a response is invalid`() = runTest {
        val previous = releaseJson()
        val metadata = MemoryMetadataStore(etag = "known-etag", cachedRelease = previous)
        val repository = repository(
            metadata = metadata,
            client = FakeHttpClient(
                GithubLatestReleaseResponse(200, "bad-etag", "{\"draft\":true}")
            )
        )

        assertEquals(ReleaseCheckResult.NoUpdate, repository.checkForUpdate("1.0.0"))
        assertEquals("known-etag", metadata.getEtag())
        assertEquals(previous, metadata.getCachedRelease())
    }

    @Test
    fun `offline check fails without throwing`() = runTest {
        val repository = repository(client = object : GithubReleaseHttpClient {
            override suspend fun getLatestRelease(ifNoneMatch: String?): GithubLatestReleaseResponse {
                throw IOException("offline")
            }
        })

        assertTrue(repository.checkForUpdate("1.0.0") is ReleaseCheckResult.Failure)
    }

    @Test
    fun `cancellation from a release check is not converted into an offline failure`() =
        runTest {
            val repository = repository(client = object : GithubReleaseHttpClient {
                override suspend fun getLatestRelease(
                    ifNoneMatch: String?
                ): GithubLatestReleaseResponse {
                    throw CancellationException("cancelled")
                }
            })

            var cancelled = false
            try {
                repository.checkForUpdate("1.0.0")
            } catch (_: CancellationException) {
                cancelled = true
            }
            assertTrue(cancelled)
        }

    @Test
    fun `downloads only when checksum and APK verification pass`() = runTest {
        val bytes = "verified APK bytes".toByteArray()
        val hash = sha256(bytes)
        val downloader = FakeDownloader(
            checksum = "$hash  ControlaPeso-v1.2.0-universal.apk\n",
            bytes = bytes
        )
        val temp = Files.createTempDirectory("controlapeso-update-test").toFile()
        val repository = repository(
            downloader = downloader,
            verifier = object : ApkArchiveVerifier {
                override fun verify(file: File): ApkVerificationResult = ApkVerificationResult.Valid
            },
            directory = temp
        )

        val result = repository.downloadUpdate(release())

        assertTrue(result is UpdateDownloadResult.Success)
        assertTrue((result as UpdateDownloadResult.Success).apkFile.isFile)
    }

    @Test
    fun `rejects bad checksum before APK installation`() = runTest {
        val repository = repository(
            downloader = FakeDownloader(
                checksum = "${"0".repeat(64)}  ControlaPeso-v1.2.0-universal.apk\n",
                bytes = "tampered".toByteArray()
            )
        )

        val result = repository.downloadUpdate(release())

        assertEquals(
            UpdateDownloadResult.Failure(UpdateDownloadFailure.CHECKSUM_MISMATCH),
            result
        )
    }

    private fun repository(
        metadata: ReleaseUpdateMetadataStore = MemoryMetadataStore(),
        client: GithubReleaseHttpClient = FakeHttpClient(
            GithubLatestReleaseResponse(404, null, null)
        ),
        downloader: GithubAssetDownloader = FakeDownloader("", ByteArray(0)),
        verifier: ApkArchiveVerifier = object : ApkArchiveVerifier {
            override fun verify(file: File): ApkVerificationResult = ApkVerificationResult.Valid
        },
        directory: File = Files.createTempDirectory("controlapeso-update-test").toFile()
    ) = GithubReleaseUpdateRepository(metadata, client, downloader, verifier, directory)

    private fun release() = ReleaseInfo(
        tagName = "v1.2.0",
        version = requireNotNull(SemanticVersion.parse("1.2.0")),
        publishedAt = java.time.Instant.parse("2026-07-28T12:00:00Z"),
        body = "",
        universalApk = ReleaseAsset("ControlaPeso-v1.2.0-universal.apk", "apk"),
        checksumFile = ReleaseAsset("SHA256SUMS.txt", "sums")
    )

    private fun releaseJson(): String = """
        {
          "tag_name":"v1.2.0",
          "draft":false,
          "prerelease":false,
          "published_at":"2026-07-28T12:00:00Z",
          "assets":[
            {"name":"ControlaPeso-v1.2.0-universal.apk","browser_download_url":"https://example.test/app.apk"},
            {"name":"SHA256SUMS.txt","browser_download_url":"https://example.test/sums"}
          ]
        }
    """.trimIndent()

    private class MemoryMetadataStore(
        private var etag: String? = null,
        private var cachedRelease: String? = null
    ) : ReleaseUpdateMetadataStore {
        override suspend fun getEtag(): String? = etag
        override suspend fun getCachedRelease(): String? = cachedRelease
        override suspend fun setCachedRelease(eTag: String?, releaseJson: String?) {
            etag = eTag
            cachedRelease = releaseJson
        }
    }

    private class FakeHttpClient(
        private val response: GithubLatestReleaseResponse
    ) : GithubReleaseHttpClient {
        var receivedEtag: String? = null
        override suspend fun getLatestRelease(ifNoneMatch: String?): GithubLatestReleaseResponse {
            receivedEtag = ifNoneMatch
            return response
        }
    }

    private class FakeDownloader(
        private val checksum: String,
        private val bytes: ByteArray
    ) : GithubAssetDownloader {
        override suspend fun downloadText(url: String): String = checksum
        override suspend fun downloadFile(url: String, destination: File) {
            destination.writeBytes(bytes)
        }
    }
}

private fun sha256(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString("") { "%02x".format(it) }
