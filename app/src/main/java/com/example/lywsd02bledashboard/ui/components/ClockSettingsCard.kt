package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.ble.BleProtocolParser
import com.example.lywsd02bledashboard.model.BleConstants
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.BorderLineStrong
import com.example.lywsd02bledashboard.theme.CyanAccent
import com.example.lywsd02bledashboard.theme.DeepTeal
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.StatusAmber
import com.example.lywsd02bledashboard.theme.StatusGreen
import com.example.lywsd02bledashboard.theme.SurfaceSoft
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary

/**
 * 기기 시계 및 시간 동기화 설정 카드
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockSettingsCard(
    deviceTimeFormatted: String?,
    deviceTimezoneMinutes: Int?,
    targetTimezoneMinutes: Int,
    isUsingSystemTimezone: Boolean,
    clockDriftSeconds: Long?,
    clockMode: ClockDisplayMode,
    isAutoSyncEnabled: Boolean,
    isSyncing: Boolean,
    isConnected: Boolean,
    onRefreshClock: () -> Unit,
    onSyncClock: () -> Unit,
    onClockModeChange: (ClockDisplayMode) -> Unit,
    onTimezoneChange: (Int) -> Unit,
    onSelectSystemTimezone: () -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 헤더 및 시계 새로고침 버튼
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
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "기기 시계 및 동기화",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "스마트폰 시간 기준 동기화",
                            fontSize = 11.sp,
                            color = InkMuted
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshClock,
                    enabled = isConnected,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "시간 새로고침",
                        tint = if (isConnected) TealPrimary else InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 기기 현재 시계 디스플레이
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceSoft)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "센서 현재 시간",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = deviceTimeFormatted ?: "--:--:--",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // 시간 오차 상태
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (deviceTimezoneMinutes != null) {
                                "센서: ${BleProtocolParser.formatTimezoneOffset(deviceTimezoneMinutes)}"
                            } else {
                                "센서: --:--"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val isSynced = clockDriftSeconds != null && Math.abs(clockDriftSeconds) <= 1
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSynced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSynced) StatusGreen else StatusAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = BleProtocolParser.formatDriftText(clockDriftSeconds),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSynced) StatusGreen else StatusAmber
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 타임존 드롭다운
            var expandedTimezone by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTimezone,
                onExpandedChange = { expandedTimezone = !expandedTimezone },
                modifier = Modifier.fillMaxWidth()
            ) {
                val textFieldValue = if (isUsingSystemTimezone) {
                    "📱 ${BleProtocolParser.formatTimezoneOffset(targetTimezoneMinutes)} (${getTimezoneDescription(targetTimezoneMinutes)})"
                } else {
                    "${BleProtocolParser.formatTimezoneOffset(targetTimezoneMinutes)} (${getTimezoneDescription(targetTimezoneMinutes)})"
                }
                androidx.compose.material3.OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {},
                    readOnly = true,
                    label = { 
                        Text(
                            text = if (isUsingSystemTimezone) "목표 타임존 (스마트폰 연동 중)" else "목표 타임존 (수동 설정)", 
                            color = InkPrimary, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ) 
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary,
                        disabledTextColor = InkPrimary,
                        focusedLabelColor = TealPrimary,
                        unfocusedLabelColor = InkPrimary,
                        disabledLabelColor = InkPrimary,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderLineStrong,
                        disabledBorderColor = BorderLine
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary,
                        fontSize = 13.sp
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimezone) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedTimezone,
                    onDismissRequest = { expandedTimezone = false },
                    modifier = Modifier.background(SurfaceWhite)
                ) {
                    val systemOffset = BleProtocolParser.getSystemTimezoneOffsetMinutes()
                    // 1. 스마트폰 시스템 타임존 (항상 최상단에 노출)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📱", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "스마트폰 시스템 타임존",
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary,
                                            fontSize = 13.sp
                                        )
                                        if (isUsingSystemTimezone) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "현재 선택됨",
                                                color = StatusGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${BleProtocolParser.formatTimezoneOffset(systemOffset)} (${getTimezoneDescription(systemOffset)})",
                                        color = InkPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelectSystemTimezone()
                            expandedTimezone = false
                        }
                    )
                    HorizontalDivider(color = BorderLine, modifier = Modifier.padding(vertical = 4.dp))

                    // 2. 전세계 표준 타임존 목록
                    BleConstants.TIMEZONE_OFFSETS.forEach { offset ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${BleProtocolParser.formatTimezoneOffset(offset)} (${getTimezoneDescription(offset)})",
                                    color = InkPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                onTimezoneChange(offset)
                                expandedTimezone = false
                            }
                        )
                    }
                }
            }

            if (!isUsingSystemTimezone) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = onSelectSystemTimezone
                    ) {
                        Text(
                            text = "📱 폰 시스템 타임존으로 되돌리기",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 12시간 / 24시간 표시 모드 라디오
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "표시 형식:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = clockMode == ClockDisplayMode.MODE_24H,
                        onClick = { onClockModeChange(ClockDisplayMode.MODE_24H) },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(text = "24시간", fontSize = 13.sp, color = InkPrimary)

                    Spacer(modifier = Modifier.width(12.dp))

                    RadioButton(
                        selected = clockMode == ClockDisplayMode.MODE_12H,
                        onClick = { onClockModeChange(ClockDisplayMode.MODE_12H) },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(text = "12시간 (MMC)", fontSize = 13.sp, color = InkPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 자동 동기화 체크박스 (10초 이상 드리프트 발생 시 연결 즉시 자동 동기화)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAutoSyncEnabled,
                    onCheckedChange = onAutoSyncChange,
                    colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "연결 시 10초 이상 오차 발생 시 자동 시계 보정",
                    fontSize = 12.sp,
                    color = InkPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 시계 동기화 실행 버튼
            Button(
                onClick = onSyncClock,
                enabled = isConnected && !isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "시계 동기화 중...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "지금 시계 동기화", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getTimezoneDescription(minutes: Int): String {
    return when (minutes) {
        540 -> "대한민국, 일본 (KST/JST)"
        480 -> "중국, 대만, 싱가포르 (CST)"
        420 -> "베트남, 태국, 인도네시아 서부 (ICT)"
        330 -> "인도, 스리랑카 (IST)"
        240 -> "UAE, 두바이 (GST)"
        180 -> "사우디, 튀르키예, 모스크바 (MSK/AST)"
        120 -> "그리스, 이집트, 남아공 (EET/SAST)"
        60 -> "중앙유럽 (CET)"
        0 -> "그리니치, 영국, 포르투갈 (UTC/GMT)"
        -180 -> "브라질, 아르헨티나 (BRT/ART)"
        -300 -> "미국 동부 (EST)"
        -360 -> "미국 중부 (CST)"
        -420 -> "미국 산악 (MST)"
        -480 -> "미국 서부 (PST)"
        -540 -> "알래스카 (AKST)"
        -600 -> "하와이 (HST)"
        600 -> "호주 동부, 괌 (AEST/ChST)"
        720 -> "뉴질랜드 (NZST)"
        else -> "오프셋 $minutes 분"
    }
}
