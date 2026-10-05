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
    deviceTimezoneMinutes: Int,
    clockDriftSeconds: Long?,
    clockMode: ClockDisplayMode,
    manualOffsetMinutes: Int,
    isAutoSyncEnabled: Boolean,
    isSyncing: Boolean,
    isConnected: Boolean,
    onRefreshClock: () -> Unit,
    onSyncClock: () -> Unit,
    onClockModeChange: (ClockDisplayMode) -> Unit,
    onTimezoneChange: (Int) -> Unit,
    onManualOffsetChange: (Int) -> Unit,
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
                            text = BleProtocolParser.formatTimezoneOffset(deviceTimezoneMinutes),
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
                onExpandedChange = { if (isConnected) expandedTimezone = !expandedTimezone },
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = "${BleProtocolParser.formatTimezoneOffset(deviceTimezoneMinutes)} (${getTimezoneDescription(deviceTimezoneMinutes)})",
                    onValueChange = {},
                    readOnly = true,
                    label = { 
                        Text(
                            text = "목표 타임존 (Timezone)", 
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
                        fontSize = 14.sp
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

            Spacer(modifier = Modifier.height(10.dp))

            // 수동 오차 보정 슬라이더 (-30분 ~ +30분)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "수동 시간 오차 조정",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkPrimary
                    )
                    Text(
                        text = "${manualOffsetMinutes}분",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }

                Slider(
                    value = manualOffsetMinutes.toFloat(),
                    onValueChange = { onManualOffsetChange(it.toInt()) },
                    valueRange = -30f..30f,
                    steps = 59,
                    colors = SliderDefaults.colors(
                        thumbColor = TealPrimary,
                        activeTrackColor = TealPrimary,
                        inactiveTrackColor = SurfaceSoft
                    ),
                    enabled = isConnected
                )
            }

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
        0 -> "그리니치, 영국 (UTC/GMT)"
        60 -> "중앙유럽 (CET)"
        -300 -> "미국 동부 (EST)"
        -360 -> "미국 중부 (CST)"
        -420 -> "미국 산악 (MST)"
        -480 -> "미국 서부 (PST)"
        330 -> "인도 (IST)"
        else -> "오프셋 $minutes 분"
    }
}
