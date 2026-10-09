package com.example.lywsd02bledashboard.ui.main

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lywsd02bledashboard.ble.BleManager
import com.example.lywsd02bledashboard.ble.BleProtocolParser
import com.example.lywsd02bledashboard.data.DevicePreferencesRepository
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.model.ConnectionState
import com.example.lywsd02bledashboard.model.DashboardUiState
import com.example.lywsd02bledashboard.model.HistoryRecord
import com.example.lywsd02bledashboard.model.LogEntry
import com.example.lywsd02bledashboard.model.LogType
import com.example.lywsd02bledashboard.model.TemperatureUnit
import com.example.lywsd02bledashboard.model.AppUpdateInfo
import com.example.lywsd02bledashboard.model.UpdateDownloadState
import com.example.lywsd02bledashboard.update.UpdateChecker
import com.example.lywsd02bledashboard.update.UpdateInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LYWSD02 BLE 대시보드의 비즈니스 로직 및 상태를 총괄하는 ViewModel.
 * 원작 웹 대시보드(app.js)의 모든 상태 전환과 프로토콜 시퀀스를 코루틴 기반으로 제어합니다.
 */
class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DevicePreferencesRepository(application)
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bleManager = BleManager(
        context = application,
        bluetoothAdapter = bluetoothManager?.adapter,
        aliasProvider = { repository.getDeviceAlias(it) },
        knownDeviceProvider = { addr -> repository.getKnownDevices().find { it.id.equals(addr, ignoreCase = true) } }
    )

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            isAutoSyncClockEnabled = repository.isAutoSyncClockEnabled,
            selectedUnit = repository.defaultTemperatureUnit,
            targetTimezoneMinutes = repository.savedTimezoneMinutes,
            isUsingSystemTimezone = repository.useSystemTimezone,
            deviceTimezoneMinutes = null,
            knownDevices = repository.getKnownDevices(),
            appLanguage = repository.appLanguage
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeBleEvents()
        addLog("LYWSD02 BLE 대시보드가 준비되었습니다. 블루투스를 켜고 기기를 검색하세요.", LogType.INFO)
        checkForUpdates()
    }

    private fun observeBleEvents() {
        // 1. 연결 상태 관찰
        viewModelScope.launch {
            bleManager.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
                if (state == ConnectionState.CONNECTED) {
                    onConnectedSequence()
                } else if (state == ConnectionState.DISCONNECTED) {
                    _uiState.update {
                        it.copy(
                            isSyncingClock = false,
                            isUpdatingUnit = false,
                            isHistoryLoading = false
                        )
                    }
                }
            }
        }

        // 2. 스캔 목록 관찰
        viewModelScope.launch {
            bleManager.scannedDevices.collect { list ->
                _uiState.update { it.copy(scannedDevices = list) }
            }
        }

        // 3. 로그 관찰
        viewModelScope.launch {
            bleManager.logFlow.collect { (msg, type) ->
                addLog(msg, type)
            }
        }

        // 4. 실시간 온습도 측정값 관찰
        viewModelScope.launch {
            bleManager.measurementFlow.collect { (tempC, humidity) ->
                val nowStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _uiState.update {
                    it.copy(
                        temperatureCelsius = tempC,
                        humidityPercentage = humidity,
                        lastMeasurementTime = nowStr,
                        measurementCount = it.measurementCount + 1
                    )
                }

                // 현재 연결된 기기 스냅샷 업데이트
                _uiState.value.connectedDeviceId?.let { deviceId ->
                    repository.recordConnectionSuccess(
                        id = deviceId,
                        name = _uiState.value.connectedDeviceName ?: "LYWSD02",
                        temp = tempC,
                        hum = humidity,
                        bat = _uiState.value.batteryPercentage
                    )
                    _uiState.update { it.copy(knownDevices = repository.getKnownDevices()) }
                }
            }
        }

        // 5. 배터리 관찰
        viewModelScope.launch {
            bleManager.batteryFlow.collect { bat ->
                val nowStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _uiState.update {
                    it.copy(
                        batteryPercentage = bat,
                        lastBatteryTime = nowStr
                    )
                }
            }
        }

        // 6. 온도 단위 관찰
        viewModelScope.launch {
            bleManager.unitFlow.collect { unit ->
                _uiState.update { it.copy(selectedUnit = unit) }
            }
        }

        // 7. 시계 읽기 관찰
        viewModelScope.launch {
            bleManager.timeFlow.collect { timeResult ->
                _uiState.update { current ->
                    // 사용자가 12시간제를 선택했거나, 기기에서 12시간제를 반환한 경우 12시간 형식으로 표시
                    val displayTime = if (current.clockMode == ClockDisplayMode.MODE_12H || timeResult.detectedClockMode == ClockDisplayMode.MODE_12H) {
                        BleProtocolParser.formatEpoch(timeResult.localEpochSeconds, isTwelveHour = true)
                    } else {
                        timeResult.formattedTime
                    }
                    current.copy(
                        deviceTimeFormatted = displayTime,
                        deviceLocalEpochSeconds = timeResult.localEpochSeconds,
                        deviceTimezoneMinutes = timeResult.deviceTimezoneMinutes,
                        clockDriftSeconds = timeResult.driftSeconds
                        // 주의: 사용자가 선택한 clockMode를 기기 응답으로 임의 덮어쓰지 않음
                    )
                }
            }
        }
    }

    /**
     * 기기 연결 성공 직후 초기 필수 데이터 자동 조회 시퀀스
     * 원작 app.js의 readInitialValues()와 동일: 배터리 -> 단위 -> 시계 순서로 읽고,
     * 드리프트가 10초 이상이면 시계를 자동 동기화합니다.
     */
    private fun onConnectedSequence() {
        viewModelScope.launch {
            val deviceId = _uiState.value.connectedDeviceId ?: return@launch
            val known = repository.getKnownDevices().find { it.id == deviceId }

            _uiState.update {
                it.copy(
                    connectedDeviceAlias = known?.alias,
                    retryAttempt = 0,
                    measurementCount = 0
                )
            }

            // 1. 배터리 읽기
            val bat = bleManager.readBattery()

            // 2. 온도 단위 읽기 및 기기별 저장소 갱신
            val unit = bleManager.readUnit()
            if (unit != null) {
                repository.updateDeviceUnit(deviceId, unit)
            }

            // 3. 기기 시계 읽기 (목표 타임존 기준으로 오차 계산)
            val timeResult = bleManager.readTime(
                desiredOffsetMinutes = _uiState.value.targetTimezoneMinutes,
                isTwelveHour = _uiState.value.clockMode == ClockDisplayMode.MODE_12H
            )
            // 기존에 설정된 모드가 없는 신규 기기이고 기기에서 12h/24h 모드를 보고한 경우에만 초기값으로 채택
            val effectiveClockMode = if (known?.clockMode == null && timeResult?.detectedClockMode != null) {
                timeResult.detectedClockMode
            } else {
                _uiState.value.clockMode
            }
            _uiState.update { it.copy(clockMode = effectiveClockMode) }

            // 4. 자동 시간 동기화 (드리프트가 10초 이상이고 자동 보정 옵션이 켜져 있을 때)
            if (_uiState.value.isAutoSyncClockEnabled && timeResult != null) {
                if (Math.abs(timeResult.driftSeconds) > 10) {
                    addLog("시간 오차가 ${Math.abs(timeResult.driftSeconds)}초 감지되어 시계를 자동으로 동기화합니다.", LogType.INFO)
                    syncClock(isAutomatic = true)
                }
            }

            // 연결 기록 갱신 (기기별 clockMode 및 unit 보존)
            repository.recordConnectionSuccess(
                id = deviceId,
                name = _uiState.value.connectedDeviceName ?: "LYWSD02",
                bat = bat,
                clockMode = effectiveClockMode,
                unit = unit ?: _uiState.value.selectedUnit
            )
            _uiState.update { it.copy(knownDevices = repository.getKnownDevices()) }
        }
    }

    fun startScan() {
        _uiState.update { it.copy(isScanDialogOpen = true) }
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun closeScanDialog() {
        bleManager.stopScan()
        _uiState.update { it.copy(isScanDialogOpen = false) }
    }

    fun connectToDevice(address: String, name: String) {
        closeScanDialog()
        val known = repository.getKnownDevices().find { it.id == address }
        // 기존 MMC 모델명 기준 12시간 강제 지정 로직을 삭제하고,
        // 이 기기에서 이전에 사용자가 설정했던 clockMode와 단위를 복원합니다.
        val initialClockMode = known?.clockMode ?: ClockDisplayMode.MODE_24H
        val initialUnit = known?.lastUnit ?: repository.defaultTemperatureUnit
        _uiState.update {
            it.copy(
                connectedDeviceId = address,
                connectedDeviceName = name,
                connectedDeviceAlias = known?.alias,
                clockMode = initialClockMode,
                selectedUnit = initialUnit,
                temperatureCelsius = null,
                humidityPercentage = null,
                batteryPercentage = null,
                deviceTimeFormatted = null,
                deviceTimezoneMinutes = null,
                clockDriftSeconds = null,
                historyRecords = emptyList()
            )
        }
        bleManager.connect(address, name)
    }

    fun disconnect() {
        bleManager.disconnect()
        _uiState.update {
            it.copy(
                connectedDeviceId = null,
                connectedDeviceName = null,
                connectedDeviceAlias = null,
                deviceTimeFormatted = null,
                deviceTimezoneMinutes = null,
                clockDriftSeconds = null
            )
        }
    }

    fun refreshClock() {
        viewModelScope.launch {
            bleManager.readTime(
                desiredOffsetMinutes = _uiState.value.targetTimezoneMinutes,
                isTwelveHour = _uiState.value.clockMode == ClockDisplayMode.MODE_12H
            )
        }
    }

    fun syncClock(isAutomatic: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingClock = true) }
            val mode = _uiState.value.clockMode
            val success = bleManager.syncTime(
                desiredOffsetMinutes = _uiState.value.targetTimezoneMinutes,
                manualOffsetMinutes = 0,
                clockMode = mode,
                automatic = isAutomatic
            )
            if (success) {
                _uiState.value.connectedDeviceId?.let { deviceId ->
                    repository.updateDeviceClockMode(deviceId, mode)
                    _uiState.update { it.copy(knownDevices = repository.getKnownDevices()) }
                }
            }
            _uiState.update { it.copy(isSyncingClock = false) }
        }
    }

    fun setClockMode(mode: ClockDisplayMode) {
        val isTwelveHour = (mode == ClockDisplayMode.MODE_12H)
        _uiState.update { current ->
            // 현재 표시된 센서 시간을 사용자가 선택한 모드로 즉시 다시 포맷팅
            val updatedFormattedTime = current.deviceLocalEpochSeconds?.let { epoch ->
                BleProtocolParser.formatEpoch(epoch, isTwelveHour)
            } ?: current.deviceTimeFormatted

            current.copy(
                clockMode = mode,
                deviceTimeFormatted = updatedFormattedTime
            )
        }
        val deviceId = _uiState.value.connectedDeviceId
        if (deviceId != null) {
            repository.updateDeviceClockMode(deviceId, mode)
            _uiState.update { it.copy(knownDevices = repository.getKnownDevices()) }
            if (_uiState.value.connectionState == ConnectionState.CONNECTED) {
                viewModelScope.launch {
                    // 기기(LYWSD02MMC/커스텀 기기)에 12h/24h 모드 쓰기 시도
                    bleManager.setClockMode(mode)
                    delay(200)
                    bleManager.readTime(
                        desiredOffsetMinutes = _uiState.value.targetTimezoneMinutes,
                        isTwelveHour = isTwelveHour
                    )
                }
            }
        }
    }

    fun setTimezoneMinutes(minutes: Int) {
        val systemOffset = BleProtocolParser.getSystemTimezoneOffsetMinutes()
        val isSystem = (minutes == systemOffset)
        repository.useSystemTimezone = isSystem
        repository.savedTimezoneMinutes = minutes
        _uiState.update { 
            it.copy(
                targetTimezoneMinutes = minutes,
                isUsingSystemTimezone = isSystem
            ) 
        }
        if (_uiState.value.connectionState == ConnectionState.CONNECTED) {
            refreshClock()
        }
    }

    fun setSystemTimezone() {
        val systemOffset = BleProtocolParser.getSystemTimezoneOffsetMinutes()
        repository.useSystemTimezone = true
        repository.savedTimezoneMinutes = systemOffset
        _uiState.update {
            it.copy(
                targetTimezoneMinutes = systemOffset,
                isUsingSystemTimezone = true
            )
        }
        addLog("스마트폰 시스템 타임존(${BleProtocolParser.formatTimezoneOffset(systemOffset)})으로 설정되었습니다.", LogType.INFO)
        if (_uiState.value.connectionState == ConnectionState.CONNECTED) {
            refreshClock()
        }
    }

    fun setAutoSyncClockEnabled(enabled: Boolean) {
        repository.isAutoSyncClockEnabled = enabled
        _uiState.update { it.copy(isAutoSyncClockEnabled = enabled) }
    }

    fun saveUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingUnit = true) }
            val success = bleManager.writeUnit(unit)
            if (success) {
                repository.defaultTemperatureUnit = unit
                _uiState.value.connectedDeviceId?.let { deviceId ->
                    repository.updateDeviceUnit(deviceId, unit)
                    _uiState.update { it.copy(knownDevices = repository.getKnownDevices()) }
                }
            }
            _uiState.update { it.copy(isUpdatingUnit = false) }
        }
    }

    /**
     * 앱 표시 언어 변경 ("system", "ko", "en")
     */
    fun setAppLanguage(lang: String) {
        repository.appLanguage = lang
        _uiState.update { it.copy(appLanguage = lang) }
    }

    fun setHistoryLimit(limit: Int) {
        _uiState.update { it.copy(historyLimit = limit) }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isHistoryLoading = true,
                    historyStatusMessage = "센서로부터 시간별 과거 기록을 수집하고 있습니다..."
                )
            }
            val records = bleManager.fetchHistory(_uiState.value.historyLimit)
            _uiState.update {
                it.copy(
                    isHistoryLoading = false,
                    historyRecords = records,
                    historyStatusMessage = if (records.isNotEmpty()) {
                        "최근 ${records.size}건의 기록을 성공적으로 불러왔습니다."
                    } else {
                        "불러올 수 있는 기록이 없거나 수신에 실패했습니다."
                    }
                )
            }
        }
    }

    /**
     * 수집된 히스토리 CSV 파일로 저장 후 공유 Intent 실행
     */
    fun exportHistoryCsv(context: Context) {
        val records = _uiState.value.historyRecords
        if (records.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val unit = _uiState.value.selectedUnit
                val sb = java.lang.StringBuilder()
                sb.append(HistoryRecord.csvHeader()).append("\n")
                for (rec in records) {
                    sb.append(rec.toCsvRow(unit)).append("\n")
                }

                val fileName = "LYWSD02_History_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val file = File(exportDir, fileName)
                file.writeText(sb.toString())

                withContext(Dispatchers.Main) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_SUBJECT, "LYWSD02 센서 과거 온습도 기록")
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "과거 기록 CSV 내보내기"))
                    addLog("과거 기록 CSV 내보내기 파일이 생성되었습니다: $fileName", LogType.SUCCESS)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLog("CSV 내보내기 실패: ${e.message}", LogType.ERROR)
                }
            }
        }
    }

    fun openRenameDialog() {
        _uiState.update { it.copy(isRenameDialogOpen = true) }
    }

    fun closeRenameDialog() {
        _uiState.update { it.copy(isRenameDialogOpen = false) }
    }

    fun saveDeviceAlias(alias: String) {
        val deviceId = _uiState.value.connectedDeviceId ?: return
        repository.updateDeviceAlias(deviceId, alias.trim())
        _uiState.update {
            it.copy(
                connectedDeviceAlias = alias.trim(),
                isRenameDialogOpen = false,
                knownDevices = repository.getKnownDevices()
            )
        }
        addLog("기기 별칭이 '${alias.trim()}'(으)로 저장되었습니다.", LogType.SUCCESS)
    }

    fun removeKnownDevice(id: String) {
        repository.removeKnownDevice(id)
        _uiState.update { it.copy(knownDevices = repository.getKnownDevices()) }
        addLog("저장된 기기 기록이 삭제되었습니다.", LogType.INFO)
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    /**
     * 깃허브 최신 릴리즈를 비동기로 조회하여 업데이트 존재 시 알림을 띄웁니다.
     * @param isManual 사용자가 직접 버튼을 눌러 수동 조회를 요청했는지 여부
     */
    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            if (isManual) {
                addLog("최신 릴리즈 버전을 확인하고 있습니다...", LogType.INFO)
            }
            val updateInfo = UpdateChecker.checkLatestRelease(getApplication())
            if (updateInfo != null) {
                if (updateInfo.hasUpdate) {
                    _uiState.update {
                        it.copy(
                            appUpdateInfo = updateInfo,
                            isUpdateDialogOpen = true,
                            updateDownloadState = UpdateDownloadState.Idle
                        )
                    }
                    addLog("새로운 릴리즈(v${updateInfo.latestVersion})가 발견되었습니다! 업데이트 팝업을 표시합니다.", LogType.INFO)
                } else {
                    addLog("현재 최신 버전(v${updateInfo.currentVersion})을 사용하고 있습니다.", LogType.SUCCESS)
                }
            } else {
                if (isManual) {
                    addLog("최신 릴리즈 정보를 확인할 수 없습니다. (네트워크 연결 상태 또는 GitHub API 호출 제한 확인)", LogType.WARNING)
                }
            }
        }
    }

    /**
     * 업데이트 APK 다운로드를 시작하고 완료 시 자동 설치를 트리거합니다.
     */
    fun startAppUpdate() {
        val updateInfo = _uiState.value.appUpdateInfo ?: return
        if (updateInfo.apkDownloadUrl.isBlank()) {
            _uiState.update {
                it.copy(updateDownloadState = UpdateDownloadState.Error("릴리즈에 등록된 APK 다운로드 링크를 찾을 수 없습니다."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(updateDownloadState = UpdateDownloadState.Downloading(0))
            }
            addLog("최신 릴리즈 APK 다운로드를 시작합니다...", LogType.INFO)

            val result = UpdateInstaller.downloadApk(
                context = getApplication(),
                downloadUrl = updateInfo.apkDownloadUrl,
                targetFileName = updateInfo.apkFileName
            ) { progress ->
                _uiState.update {
                    it.copy(updateDownloadState = UpdateDownloadState.Downloading(progress))
                }
            }

            result.fold(
                onSuccess = { file ->
                    _uiState.update {
                        it.copy(updateDownloadState = UpdateDownloadState.DownloadCompleted(file.absolutePath))
                    }
                    addLog("APK 다운로드 완료. 패키지 설치 화면을 호출합니다.", LogType.SUCCESS)
                    installAppUpdate(file)
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "다운로드 중 오류가 발생했습니다."
                    _uiState.update {
                        it.copy(updateDownloadState = UpdateDownloadState.Error(errorMsg))
                    }
                    addLog("업데이트 다운로드 실패: $errorMsg", LogType.ERROR)
                }
            )
        }
    }

    /**
     * 다운로드된 APK 파일을 패키지 인스톨러로 연결합니다.
     */
    fun installAppUpdate(specifiedFile: File? = null) {
        val file = specifiedFile ?: run {
            val path = (_uiState.value.updateDownloadState as? UpdateDownloadState.DownloadCompleted)?.apkFilePath
            path?.let { File(it) }
        }

        if (file == null || !file.exists()) {
            _uiState.update {
                it.copy(updateDownloadState = UpdateDownloadState.Error("설치할 APK 파일이 존재하지 않습니다."))
            }
            return
        }

        val installResult = UpdateInstaller.installApk(getApplication(), file)
        installResult.onFailure { error ->
            val msg = error.message ?: "앱 설치 화면 호출에 실패했습니다."
            _uiState.update {
                it.copy(updateDownloadState = UpdateDownloadState.Error(msg))
            }
            addLog("앱 설치 실패: $msg", LogType.ERROR)
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(isUpdateDialogOpen = false) }
    }

    private fun addLog(message: String, type: LogType) {
        val newEntry = LogEntry(message = message, type = type)
        _uiState.update {
            val updated = it.logs.toMutableList()
            updated.add(newEntry)
            if (updated.size > 120) {
                updated.removeAt(0)
            }
            it.copy(logs = updated)
        }
    }
}
