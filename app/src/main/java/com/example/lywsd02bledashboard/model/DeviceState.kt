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
    val deviceTimezoneMinutes: Int = 540, // 기본값: 한국 표준시 UTC+9 (540분)
    val clockDriftSeconds: Long? = null,
    val clockMode: ClockDisplayMode = ClockDisplayMode.MODE_24H,
    val manualOffsetMinutes: Int = 0,
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
    val scannedDevices: List<ScannedDeviceInfo> = emptyList()
)

/**
 * BLE 스캔을 통해 발견된 장치 정보
 */
data class ScannedDeviceInfo(
    val address: String,
    val name: String,
    val rssi: Int
)
