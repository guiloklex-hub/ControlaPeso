package br.com.paivalab.controlapeso.data.update

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Parses only the fields needed from GitHub's public latest-release response. */
object GithubReleaseParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseStableRelease(content: String): ReleaseInfo? {
        val release = runCatching { json.decodeFromString(GithubReleaseDto.serializer(), content) }
            .getOrNull()
            ?: return null
        if (release.draft || release.preRelease) return null
        val version = SemanticVersion.parse(release.tagName) ?: return null
        if (!version.isStable) return null
        val publishedAt = release.publishedAt?.let { value ->
            runCatching { Instant.parse(value) }.getOrNull()
        }
            ?: return null
        val apk = release.assets.singleOrNull { asset ->
            asset.name.matches(UNIVERSAL_APK_NAME)
        } ?: return null
        val checksums = release.assets.singleOrNull { it.name == CHECKSUM_FILE_NAME } ?: return null
        return ReleaseInfo(
            tagName = release.tagName,
            version = version,
            publishedAt = publishedAt,
            body = release.body.orEmpty(),
            universalApk = ReleaseAsset(apk.name, apk.browserDownloadUrl),
            checksumFile = ReleaseAsset(checksums.name, checksums.browserDownloadUrl)
        )
    }

    private val UNIVERSAL_APK_NAME = Regex(".+-universal\\.apk")
    private const val CHECKSUM_FILE_NAME = "SHA256SUMS.txt"
}

@Serializable
private data class GithubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    val draft: Boolean = false,
    @SerialName("prerelease") val preRelease: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null,
    val body: String? = null,
    val assets: List<GithubReleaseAssetDto> = emptyList()
)

@Serializable
private data class GithubReleaseAssetDto(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)
