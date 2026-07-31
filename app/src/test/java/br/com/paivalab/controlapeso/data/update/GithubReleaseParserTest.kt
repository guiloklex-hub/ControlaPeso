package br.com.paivalab.controlapeso.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GithubReleaseParserTest {
    @Test
    fun `parses published stable release with the only accepted APK assets`() {
        val release = requireNotNull(GithubReleaseParser.parseStableRelease(validReleaseJson()))

        assertEquals("v1.2.0", release.tagName)
        assertEquals("ControlaPeso-v1.2.0-universal.apk", release.universalApk.name)
        assertEquals("SHA256SUMS.txt", release.checksumFile.name)
        assertTrue(release.version.isStable)
    }

    @Test
    fun `rejects pre release and missing universal APK`() {
        assertNull(GithubReleaseParser.parseStableRelease(validReleaseJson().replace(
            "\"prerelease\":false",
            "\"prerelease\":true"
        )))
        assertNull(GithubReleaseParser.parseStableRelease(validReleaseJson().replace(
            "ControlaPeso-v1.2.0-universal.apk",
            "ControlaPeso-v1.2.0-arm64-v8a.apk"
        )))
    }

    @Test
    fun `rejects ambiguous checksum entries`() {
        val fileName = "ControlaPeso-v1.2.0-universal.apk"
        val content = "${"a".repeat(64)}  $fileName\n${"b".repeat(64)}  $fileName"

        assertNull(Sha256Sums.findHashFor(content, fileName))
    }

    private fun validReleaseJson(): String = """
        {
          "tag_name":"v1.2.0",
          "draft":false,
          "prerelease":false,
          "published_at":"2026-07-28T12:00:00Z",
          "body":"Correções.",
          "assets":[
            {"name":"ControlaPeso-v1.2.0-universal.apk","browser_download_url":"https://example.test/app.apk"},
            {"name":"SHA256SUMS.txt","browser_download_url":"https://example.test/sums"}
          ]
        }
    """.trimIndent()
}
