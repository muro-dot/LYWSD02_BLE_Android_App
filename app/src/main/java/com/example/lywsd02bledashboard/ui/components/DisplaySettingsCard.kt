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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.R
import com.example.lywsd02bledashboard.model.TemperatureUnit
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.CyanAccent
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary

/**
 * 센서 액정 표시 단위 (°C / °F) 설정 카드
 * 의도: 사용자가 센서 액정의 온도 단위를 직관적인 세그먼트 버튼으로 원클릭 설정할 수 있도록 제공합니다.
 * (앱 언어 설정은 화면 최하단 전용 드롭다운 메뉴로 일원화되어 본 카드에서는 제외되었습니다.)
 */
@Composable
fun DisplaySettingsCard(
    currentUnit: TemperatureUnit,
    isUpdating: Boolean,
    isConnected: Boolean,
    onSaveUnit: (TemperatureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 카드 상단 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_card_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.settings_card_subtitle),
                        fontSize = 10.5.sp,
                        color = InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 센서 액정 온도 단위 설정 (세그먼트 토글 버튼)
            Text(
                text = stringResource(R.string.settings_temp_unit_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 섭씨 (°C) 선택 버튼
                OutlinedButton(
                    onClick = { onSaveUnit(TemperatureUnit.CELSIUS) },
                    enabled = isConnected && !isUpdating,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (currentUnit == TemperatureUnit.CELSIUS) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = TealPrimary.copy(alpha = 0.12f),
                            contentColor = TealPrimary
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = InkPrimary
                        )
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentUnit == TemperatureUnit.CELSIUS) TealPrimary else BorderLine
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    if (isUpdating && currentUnit != TemperatureUnit.CELSIUS) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TealPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = stringResource(R.string.settings_unit_celsius),
                        fontSize = 11.5.sp,
                        fontWeight = if (currentUnit == TemperatureUnit.CELSIUS) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // 화씨 (°F) 선택 버튼
                OutlinedButton(
                    onClick = { onSaveUnit(TemperatureUnit.FAHRENHEIT) },
                    enabled = isConnected && !isUpdating,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (currentUnit == TemperatureUnit.FAHRENHEIT) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = TealPrimary.copy(alpha = 0.12f),
                            contentColor = TealPrimary
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = InkPrimary
                        )
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentUnit == TemperatureUnit.FAHRENHEIT) TealPrimary else BorderLine
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    if (isUpdating && currentUnit != TemperatureUnit.FAHRENHEIT) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TealPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = stringResource(R.string.settings_unit_fahrenheit),
                        fontSize = 11.5.sp,
                        fontWeight = if (currentUnit == TemperatureUnit.FAHRENHEIT) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
