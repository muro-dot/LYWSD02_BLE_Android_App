package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.model.HistoryRecord
import com.example.lywsd02bledashboard.model.TemperatureUnit
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.BorderLineStrong
import com.example.lywsd02bledashboard.theme.CyanAccent
import com.example.lywsd02bledashboard.theme.DeepTeal
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.SurfaceSoft
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary
import java.util.Locale

/**
 * 센서 과거 기록(최대 96시간 통계) 조회 및 CSV 내보내기 카드
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCard(
    historyRecords: List<HistoryRecord>,
    historyLimit: Int,
    isLoading: Boolean,
    statusMessage: String?,
    unit: TemperatureUnit,
    isConnected: Boolean,
    onLimitChange: (Int) -> Unit,
    onLoadHistory: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 헤더 (좌측 타이틀 + 우측 CSV 내보내기 버튼)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "과거 온습도 기록",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "센서 내장 메모리 통계",
                            fontSize = 11.sp,
                            color = InkMuted,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                // CSV 내보내기 버튼 (세로 줄바꿈 완벽 방지 및 고대비 색상 적용)
                OutlinedButton(
                    onClick = onExportCsv,
                    enabled = historyRecords.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        if (historyRecords.isNotEmpty()) TealPrimary else BorderLineStrong
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TealPrimary,
                        disabledContentColor = InkPrimary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .defaultMinSize(minWidth = 105.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "CSV 내보내기",
                        modifier = Modifier.size(13.dp),
                        tint = if (historyRecords.isNotEmpty()) TealPrimary else InkPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CSV 내보내기",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (historyRecords.isNotEmpty()) TealPrimary else InkPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 조회 개수 선택 및 불러오기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var expandedLimit by remember { mutableStateOf(false) }
                val limits = listOf(24, 48, 96)

                ExposedDropdownMenuBox(
                    expanded = expandedLimit,
                    onExpandedChange = { if (isConnected && !isLoading) expandedLimit = !expandedLimit },
                    modifier = Modifier.weight(1f)
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = "최근 $historyLimit 개",
                        onValueChange = {},
                        readOnly = true,
                        label = { 
                            Text(
                                text = "조회 개수", 
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLimit) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedLimit,
                        onDismissRequest = { expandedLimit = false }
                    ) {
                        limits.forEach { lim ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "최근 $lim 개",
                                        color = InkPrimary,
                                        fontWeight = FontWeight.Medium
                                    ) 
                                },
                                onClick = {
                                    onLimitChange(lim)
                                    expandedLimit = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onLoadHistory,
                    enabled = isConnected && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.15f)
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "수집 중...", 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "기록 불러오기", 
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 기록 테이블 헤더 및 리스트
            if (historyRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceSoft)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) "센서로부터 기록을 수신하고 있습니다..." else "불러온 과거 기록이 없습니다. '기록 불러오기'를 누르세요.",
                        fontSize = 12.sp,
                        color = InkMuted
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceSoft)
                ) {
                    // 테이블 헤더
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE2EBE8))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "시각", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkPrimary, modifier = Modifier.weight(1.4f))
                        Text(text = "최고 온/습", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkPrimary, modifier = Modifier.weight(1f))
                        Text(text = "최저 온/습", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkPrimary, modifier = Modifier.weight(1f))
                    }

                    // 테이블 행 목록 (최대 높이 220dp 스크롤)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(historyRecords) { item ->
                            val maxTempDisplay = formatTemperature(item.maxTemperature, unit)
                            val minTempDisplay = formatTemperature(item.minTemperature, unit)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.formattedDateTime(),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = InkPrimary,
                                    modifier = Modifier.weight(1.4f)
                                )
                                Text(
                                    text = "$maxTempDisplay / ${item.maxHumidity}%",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFC0392B),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$minTempDisplay / ${item.minHumidity}%",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF2980B9),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = BorderLine.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTemperature(celsius: Float, unit: TemperatureUnit): String {
    return if (unit == TemperatureUnit.FAHRENHEIT) {
        val f = (celsius * 9f / 5f) + 32f
        String.format(Locale.US, "%.1f°F", f)
    } else {
        String.format(Locale.US, "%.1f°C", celsius)
    }
}
