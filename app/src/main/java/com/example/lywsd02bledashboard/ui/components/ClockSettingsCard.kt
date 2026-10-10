package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.R
import com.example.lywsd02bledashboard.ble.BleProtocolParser
import com.example.lywsd02bledashboard.model.BleConstants
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.BorderLineStrong
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
        Column(modifier = Modifier.padding(12.dp)) {
            // 헤더 및 시계 새로고침 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.clock_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.clock_subtitle),
                            fontSize = 10.5.sp,
                            color = InkMuted
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshClock,
                    enabled = isConnected,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.clock_cd_refresh),
                        tint = if (isConnected) TealPrimary else InkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 기기 현재 시계 디스플레이
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceSoft)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.clock_device_current_time),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = deviceTimeFormatted ?: "--:--:--",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // 시간 오차 상태
                    Column(horizontalAlignment = Alignment.End) {
                        val tzString = if (deviceTimezoneMinutes != null) {
                            BleProtocolParser.formatTimezoneOffset(deviceTimezoneMinutes)
                        } else {
                            "--:--"
                        }
                        Text(
                            text = stringResource(R.string.clock_sensor_timezone, tzString),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val isSynced = clockDriftSeconds != null && Math.abs(clockDriftSeconds) <= 1
                        val driftText = when {
                            clockDriftSeconds == null -> stringResource(R.string.drift_none)
                            Math.abs(clockDriftSeconds) <= 1 -> stringResource(R.string.drift_synced, Math.abs(clockDriftSeconds))
                            clockDriftSeconds > 0 -> stringResource(R.string.drift_fast, Math.abs(clockDriftSeconds))
                            else -> stringResource(R.string.drift_slow, Math.abs(clockDriftSeconds))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSynced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSynced) StatusGreen else StatusAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = driftText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSynced) StatusGreen else StatusAmber
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 타임존 드롭다운 (단일 행 고정 및 텍스트 줄바꿈 방지)
            var expandedTimezone by remember { mutableStateOf(false) }
            val tzDesc = getTimezoneDescriptionRes(targetTimezoneMinutes)

            ExposedDropdownMenuBox(
                expanded = expandedTimezone,
                onExpandedChange = { expandedTimezone = !expandedTimezone },
                modifier = Modifier.fillMaxWidth()
            ) {
                val textFieldValue = if (isUsingSystemTimezone) {
                    "📱 ${BleProtocolParser.formatTimezoneOffset(targetTimezoneMinutes)} ($tzDesc)"
                } else {
                    "${BleProtocolParser.formatTimezoneOffset(targetTimezoneMinutes)} ($tzDesc)"
                }
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    maxLines = 1,
                    label = { 
                        Text(
                            text = if (isUsingSystemTimezone) {
                                stringResource(R.string.clock_target_timezone_system)
                            } else {
                                stringResource(R.string.clock_target_timezone_manual)
                            }, 
                            color = InkPrimary, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            maxLines = 1
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
                        fontSize = 12.sp
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
                    val systemTzDesc = getTimezoneDescriptionRes(systemOffset)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📱", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.clock_phone_system_timezone),
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary,
                                            fontSize = 12.sp
                                        )
                                        if (isUsingSystemTimezone) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.clock_currently_selected),
                                                color = StatusGreen,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${BleProtocolParser.formatTimezoneOffset(systemOffset)} ($systemTzDesc)",
                                        color = InkPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp
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
                        val itemDesc = getTimezoneDescriptionRes(offset)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${BleProtocolParser.formatTimezoneOffset(offset)} ($itemDesc)",
                                    color = InkPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
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
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onSelectSystemTimezone,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.clock_revert_to_system_tz),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 12시간 / 24시간 표시 모드 라디오 (한 줄 유지 컴팩트 디자인)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.clock_display_format),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onClockModeChange(ClockDisplayMode.MODE_24H) }
                    ) {
                        RadioButton(
                            selected = clockMode == ClockDisplayMode.MODE_24H,
                            onClick = { onClockModeChange(ClockDisplayMode.MODE_24H) },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.clock_format_24h),
                            fontSize = 12.sp,
                            color = InkPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onClockModeChange(ClockDisplayMode.MODE_12H) }
                    ) {
                        RadioButton(
                            selected = clockMode == ClockDisplayMode.MODE_12H,
                            onClick = { onClockModeChange(ClockDisplayMode.MODE_12H) },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.clock_format_12h),
                            fontSize = 12.sp,
                            color = InkPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 자동 동기화 체크박스 (컴팩트)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAutoSyncEnabled,
                    onCheckedChange = onAutoSyncChange,
                    colors = CheckboxDefaults.colors(checkedColor = TealPrimary),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.clock_auto_sync_label),
                    fontSize = 11.5.sp,
                    color = InkPrimary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 시계 동기화 실행 버튼
            Button(
                onClick = onSyncClock,
                enabled = isConnected && !isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.clock_btn_syncing),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.clock_btn_sync_now),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun getTimezoneDescriptionRes(minutes: Int): String {
    return when (minutes) {
        -720 -> stringResource(R.string.tz_utc_minus_12)
        -660 -> stringResource(R.string.tz_utc_minus_11)
        -600 -> stringResource(R.string.tz_hst)
        -540 -> stringResource(R.string.tz_akst)
        -480 -> stringResource(R.string.tz_pst)
        -420 -> stringResource(R.string.tz_mst)
        -360 -> stringResource(R.string.tz_cst_us)
        -300 -> stringResource(R.string.tz_est)
        -240 -> stringResource(R.string.tz_ast_clt)
        -180 -> stringResource(R.string.tz_brt_art)
        -120 -> stringResource(R.string.tz_utc_minus_2)
        -60 -> stringResource(R.string.tz_azot_cvt)
        0 -> stringResource(R.string.tz_utc_gmt)
        60 -> stringResource(R.string.tz_cet)
        120 -> stringResource(R.string.tz_eet_sast)
        180 -> stringResource(R.string.tz_msk_ast)
        240 -> stringResource(R.string.tz_gst)
        300 -> stringResource(R.string.tz_pkt_uzt)
        330 -> stringResource(R.string.tz_ist)
        360 -> stringResource(R.string.tz_bst)
        420 -> stringResource(R.string.tz_ict)
        480 -> stringResource(R.string.tz_cst)
        540 -> stringResource(R.string.tz_kst_jst)
        600 -> stringResource(R.string.tz_aest)
        660 -> stringResource(R.string.tz_sbt)
        720 -> stringResource(R.string.tz_nzst)
        780 -> stringResource(R.string.tz_tot)
        840 -> stringResource(R.string.tz_lint)
        else -> stringResource(R.string.tz_offset_minutes, minutes)
    }
}
