package com.example.lywsd02bledashboard.update

import android.content.Context
import android.os.Build
import com.example.lywsd02bledashboard.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub Releases API를 조회하여 최신 버전 확인 및 릴리즈 정보 파싱을 담당하는 클래스.
 * 의도: 외부 무거운 라이브러리 없이 순수 Android 내장 API(HttpURLConnection, org.json)를 활용하여
 * 경량이고 안정적으로 백그라운드에서 최신 릴리즈를 검사합니다.
 */
object UpdateChecker {

    private const val GITHUB_OWNER = "muro-dot"
    private const val GITHUB_REPO = "LYWSD02_BLE_Tool"
    private const val LATEST_RELEASE_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    /**
     * GitHub 최신 릴리즈 정보를 비동기(IO 스레드)로 조회합니다.
     */
    suspend fun checkLatestRelease(context: Context): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val currentVersion = getCurrentAppVersion(context)

        var connection: HttpURLConnection? = null
        try {
            val url = URL(LATEST_RELEASE_API_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "LYWSD02-Android-App")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // Rate limit(403) 또는 릴리즈 미등록(404) 등 예외 처리
                return@withContext null
            }

            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val json = JSONObject(responseBody)

            val rawTagName = json.optString("tag_name", "").trim()
            val latestVersion = rawTagName.removePrefix("v").removePrefix("V")
            val releaseTitle = json.optString("name", "새로운 버전이 출시되었습니다")
            val releaseNotes = json.optString("body", "")

            // assets 배열에서 .apk 파일 다운로드 링크 추출
            var apkUrl = ""
            var apkFileName = ""
            val assetsArray = json.optJSONArray("assets")
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", "")
                        apkFileName = assetName
                        break
                    }
                }
            }

            val hasUpdate = isNewerVersion(currentVersion, latestVersion)

            AppUpdateInfo(
                currentVersion = currentVersion,
                latestVersion = latestVersion,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                apkDownloadUrl = apkUrl,
                apkFileName = apkFileName.ifEmpty { "LYWSD02_BLE_Tool_v$latestVersion.apk" },
                hasUpdate = hasUpdate
            )
        } catch (e: Exception) {
            // 네트워크 미연결 또는 일시적 오류 시 크래시 없이 무시
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 현재 앱에 설치된 versionName을 획득합니다.
     */
    fun getCurrentAppVersion(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * 유의적 버전(Semantic Versioning)을 비교하여 원격 버전이 현재 버전보다 높은지 판별합니다.
     * 예: "1.2.0" > "1.1.1" -> true
     */
    fun isNewerVersion(current: String, latest: String): Boolean {
        if (latest.isBlank()) return false
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLen = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
