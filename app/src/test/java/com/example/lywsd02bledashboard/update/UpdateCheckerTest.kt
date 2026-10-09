package com.example.lywsd02bledashboard.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 깃허브 릴리즈 버전 비교 로직(Semantic Versioning) 단위 테스트
 */
class UpdateCheckerTest {

    @Test
    fun testIsNewerVersion_whenRemoteIsHigher_returnsTrue() {
        assertTrue(UpdateChecker.isNewerVersion("1.1.1", "1.2.0"))
        assertTrue(UpdateChecker.isNewerVersion("1.0.0", "1.0.1"))
        assertTrue(UpdateChecker.isNewerVersion("1.0.0", "2.0.0"))
        assertTrue(UpdateChecker.isNewerVersion("1.2.0", "1.2.1"))
    }

    @Test
    fun testIsNewerVersion_whenRemoteIsEqualOrLower_returnsFalse() {
        assertFalse(UpdateChecker.isNewerVersion("1.2.0", "1.2.0"))
        assertFalse(UpdateChecker.isNewerVersion("1.2.1", "1.2.0"))
        assertFalse(UpdateChecker.isNewerVersion("2.0.0", "1.9.9"))
        assertFalse(UpdateChecker.isNewerVersion("1.2.0", ""))
    }

    /**
     * 구버전 앱(1.2.0, 1.2.1)에서 사용하던 기존 저장소 URL(LYWSD02_BLE_Tool)이
     * GitHub API의 301 리다이렉트를 통해 정상적으로 최신 릴리즈 정보를 수신하는지 검증합니다.
     */
    @Test
    fun testLegacyUrlHttpURLConnectionRedirect() {
        val legacyUrl = java.net.URL("https://api.github.com/repos/muro-dot/LYWSD02_BLE_Tool/releases/latest")
        val conn = (legacyUrl.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 8000
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", "LYWSD02-Android-App")
        }
        val code = conn.responseCode
        val body = java.io.BufferedReader(java.io.InputStreamReader(conn.inputStream)).use { it.readText() }
        org.junit.Assert.assertEquals(200, code)
        org.junit.Assert.assertTrue(body.contains("\"tag_name\""))
    }
}
