package com.example.lywsd02bledashboard.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * APK 파일 백그라운드 다운로드 및 Android 패키지 설치 Intent 실행을 담당하는 헬퍼 클래스.
 * 의도: 사용자가 팝업에서 '지금 업데이트'를 클릭했을 때 앱 내에서 다운로드 진행률을 보여주고,
 * 다운로드가 완료되면 FileProvider를 통해 안전하게 시스템 설치 프로그램으로 연결합니다.
 */
object UpdateInstaller {

    /**
     * 지정된 URL로부터 APK 파일을 앱 내부 캐시 디렉터리로 스트리밍 다운로드합니다.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        targetFileName: String,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val updateDir = File(context.cacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }
            // 기존 이전 다운로드 APK 파일들 정리
            updateDir.listFiles()?.forEach { it.delete() }

            val targetFile = File(updateDir, targetFileName)

            val url = URL(downloadUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 20000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "LYWSD02-Android-App")
            }

            // GitHub Releases 리다이렉트 (302) 처리
            var responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == 307) {
                val newUrl = connection.getHeaderField("Location")
                connection.disconnect()
                connection = (URL(newUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 20000
                    setRequestProperty("User-Agent", "LYWSD02-Android-App")
                }
                responseCode = connection.responseCode
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("다운로드 서버 응답 코드 오류: $responseCode"))
            }

            val fileLength = connection.contentLength
            inputStream = connection.inputStream
            outputStream = FileOutputStream(targetFile)

            val data = ByteArray(8192)
            var totalBytesRead = 0L
            var count: Int
            var lastReportedPercent = -1

            while (inputStream.read(data).also { count = it } != -1) {
                outputStream.write(data, 0, count)
                totalBytesRead += count

                if (fileLength > 0) {
                    val percent = ((totalBytesRead * 100) / fileLength).toInt()
                    if (percent != lastReportedPercent) {
                        lastReportedPercent = percent
                        onProgress(percent.coerceIn(0, 100))
                    }
                }
            }

            outputStream.flush()
            onProgress(100)
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { outputStream?.close() } catch (ignored: Exception) {}
            try { inputStream?.close() } catch (ignored: Exception) {}
            connection?.disconnect()
        }
    }

    /**
     * 다운로드된 APK 파일을 FileProvider를 통해 시스템 패키지 설치 화면으로 띄웁니다.
     */
    fun installApk(context: Context, apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(Exception("설치할 APK 파일이 존재하지 않습니다."))
            }

            // Android 8.0(Oreo, API 26) 이상: 알 수 없는 앱 설치 허용 여부 체크
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(permissionIntent)
                    // 권한 설정 창으로 이동한 후 사용자에게 안내
                    return Result.failure(Exception("원활한 앱 업데이트를 위해 '이 출처의 앱 설치 허용'을 활성화해주세요."))
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
