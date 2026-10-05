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
import kotlinx.coroutines.Dispatchers
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
    val bleManager = BleManager(application, bluetoothManager?.adapter)

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            isAutoSyncClockEnabled = repository.isAutoSyncClockEnabled,
            selectedUnit = repository.defaultTemperatureUnit,
            deviceTimezoneMinutes = repository.savedTimezoneMinutes,
            knownDevices = repository.getKnownDevices()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeBleEvents()
        addLog("LYWSD02 BLE 대시보드가 준비되었습니다. 블루투스를 켜고 기기를 검색하세요.", LogType.INFO)
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
                _uiState.update {
                    it.copy(
                        deviceTimeFormatted = timeResult.formattedTime,
                        deviceTimezoneMinutes = timeResult.deviceTimezoneMinutes,
                        clockDriftSeconds = timeResult.driftSeconds
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
            bleManager.readBattery()

            // 2. 온도 단위 읽기
            bleManager.readUnit()

            // 3. 기기 시계 읽기
            val timeResult = bleManager.readTime(
                desiredOffsetMinutes = _uiState.value.deviceTimezoneMinutes,
                isTwelveHour = _uiState.value.clockMode == ClockDisplayMode.MODE_12H
            )

            // 4. 자동 시간 동기화 (드리프트가 10초 이상이고 자동 보정 옵션이 켜져 있을 때)
            if (_uiState.value.isAutoSyncClockEnabled && timeResult != null) {
                if (Math.abs(timeResult.driftSeconds) > 10) {
                    addLog("시간 오차가 ${Math.abs(timeResult.driftSeconds)}초 감지되어 시계를 자동으로 동기화합니다.", LogType.INFO)
                    syncClock(isAutomatic = true)
                }
            }

            // 연결 기록 갱신
            repository.recordConnectionSuccess(
                id = deviceId,
                name = _uiState.value.connectedDeviceName ?: "LYWSD02"
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
        _uiState.update {
            it.copy(
                connectedDeviceId = address,
                connectedDeviceName = name,
                connectedDeviceAlias = known?.alias,
                temperatureCelsius = null,
                humidityPercentage = null,
                batteryPercentage = null,
                deviceTimeFormatted = null,
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
                connectedDeviceAlias = null
            )
        }
    }

    fun refreshClock() {
        viewModelScope.launch {
            bleManager.readTime(
                desiredOffsetMinutes = _uiState.value.deviceTimezoneMinutes,
                isTwelveHour = _uiState.value.clockMode == ClockDisplayMode.MODE_12H
            )
        }
    }

    fun syncClock(isAutomatic: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingClock = true) }
            bleManager.syncTime(
                desiredOffsetMinutes = _uiState.value.deviceTimezoneMinutes,
                manualOffsetMinutes = _uiState.value.manualOffsetMinutes,
                clockMode = _uiState.value.clockMode,
                automatic = isAutomatic
            )
            _uiState.update { it.copy(isSyncingClock = false) }
        }
    }

    fun setClockMode(mode: ClockDisplayMode) {
        _uiState.update { it.copy(clockMode = mode) }
    }

    fun setTimezoneMinutes(minutes: Int) {
        repository.savedTimezoneMinutes = minutes
        _uiState.update { it.copy(deviceTimezoneMinutes = minutes) }
        refreshClock()
    }

    fun setManualOffsetMinutes(minutes: Int) {
        _uiState.update { it.copy(manualOffsetMinutes = minutes) }
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
            }
            _uiState.update { it.copy(isUpdatingUnit = false) }
        }
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
