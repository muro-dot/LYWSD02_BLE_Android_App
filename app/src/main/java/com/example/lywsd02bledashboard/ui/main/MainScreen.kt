package com.example.lywsd02bledashboard.ui.main

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lywsd02bledashboard.R
import com.example.lywsd02bledashboard.model.ConnectionState
import com.example.lywsd02bledashboard.theme.BackgroundPaper
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary
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
import com.example.lywsd02bledashboard.ui.components.UpdateDialog

/**
 * LYWSD02 BLE 대시보드 메인 화면.
 * 원작 웹 대시보드(https://drslid.github.io/LYWSD02_BLE_Dashboard/)와
 * 동일한 디자인 및 센서 연동 기능을 제공하며,
 * 비한국어 단말 대응(기본 영어) 및 앱 내 실시간 언어 수동 전환을 완벽히 지원합니다.
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
            // 블루투스 권한 핸들러 (Activity Context 정상 보존으로 rememberLauncherForActivityResult 안전 실행)
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
                isAutoSyncEnabled = state.isAutoSyncClockEnabled,
                isSyncing = state.isSyncingClock,
                isConnected = state.connectionState == ConnectionState.CONNECTED,
                onRefreshClock = { viewModel.refreshClock() },
                onSyncClock = { viewModel.syncClock() },
                onClockModeChange = { viewModel.setClockMode(it) },
                onTimezoneChange = { viewModel.setTimezoneMinutes(it) },
                onSelectSystemTimezone = { viewModel.setSystemTimezone() },
                onAutoSyncChange = { viewModel.setAutoSyncClockEnabled(it) }
            )

            // 4. 액정 표시 온도 단위 및 앱 언어 수동 전환 설정 카드
            DisplaySettingsCard(
                currentUnit = state.selectedUnit,
                isUpdating = state.isUpdatingUnit,
                isConnected = state.connectionState == ConnectionState.CONNECTED,
                onSaveUnit = { viewModel.saveUnit(it) },
                currentLanguage = state.appLanguage,
                onLanguageChange = { newLang ->
                    viewModel.setAppLanguage(newLang)
                    (context as? Activity)?.recreate()
                }
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

            // 6. 최근 연결 기기 목록 카드
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

            // 8. 앱 버전 및 출시일자 푸터 정보 (v1.2.6)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LYWSD02 BLE Dashboard · v1.2.6",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.footer_release_date),
                    fontSize = 11.sp,
                    color = InkMuted.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // 하단 행: [업데이트 확인] 및 [수동 언어 선택 드롭다운]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 업데이트 확인 버튼
                    TextButton(
                        onClick = { viewModel.checkForUpdates(isManual = true) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.footer_check_updates),
                            tint = TealPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.footer_check_updates),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 수동 언어 선택 드롭다운 버튼 ('시스템기본값', '영문', '한글')
                    var expandedLanguageMenu by remember { mutableStateOf(false) }

                    Box {
                        TextButton(
                            onClick = { expandedLanguageMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = TealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val currentLangLabel = when (state.appLanguage) {
                                "en" -> stringResource(R.string.settings_lang_english)
                                "ko" -> stringResource(R.string.settings_lang_korean)
                                else -> stringResource(R.string.settings_lang_system)
                            }
                            Text(
                                text = currentLangLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedLanguageMenu,
                            onDismissRequest = { expandedLanguageMenu = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            // 1. 시스템기본값
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.settings_lang_system),
                                            fontWeight = if (state.appLanguage == "system") FontWeight.Bold else FontWeight.Medium,
                                            color = if (state.appLanguage == "system") TealPrimary else InkPrimary,
                                            fontSize = 13.sp
                                        )
                                        if (state.appLanguage == "system") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "✓", color = TealPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                onClick = {
                                    expandedLanguageMenu = false
                                    viewModel.setAppLanguage("system")
                                    (context as? Activity)?.recreate()
                                }
                            )

                            // 2. 영문 (English)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.settings_lang_english),
                                            fontWeight = if (state.appLanguage == "en") FontWeight.Bold else FontWeight.Medium,
                                            color = if (state.appLanguage == "en") TealPrimary else InkPrimary,
                                            fontSize = 13.sp
                                        )
                                        if (state.appLanguage == "en") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "✓", color = TealPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                onClick = {
                                    expandedLanguageMenu = false
                                    viewModel.setAppLanguage("en")
                                    (context as? Activity)?.recreate()
                                }
                            )

                            // 3. 한글 (한국어)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.settings_lang_korean),
                                            fontWeight = if (state.appLanguage == "ko") FontWeight.Bold else FontWeight.Medium,
                                            color = if (state.appLanguage == "ko") TealPrimary else InkPrimary,
                                            fontSize = 13.sp
                                        )
                                        if (state.appLanguage == "ko") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "✓", color = TealPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                onClick = {
                                    expandedLanguageMenu = false
                                    viewModel.setAppLanguage("ko")
                                    (context as? Activity)?.recreate()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 주변 센서 검색 결과 모달 다이얼로그
    if (state.isScanDialogOpen) {
        DeviceScanDialog(
            devices = state.scannedDevices,
            isScanning = state.connectionState == ConnectionState.SCANNING,
            onDismiss = { viewModel.closeScanDialog() },
            onRefreshScan = { viewModel.startScan() },
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

    // 깃허브 최신 릴리즈 인앱 업데이트 팝업 다이얼로그
    if (state.isUpdateDialogOpen && state.appUpdateInfo != null) {
        UpdateDialog(
            updateInfo = state.appUpdateInfo!!,
            downloadState = state.updateDownloadState,
            onDismiss = { viewModel.dismissUpdateDialog() },
            onStartDownload = { viewModel.startAppUpdate() },
            onInstall = { viewModel.installAppUpdate() }
        )
    }
}
