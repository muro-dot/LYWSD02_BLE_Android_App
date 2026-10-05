package com.example.lywsd02bledashboard.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import com.example.lywsd02bledashboard.model.BleConstants
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.model.ConnectionState
import com.example.lywsd02bledashboard.model.HistoryRecord
import com.example.lywsd02bledashboard.model.LogType
import com.example.lywsd02bledashboard.model.ScannedDeviceInfo
import com.example.lywsd02bledashboard.model.TemperatureUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

/**
 * Xiaomi Mijia LYWSD02 센서 전용 BLE 통신 관리자.
 * GATT 연결 수명 주기, 특성 읽기/쓰기 큐, 실시간 데이터 스트리밍, 히스토리 수집 및 재연결을 담당합니다.
 */
class BleManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val mainHandler = Handler(Looper.getMainLooper())

    // 연결 상태
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // 스캔 결과 목록
    private val _scannedDevices = MutableStateFlow<List<ScannedDeviceInfo>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDeviceInfo>> = _scannedDevices.asStateFlow()

    // 로그 이벤트 방출
    private val _logFlow = MutableSharedFlow<Pair<String, LogType>>(extraBufferCapacity = 64)
    val logFlow: SharedFlow<Pair<String, LogType>> = _logFlow.asSharedFlow()

    // 센서 측정값 방출 (온도, 습도)
    private val _measurementFlow = MutableSharedFlow<Pair<Float, Int>>(extraBufferCapacity = 16)
    val measurementFlow: SharedFlow<Pair<Float, Int>> = _measurementFlow.asSharedFlow()

    // 배터리 방출
    private val _batteryFlow = MutableSharedFlow<Int>(extraBufferCapacity = 4)
    val batteryFlow: SharedFlow<Int> = _batteryFlow.asSharedFlow()

    // 단위 방출
    private val _unitFlow = MutableSharedFlow<TemperatureUnit>(extraBufferCapacity = 4)
    val unitFlow: SharedFlow<TemperatureUnit> = _unitFlow.asSharedFlow()

    // 시간 파싱 결과 방출
    private val _timeFlow = MutableSharedFlow<BleProtocolParser.ParsedTimeResult>(extraBufferCapacity = 4)
    val timeFlow: SharedFlow<BleProtocolParser.ParsedTimeResult> = _timeFlow.asSharedFlow()

    // 과거 기록 수신 방출
    private val _historyRecordFlow = MutableSharedFlow<HistoryRecord>(extraBufferCapacity = 128)
    val historyRecordFlow: SharedFlow<HistoryRecord> = _historyRecordFlow.asSharedFlow()

    // GATT 객체 및 활성 연결
    private var bluetoothGatt: BluetoothGatt? = null
    private var currentTargetAddress: String? = null
    private var currentTargetName: String = "LYWSD02"
    private var mainService: BluetoothGattService? = null

    // 비동기 GATT 작업 순차 실행을 위한 Mutex 및 Deferred
    private val gattMutex = Mutex()
    private var pendingReadDeferred: CompletableDeferred<ByteArray?>? = null
    private var pendingWriteDeferred: CompletableDeferred<Boolean>? = null
    private var pendingDescriptorDeferred: CompletableDeferred<Boolean>? = null

    // 재연결 제어
    private var isManualDisconnect = false
    private var autoRetryJob: Job? = null
    private var retryAttempt = 0

    // 스캔 콜백
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val name = device.name ?: result.scanRecord?.deviceName ?: "알 수 없는 기기"
                val address = device.address

                // LYWSD02 또는 관련 센서만 필터링하거나 전체 목록에 추가
                val isTarget = name.contains("LYWSD02", ignoreCase = true) ||
                        name.contains("Mijia", ignoreCase = true)

                val currentList = _scannedDevices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.address == address }

                val info = ScannedDeviceInfo(
                    address = address,
                    name = if (isTarget && !name.contains("LYWSD02")) "LYWSD02 ($name)" else name,
                    rssi = result.rssi
                )

                if (existingIndex >= 0) {
                    currentList[existingIndex] = info
                } else {
                    currentList.add(info)
                }
                _scannedDevices.value = currentList.sortedByDescending { it.rssi }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            emitLog("BLE 스캔 실패 (코드: $errorCode)", LogType.ERROR)
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    // GATT 콜백 정의
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                emitLog("기기 연결 성공. 서비스 검색을 시작합니다...", LogType.SUCCESS)
                _connectionState.value = ConnectionState.CONNECTING
                retryAttempt = 0
                // 안정적인 서비스 검색을 위해 약간의 지연 후 호출
                mainHandler.postDelayed({
                    gatt.discoverServices()
                }, 300)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                emitLog("GATT 연결이 해제되었습니다. (상태 코드: $status)", LogType.WARNING)
                cleanUpGatt()

                if (!isManualDisconnect && currentTargetAddress != null) {
                    scheduleAutoReconnect()
                } else {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mainService = gatt.getService(BleConstants.SERVICE_UUID)
                if (mainService != null) {
                    emitLog("LYWSD02 메인 서비스 발견 완료!", LogType.SUCCESS)
                    _connectionState.value = ConnectionState.CONNECTED

                    // 연결 즉시 실시간 온습도 노티피케이션 활성화
                    scope.launch {
                        enableMeasurementsNotification()
                    }
                } else {
                    emitLog("LYWSD02 필수 서비스를 찾을 수 없습니다.", LogType.ERROR)
                    disconnect()
                }
            } else {
                emitLog("서비스 검색 실패 (상태: $status)", LogType.ERROR)
                disconnect()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            handleCharacteristicRead(characteristic.uuid, value, status)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            @Suppress("DEPRECATION")
            handleCharacteristicRead(characteristic.uuid, characteristic.value, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            val success = status == BluetoothGatt.GATT_SUCCESS
            pendingWriteDeferred?.complete(success)
            pendingWriteDeferred = null
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleCharacteristicChanged(characteristic.uuid, value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            handleCharacteristicChanged(characteristic.uuid, characteristic.value)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            val success = status == BluetoothGatt.GATT_SUCCESS
            pendingDescriptorDeferred?.complete(success)
            pendingDescriptorDeferred = null
        }
    }

    private fun handleCharacteristicRead(uuid: UUID, value: ByteArray?, status: Int) {
        val bytes = if (status == BluetoothGatt.GATT_SUCCESS) value else null
        pendingReadDeferred?.complete(bytes)
        pendingReadDeferred = null
    }

    private fun handleCharacteristicChanged(uuid: UUID, value: ByteArray?) {
        if (value == null) return
        when (uuid) {
            BleConstants.MEASUREMENTS_CHARACTERISTIC_UUID -> {
                BleProtocolParser.parseMeasurement(value)?.let { (temp, hum) ->
                    _measurementFlow.tryEmit(Pair(temp, hum))
                }
            }
            BleConstants.HISTORY_CHARACTERISTIC_UUID -> {
                BleProtocolParser.parseHistoryRecord(value)?.let { record ->
                    _historyRecordFlow.tryEmit(record)
                }
            }
        }
    }

    /**
     * 주변 BLE 기기 검색 시작
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            emitLog("블루투스가 꺼져 있습니다. 활성화해주세요.", LogType.ERROR)
            return
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            emitLog("BLE 스캐너를 초기화할 수 없습니다.", LogType.ERROR)
            return
        }

        _scannedDevices.value = emptyList()
        _connectionState.value = ConnectionState.SCANNING
        emitLog("주변 LYWSD02 센서 검색을 시작합니다...", LogType.INFO)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
            .build()

        // 특정 UUID 필터링과 함께 검색 (일부 기기는 광고에 UUID를 포함하지 않을 수 있으므로 폭넓게 수신)
        scanner.startScan(null, settings, scanCallback)

        // 12초 후 자동 스캔 중단
        mainHandler.postDelayed({
            stopScan()
        }, 12000)
    }

    /**
     * BLE 기기 검색 중지
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (_connectionState.value == ConnectionState.SCANNING) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            } catch (e: Exception) {
                // 무시
            }
            if (_connectionState.value == ConnectionState.SCANNING) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
            emitLog("기기 검색이 종료되었습니다.", LogType.INFO)
        }
    }

    /**
     * 특정 기기에 연결 요청
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String, name: String = "LYWSD02") {
        stopScan()
        autoRetryJob?.cancel()
        isManualDisconnect = false
        currentTargetAddress = address
        currentTargetName = name
        retryAttempt = 0

        _connectionState.value = ConnectionState.CONNECTING
        emitLog("$name ($address) 에 연결을 시도합니다...", LogType.INFO)

        val device = bluetoothAdapter?.getRemoteDevice(address)
        if (device == null) {
            emitLog("해당 주소의 블루투스 장치를 찾을 수 없습니다.", LogType.ERROR)
            _connectionState.value = ConnectionState.DISCONNECTED
            return
        }

        cleanUpGatt()

        // Android 7.0(API 24) 이상: TRANSPORT_LE 명시
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    /**
     * 연결 수동 해제
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        isManualDisconnect = true
        autoRetryJob?.cancel()
        _connectionState.value = ConnectionState.DISCONNECTED
        emitLog("연결을 수동으로 해제합니다.", LogType.INFO)

        cleanUpGatt()
    }

    @SuppressLint("MissingPermission")
    private fun cleanUpGatt() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: Exception) {
            // 무시
        } finally {
            bluetoothGatt = null
            mainService = null
        }
    }

    /**
     * 연결 실패/끊김 시 지수 백오프 자동 재연결 스케줄링
     */
    private fun scheduleAutoReconnect() {
        if (retryAttempt >= BleConstants.RETRY_DELAYS_MS.size) {
            emitLog("최대 재시도 횟수 초과로 재연결을 중단합니다.", LogType.ERROR)
            _connectionState.value = ConnectionState.DISCONNECTED
            return
        }

        val delayMs = BleConstants.RETRY_DELAYS_MS[retryAttempt]
        retryAttempt++
        _connectionState.value = ConnectionState.CONNECTING
        emitLog("${delayMs / 1000}초 후 재연결을 시도합니다... (시도 $retryAttempt/${BleConstants.RETRY_DELAYS_MS.size})", LogType.WARNING)

        autoRetryJob?.cancel()
        autoRetryJob = scope.launch {
            delay(delayMs)
            currentTargetAddress?.let { address ->
                connect(address, currentTargetName)
            }
        }
    }

    /**
     * 실시간 측정값(온도/습도) 알림 활성화
     */
    @SuppressLint("MissingPermission")
    private suspend fun enableMeasurementsNotification(): Boolean {
        val service = mainService ?: return false
        val char = service.getCharacteristic(BleConstants.MEASUREMENTS_CHARACTERISTIC_UUID) ?: return false

        gattMutex.withLock {
            val gatt = bluetoothGatt ?: return false
            gatt.setCharacteristicNotification(char, true)

            val descriptor = char.getDescriptor(BleConstants.CCCD_UUID) ?: return false
            val deferred = CompletableDeferred<Boolean>()
            pendingDescriptorDeferred = deferred

            val writeSuccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == BluetoothGatt.GATT_SUCCESS
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }

            if (!writeSuccess) {
                pendingDescriptorDeferred = null
                return false
            }

            val result = withTimeoutOrNull(3000) { deferred.await() } ?: false
            if (result) {
                emitLog("실시간 측정값 스트리밍 수신이 시작되었습니다.", LogType.SUCCESS)
            }
            return result
        }
    }

    /**
     * 특성 값 안전하게 읽기
     */
    @SuppressLint("MissingPermission")
    private suspend fun readCharacteristicBytes(uuid: UUID): ByteArray? {
        val service = mainService ?: return null
        val char = service.getCharacteristic(uuid) ?: return null

        return gattMutex.withLock {
            val gatt = bluetoothGatt ?: return null
            val deferred = CompletableDeferred<ByteArray?>()
            pendingReadDeferred = deferred

            val initiated = gatt.readCharacteristic(char)
            if (!initiated) {
                pendingReadDeferred = null
                return null
            }

            withTimeoutOrNull(4000) { deferred.await() }
        }
    }

    /**
     * 특성 값 안전하게 쓰기
     */
    @SuppressLint("MissingPermission")
    private suspend fun writeCharacteristicBytes(uuid: UUID, data: ByteArray): Boolean {
        val service = mainService ?: return false
        val char = service.getCharacteristic(uuid) ?: return false

        return gattMutex.withLock {
            val gatt = bluetoothGatt ?: return false
            val deferred = CompletableDeferred<Boolean>()
            pendingWriteDeferred = deferred

            val initiated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(char, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothGatt.GATT_SUCCESS
            } else {
                @Suppress("DEPRECATION")
                char.value = data
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(char)
            }

            if (!initiated) {
                pendingWriteDeferred = null
                return false
            }

            withTimeoutOrNull(4000) { deferred.await() } ?: false
        }
    }

    /**
     * 배터리 잔량 읽기
     */
    suspend fun readBattery(): Int? {
        val bytes = readCharacteristicBytes(BleConstants.BATTERY_CHARACTERISTIC_UUID) ?: return null
        val level = BleProtocolParser.parseBattery(bytes)
        if (level != null) {
            _batteryFlow.tryEmit(level)
            emitLog("배터리 잔량 확인: $level%", LogType.INFO)
        }
        return level
    }

    /**
     * 기기 표시 온도 단위 읽기
     */
    suspend fun readUnit(): TemperatureUnit? {
        val bytes = readCharacteristicBytes(BleConstants.UNIT_CHARACTERISTIC_UUID) ?: return null
        val unit = BleProtocolParser.parseUnit(bytes)
        _unitFlow.tryEmit(unit)
        emitLog("기기 온도 단위 확인: ${if (unit == TemperatureUnit.FAHRENHEIT) "°F (화씨)" else "°C (섭씨)"}", LogType.INFO)
        return unit
    }

    /**
     * 기기 표시 온도 단위 변경 저장
     */
    suspend fun writeUnit(unit: TemperatureUnit): Boolean {
        val payload = BleProtocolParser.encodeUnit(unit)
        val success = writeCharacteristicBytes(BleConstants.UNIT_CHARACTERISTIC_UUID, payload)
        if (success) {
            _unitFlow.tryEmit(unit)
            emitLog("기기 온도 단위 변경 완료: ${if (unit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"}", LogType.SUCCESS)
        } else {
            emitLog("온도 단위 변경 실패", LogType.ERROR)
        }
        return success
    }

    /**
     * 기기 현재 시계 읽기
     */
    suspend fun readTime(desiredOffsetMinutes: Int, isTwelveHour: Boolean): BleProtocolParser.ParsedTimeResult? {
        val bytes = readCharacteristicBytes(BleConstants.TIME_CHARACTERISTIC_UUID) ?: return null
        val result = BleProtocolParser.parseTime(bytes, desiredOffsetMinutes, isTwelveHour)
        if (result != null) {
            _timeFlow.tryEmit(result)
            emitLog("기기 시계 읽기 성공: ${result.formattedTime} (${BleProtocolParser.formatDriftText(result.driftSeconds)})", LogType.SUCCESS)
        }
        return result
    }

    /**
     * 기기 시계 스마트폰 시간과 동기화
     */
    suspend fun syncTime(
        desiredOffsetMinutes: Int,
        manualOffsetMinutes: Int = 0,
        clockMode: ClockDisplayMode = ClockDisplayMode.MODE_24H,
        automatic: Boolean = false
    ): Boolean {
        val payload = BleProtocolParser.encodeTimeSync(desiredOffsetMinutes, manualOffsetMinutes, automatic)
        val success = writeCharacteristicBytes(BleConstants.TIME_CHARACTERISTIC_UUID, payload)
        if (!success) {
            emitLog("시간 동기화 쓰기 실패", LogType.ERROR)
            return false
        }

        // 12h / 24h 모드 추가 쓰기 시도 (LYWSD02MMC 모델 지원)
        val modePayload = BleProtocolParser.encodeClockMode(clockMode)
        try {
            writeCharacteristicBytes(BleConstants.TIME_CHARACTERISTIC_UUID, modePayload)
        } catch (e: Exception) {
            // 구형 LYWSD02는 지원하지 않을 수 있음
        }

        emitLog("시계 동기화 완료! (${BleProtocolParser.formatTimezoneOffset(desiredOffsetMinutes)})", LogType.SUCCESS)
        delay(250)
        readTime(desiredOffsetMinutes, clockMode == ClockDisplayMode.MODE_12H)
        return true
    }

    /**
     * 과거 기록(History) 수집
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchHistory(limit: Int): List<HistoryRecord> {
        val service = mainService ?: return emptyList()

        // 1. 레코드 개수 읽기
        val countBytes = readCharacteristicBytes(BleConstants.RECORD_COUNT_CHARACTERISTIC_UUID)
            ?: run {
                emitLog("히스토리 개수 조회 실패", LogType.ERROR)
                return emptyList()
            }

        val (totalRecords, storedRecords) = BleProtocolParser.parseRecordCount(countBytes)
            ?: run {
                emitLog("히스토리 개수 파싱 실패", LogType.ERROR)
                return emptyList()
            }

        if (storedRecords == 0L) {
            emitLog("기기에 저장된 과거 기록이 없습니다.", LogType.WARNING)
            return emptyList()
        }

        val expectedCount = Math.min(limit.toLong(), storedRecords).toInt()
        val startIndex = Math.max(0L, totalRecords - expectedCount + 1L)

        emitLog("총 $storedRecords 건 중 최근 $expectedCount 건의 과거 기록 수신을 요청합니다...", LogType.INFO)

        // 2. 시작 인덱스 쓰기
        val indexPayload = BleProtocolParser.encodeRecordIndex(startIndex)
        val writeIndexSuccess = writeCharacteristicBytes(BleConstants.RECORD_INDEX_CHARACTERISTIC_UUID, indexPayload)
        if (!writeIndexSuccess) {
            emitLog("히스토리 시작 인덱스 설정 실패", LogType.ERROR)
            return emptyList()
        }

        // 3. History Notification 활성화 후 레코드 수집
        val historyChar = service.getCharacteristic(BleConstants.HISTORY_CHARACTERISTIC_UUID)
            ?: return emptyList()

        val collectedRecords = mutableMapOf<Long, HistoryRecord>()
        val collectionJob = Job()

        val notifyEnabled = gattMutex.withLock {
            val gatt = bluetoothGatt ?: return emptyList()
            gatt.setCharacteristicNotification(historyChar, true)
            val descriptor = historyChar.getDescriptor(BleConstants.CCCD_UUID) ?: return emptyList()

            val deferred = CompletableDeferred<Boolean>()
            pendingDescriptorDeferred = deferred

            val sent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == BluetoothGatt.GATT_SUCCESS
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }

            if (!sent) {
                pendingDescriptorDeferred = null
                false
            } else {
                withTimeoutOrNull(3000) { deferred.await() } ?: false
            }
        }

        if (!notifyEnabled) {
            emitLog("히스토리 스트리밍 활성화 실패", LogType.ERROR)
            return emptyList()
        }

        // 스트림 수집 루프 (타임아웃 25초, 또는 유휴 4초)
        val collectorScope = CoroutineScope(Dispatchers.IO + collectionJob)
        collectorScope.launch {
            historyRecordFlow.collect { record ->
                collectedRecords[record.index] = record
                if (collectedRecords.size >= expectedCount) {
                    collectionJob.complete()
                }
            }
        }

        withTimeoutOrNull(25000) {
            collectionJob.join()
        }
        collectionJob.cancel()

        // Notification 비활성화
        try {
            gattMutex.withLock {
                val gatt = bluetoothGatt
                if (gatt != null) {
                    gatt.setCharacteristicNotification(historyChar, false)
                    val descriptor = historyChar.getDescriptor(BleConstants.CCCD_UUID)
                    if (descriptor != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
                        } else {
                            @Suppress("DEPRECATION")
                            descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // 무시
        }

        val resultList = collectedRecords.values.sortedByDescending { it.index }
        emitLog("과거 기록 ${resultList.size}건 수신 완료!", LogType.SUCCESS)
        return resultList
    }

    private fun emitLog(message: String, type: LogType) {
        _logFlow.tryEmit(Pair(message, type))
    }
}
