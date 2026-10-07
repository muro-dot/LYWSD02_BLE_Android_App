package com.example.lywsd02bledashboard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lywsd02bledashboard.model.ConnectionState
import com.example.lywsd02bledashboard.theme.BackgroundPaper
import com.example.lywsd02bledashboard.ui.components.ClockSettingsCard
import com.example.lywsd02bledashboard.ui.components.DeviceIdentityCard
import com.example.lywsd02bledashboard.ui.components.DeviceRenameDialog
import com.example.lywsd02bledashboard.ui.components.DeviceScanDialog
import com.example.lywsd02bledashboard.ui.components.DisplaySettingsCard
import com.example.lywsd02bledashboard.ui.components.HeaderSection
import com.example.lywsd02bledashboard.ui.components.HistoryCard
import com.example.lywsd02bledashboard.ui.components.KnownDevicesCard
import com.example.lywsd02bledashboard.ui.components.LogConsoleCard
import com.example.lywsd02bledashboard.ui.components.PermissionHandler
import com.example.lywsd02bledashboard.ui.components.SensorMetricsGrid

/**
 * LYWSD02 BLE 대시보드 메인 화면.
 * 원작 웹 대시보드(https://drslid.github.io/LYWSD02_BLE_Dashboard/)와
 * 동일한 디자인 및 센서 연동 기능을 제공합니다.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundPaper,
        topBar = {
            HeaderSection(
                connectionState = state.connectionState,
                onSearchClick = { viewModel.startScan() },
                onDisconnectClick = { viewModel.disconnect() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPaper)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 블루투스 권한 핸들러
            PermissionHandler(onPermissionsGranted = {})

            // 1. 기기 정보 및 별칭 카드
            DeviceIdentityCard(
                deviceId = state.connectedDeviceId,
                deviceName = state.connectedDeviceName,
                deviceAlias = state.connectedDeviceAlias,
                isConnected = state.connectionState == ConnectionState.CONNECTED,
                onRenameClick = { viewModel.openRenameDialog() }
            )

            // 2. 실시간 센서 온습도 및 배터리 메트릭 그리드
            SensorMetricsGrid(
                temperatureC = state.temperatureCelsius,
                humidity = state.humidityPercentage,
                battery = state.batteryPercentage,
                unit = state.selectedUnit,
                lastMeasurementTime = state.lastMeasurementTime,
                lastBatteryTime = state.lastBatteryTime,
                updateCount = state.measurementCount
            )

            // 3. 기기 시계 및 시간 동기화 카드
            ClockSettingsCard(
                deviceTimeFormatted = state.deviceTimeFormatted,
                deviceTimezoneMinutes = state.deviceTimezoneMinutes,
                targetTimezoneMinutes = state.targetTimezoneMinutes,
                isUsingSystemTimezone = state.isUsingSystemTimezone,
                clockDriftSeconds = state.clockDriftSeconds,
                clockMode = state.clockMode,
                manualOffsetMinutes = state.manualOffsetMinutes,
                isAutoSyncEnabled = state.isAutoSyncClockEnabled,
                isSyncing = state.isSyncingClock,
                isConnected = state.connectionState == ConnectionState.CONNECTED,
                onRefreshClock = { viewModel.refreshClock() },
                onSyncClock = { viewModel.syncClock() },
                onClockModeChange = { viewModel.setClockMode(it) },
                onTimezoneChange = { viewModel.setTimezoneMinutes(it) },
                onSelectSystemTimezone = { viewModel.setSystemTimezone() },
                onManualOffsetChange = { viewModel.setManualOffsetMinutes(it) },
                onAutoSyncChange = { viewModel.setAutoSyncClockEnabled(it) }
            )

            // 4. 액정 표시 온도 단위 설정 카드
            DisplaySettingsCard(
                currentUnit = state.selectedUnit,
                isUpdating = state.isUpdatingUnit,
                isConnected = state.connectionState == ConnectionState.CONNECTED,
                onSaveUnit = { viewModel.saveUnit(it) }
            )

            // 5. 과거 온습도 기록(최대 96개) 조회 및 CSV 내보내기 카드
            HistoryCard(
                historyRecords = state.historyRecords,
                historyLimit = state.historyLimit,
                isLoading = state.isHistoryLoading,
                statusMessage = state.historyStatusMessage,
                unit = state.selectedUnit,
                isConnected = state.connectionState == ConnectionState.CONNECTED,
                onLimitChange = { viewModel.setHistoryLimit(it) },
                onLoadHistory = { viewModel.loadHistory() },
                onExportCsv = { viewModel.exportHistoryCsv(context) }
            )

            // 6. 최근 연결 기기 목록 (빠른 재연결) 카드
            KnownDevicesCard(
                knownDevices = state.knownDevices,
                currentConnectedId = state.connectedDeviceId,
                onDeviceClick = { dev ->
                    viewModel.connectToDevice(dev.id, dev.name)
                },
                onDeleteDevice = { id ->
                    viewModel.removeKnownDevice(id)
                }
            )

            // 7. 실시간 BLE 통신 이벤트 로그 콘솔 카드
            LogConsoleCard(
                logs = state.logs,
                onClearLogs = { viewModel.clearLogs() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 주변 센서 검색 결과 모달 다이얼로그
    if (state.isScanDialogOpen) {
        DeviceScanDialog(
            devices = state.scannedDevices,
            isScanning = state.connectionState == ConnectionState.SCANNING,
            onDismiss = { viewModel.closeScanDialog() },
            onDeviceSelect = { dev ->
                viewModel.connectToDevice(dev.address, dev.name)
            }
        )
    }

    // 별칭 수정 팝업 다이얼로그
    if (state.isRenameDialogOpen) {
        DeviceRenameDialog(
            currentAlias = state.connectedDeviceAlias,
            onDismiss = { viewModel.closeRenameDialog() },
            onSave = { newAlias ->
                viewModel.saveDeviceAlias(newAlias)
            }
        )
    }
}
