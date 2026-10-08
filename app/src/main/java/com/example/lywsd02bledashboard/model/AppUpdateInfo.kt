package com.example.lywsd02bledashboard.model

/**
 * GitHub 릴리즈로부터 수집한 앱 최신 업데이트 정보 데이터 모델
 *
 * @param currentVersion 현재 기기에 설치된 앱 버전
 * @param latestVersion 깃허브에 게시된 최신 릴리즈 버전 (예: "1.2.0")
 * @param releaseTitle 릴리즈 제목
 * @param releaseNotes 릴리즈 변경 내역 내용 (마크다운/텍스트)
 * @param apkDownloadUrl APK 파일 직접 다운로드 URL
 * @param apkFileName 다운로드할 파일명 (예: "LYWSD02_BLE_Tool_v1.2.0.apk")
 * @param hasUpdate 새 버전 존재 여부
 */
data class AppUpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String = "",
    val releaseNotes: String = "",
    val apkDownloadUrl: String = "",
    val apkFileName: String = "",
    val hasUpdate: Boolean = false
)

/**
 * 인앱 업데이트 다운로드 및 설치 진행 상태
 */
sealed interface UpdateDownloadState {
    // 유휴 상태 (업데이트 확인 전 또는 팝업 닫힘)
    data object Idle : UpdateDownloadState

    // 다운로드 진행 중 (0 ~ 100 퍼센트)
    data class Downloading(val progressPercent: Int) : UpdateDownloadState

    // 다운로드 완료 및 설치 인텐트 대기
    data class DownloadCompleted(val apkFilePath: String) : UpdateDownloadState

    // 다운로드 또는 설치 실패
    data class Error(val errorMessage: String) : UpdateDownloadState
}
