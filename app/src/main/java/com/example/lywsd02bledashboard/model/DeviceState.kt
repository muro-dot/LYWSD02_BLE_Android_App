package com.example.lywsd02bledashboard.model

/**
 * BLE 연결 상태를 나타내는 열거형
 */
enum class ConnectionState {
    DISCONNECTED, // 연결 끊김
    SCANNING,     // 기기 탐색 중
    CONNECTING,   // GATT 연결 진행 중
    CONNECTED     // 서비스 탐색 및 Notification 구독 완료되어 정상 통신 중
}

/**
 * 온도 단위
 */
enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

/**
 * 시계 표시 형식 (12시간 / 24시간)
 */
enum class ClockDisplayMode {
    MODE_24H,
    MODE_12H
}

/**
 * 대시보드 전체 UI 상태를 담는 데이터 클래스
 */
data class DashboardUiState(
    // 연결 관련 상태
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val connectedDeviceId: String? = null,
    val connectedDeviceName: String? = null,
    val connectedDeviceAlias: String? = null,
    val isAutoConnecting: Boolean = false,
    val retryAttempt: Int = 0,
    val retryMessage: String? = null,

    // 센서 측정값
    val temperatureCelsius: Float? = null,
    val humidityPercentage: Int? = null,
    val batteryPercentage: Int? = null,
    val lastMeasurementTime: String? = null,
    val lastBatteryTime: String? = null,
    val measurementCount: Int = 0,

    // 기기 시계 및 시간 설정
    val deviceTimeFormatted: String? = null,
    val deviceLocalEpochSeconds: Long? = null,
    val deviceTimezoneMinutes: Int? = null, // 센서 내부 현재 타임존 (기기에서 읽은 값)
    val targetTimezoneMinutes: Int = 540, // 동기화 목표 타임존 (사용자가 선택한 값, 기본값: 시스템/KST 540분)
    val isUsingSystemTimezone: Boolean = true, // 스마트폰 시스템 타임존 추종 여부
    val clockDriftSeconds: Long? = null,
    val clockMode: ClockDisplayMode = ClockDisplayMode.MODE_24H,
    val isAutoSyncClockEnabled: Boolean = true,
    val isSyncingClock: Boolean = false,

    // 단위 설정
    val selectedUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val isUpdatingUnit: Boolean = false,

    // 히스토리 기록
    val historyRecords: List<HistoryRecord> = emptyList(),
    val historyLimit: Int = 96,
    val isHistoryLoading: Boolean = false,
    val historyStatusMessage: String? = null,

    // 기기 히스토리 목록
    val knownDevices: List<KnownDevice> = emptyList(),

    // 로그 목록
    val logs: List<LogEntry> = emptyList(),

    // UI 다이얼로그 표시 여부
    val isScanDialogOpen: Boolean = false,
    val isRenameDialogOpen: Boolean = false,
    val scannedDevices: List<ScannedDeviceInfo> = emptyList(),

    // 깃허브 최신 릴리즈 인앱 업데이트 상태
    val appUpdateInfo: AppUpdateInfo? = null,
    val updateDownloadState: UpdateDownloadState = UpdateDownloadState.Idle,
    val isUpdateDialogOpen: Boolean = false,

    // 다국어 언어 설정 ("system", "ko", "en")
    val appLanguage: String = "system"
)

/**
 * BLE 스캔을 통해 발견된 장치 정보
 *
 * @param address 블루투스 MAC 주소
 * @param name 장치에서 브로드캐스팅하는 원본 이름
 * @param rssi 신호 세기
 * @param alias 사용자가 이전에 저장한 별칭(변경한 이름)
 */
data class ScannedDeviceInfo(
    val address: String,
    val name: String,
    val rssi: Int,
    val alias: String? = null,
    val isKnownDevice: Boolean = false
) {
    /**
     * UI에 표시할 이름 (사용자가 변경한 별칭이 있으면 별칭 우선 노출)
     */
    val displayName: String
        get() = if (!alias.isNullOrBlank()) alias else name

    /**
     * 기기명이 'LYWSD02'를 포함하거나, 이전에 연결한 적이 있는 등록 기기인지 여부 (최상단 정렬 및 UI 강조에 사용)
     */
    val isLywsd02: Boolean
        get() = name.contains("LYWSD02", ignoreCase = true) ||
                (alias?.contains("LYWSD02", ignoreCase = true) == true) ||
                isKnownDevice
}
