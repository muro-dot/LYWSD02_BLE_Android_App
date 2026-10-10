package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.R
import com.example.lywsd02bledashboard.model.HistoryRecord
import com.example.lywsd02bledashboard.model.TemperatureUnit
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.BorderLineStrong
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
        Column(modifier = Modifier.padding(12.dp)) {
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.history_card_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.history_card_subtitle),
                            fontSize = 11.sp,
                            color = InkMuted,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                // CSV 내보내기 버튼
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .defaultMinSize(minWidth = 96.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.history_cd_export_csv),
                        modifier = Modifier.size(12.dp),
                        tint = if (historyRecords.isNotEmpty()) TealPrimary else InkPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.history_btn_export_csv),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (historyRecords.isNotEmpty()) TealPrimary else InkPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    OutlinedTextField(
                        value = stringResource(R.string.history_recent_items, historyLimit),
                        onValueChange = {},
                        readOnly = true,
                        label = { 
                            Text(
                                text = stringResource(R.string.history_query_count_label), 
                                color = InkPrimary, 
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLimit) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedLimit,
                        onDismissRequest = { expandedLimit = false },
                        modifier = Modifier.background(SurfaceWhite)
                    ) {
                        limits.forEach { lim ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = stringResource(R.string.history_recent_items, lim),
                                        color = InkPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
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
                        .height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.history_btn_loading), 
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
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
                            text = stringResource(R.string.history_btn_fetch), 
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusMessage,
                    fontSize = 11.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 기록 테이블 헤더 및 리스트
            if (historyRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceSoft)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) {
                            stringResource(R.string.history_empty_loading)
                        } else {
                            stringResource(R.string.history_empty_idle)
                        },
                        fontSize = 11.sp,
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
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.history_col_time), 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = InkPrimary, 
                            modifier = Modifier.weight(1.4f)
                        )
                        Text(
                            text = stringResource(R.string.history_col_max), 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = InkPrimary, 
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.history_col_min), 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = InkPrimary, 
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 테이블 행 목록
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                    ) {
                        items(historyRecords) { item ->
                            val maxTempDisplay = formatTemperature(item.maxTemperature, unit)
                            val minTempDisplay = formatTemperature(item.minTemperature, unit)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
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
