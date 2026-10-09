package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.model.ScannedDeviceInfo
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.BorderLineStrong
import com.example.lywsd02bledashboard.theme.CyanAccent
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.InkSecondary
import com.example.lywsd02bledashboard.theme.SurfaceSoft
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary

/**
 * 주변의 LYWSD02 센서 검색 결과 모달 다이얼로그 (순백색 라이트 테마)
 */
@Composable
fun DeviceScanDialog(
    devices: List<ScannedDeviceInfo>,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onRefreshScan: () -> Unit,
    onDeviceSelect: (ScannedDeviceInfo) -> Unit
) {
    val listState = rememberLazyListState()

    // 1순위(LYWSD02)와 2순위(기타 BLE 기기) 분리
    val (lywsdDevices, otherDevices) = remember(devices) {
        devices.partition { it.isLywsd02 }
    }

    // 1순위 기기가 새로 추가되거나 첫 항목이 바뀔 때 애니메이션 지연 없이 즉각 0번 인덱스로 스크롤 고정
    val topKey = devices.firstOrNull()?.address
    LaunchedEffect(topKey, lywsdDevices.size) {
        if (!listState.isScrollInProgress) {
            listState.scrollToItem(0)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        titleContentColor = InkPrimary,
        textContentColor = InkPrimary,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "기기 검색",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = TealPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    IconButton(
                        onClick = onRefreshScan,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "다시 검색",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isScanning) "주변의 BLE 기기를 검색 중입니다" else "주변의 BLE 기기 검색을 완료하였습니다",
                    fontSize = 13.sp,
                    color = if (isScanning) InkSecondary else TealPrimary,
                    fontWeight = if (isScanning) FontWeight.Normal else FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (devices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceSoft)
                            .border(1.dp, BorderLine, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = TealPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "주변 신호를 탐색하고 있습니다...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = InkPrimary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 290.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceSoft)
                            .border(1.dp, BorderLine, RoundedCornerShape(10.dp))
                    ) {
                        // 1순위: LYWSD02 기기 목록 (최상단 고정 노출)
                        if (lywsdDevices.isNotEmpty()) {
                            item(key = "header_lywsd") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TealPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "⭐ 감지된 LYWSD02 센서 (${lywsdDevices.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary
                                    )
                                }
                            }
                            items(lywsdDevices, key = { it.address }) { dev ->
                                DeviceItemRow(dev = dev, isLywsd02 = true, onSelect = onDeviceSelect)
                                HorizontalDivider(color = BorderLine.copy(alpha = 0.6f), thickness = 0.8.dp)
                            }
                        }

                        // 2순위: 기타 주변 BLE 기기 목록
                        if (otherDevices.isNotEmpty()) {
                            if (lywsdDevices.isNotEmpty()) {
                                item(key = "header_other") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(BorderLine.copy(alpha = 0.3f))
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "기타 주변 BLE 기기 (${otherDevices.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = InkSecondary
                                        )
                                    }
                                }
                            }
                            items(otherDevices, key = { it.address }) { dev ->
                                DeviceItemRow(dev = dev, isLywsd02 = false, onSelect = onDeviceSelect)
                                HorizontalDivider(color = BorderLine.copy(alpha = 0.6f), thickness = 0.8.dp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderLineStrong),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "닫기",
                    color = InkPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    )
}

/**
 * 스캔된 BLE 기기 항목 행(Row) 컴포넌트
 */
@Composable
private fun DeviceItemRow(
    dev: ScannedDeviceInfo,
    isLywsd02: Boolean,
    onSelect: (ScannedDeviceInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isLywsd02) TealPrimary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onSelect(dev) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isLywsd02) Icons.Default.Sensors else Icons.Default.Bluetooth,
                contentDescription = null,
                tint = if (isLywsd02) TealPrimary else InkSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dev.displayName,
                    fontSize = 14.sp,
                    fontWeight = if (isLywsd02) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isLywsd02) TealPrimary else InkPrimary
                )
                val subText = if (!dev.alias.isNullOrBlank()) "${dev.name} · ${dev.address}" else dev.address
                Text(
                    text = subText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = InkSecondary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SignalCellularAlt,
                contentDescription = "신호 세기",
                tint = if (isLywsd02) TealPrimary else CyanAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${dev.rssi} dBm",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
