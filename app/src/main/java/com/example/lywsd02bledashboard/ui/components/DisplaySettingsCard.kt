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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
 * 센서 표시 단위 (°C / °F) 및 앱 표시 언어(한국어 / English / 시스템 기본) 설정 카드
 * 의도: 사용자가 센서 액정의 온도 단위와 앱 전역 표시 언어를 한곳에서 편리하게 설정할 수 있도록 제공합니다.
 */
@Composable
fun DisplaySettingsCard(
    currentUnit: TemperatureUnit,
    isUpdating: Boolean,
    isConnected: Boolean,
    onSaveUnit: (TemperatureUnit) -> Unit,
    currentLanguage: String = "system",
    onLanguageChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedUnit by remember(currentUnit) { mutableStateOf(currentUnit) }

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

            // 1. 센서 액정 온도 단위 설정 (세그먼트 토글 버튼으로 찌그러짐 원천 방지)
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
                androidx.compose.material3.OutlinedButton(
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
                androidx.compose.material3.OutlinedButton(
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

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderLine.copy(alpha = 0.7f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // 2. 앱 표시 언어 수동 전환 섹션
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_language_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.settings_language_subtitle),
                        fontSize = 10.5.sp,
                        color = InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 시스템 기본 (Follow System)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLanguageChange("system") }
                ) {
                    RadioButton(
                        selected = currentLanguage == "system",
                        onClick = { onLanguageChange("system") },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stringResource(R.string.settings_lang_system),
                        fontSize = 11.5.sp,
                        color = InkPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // 한국어
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLanguageChange("ko") }
                ) {
                    RadioButton(
                        selected = currentLanguage == "ko",
                        onClick = { onLanguageChange("ko") },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stringResource(R.string.settings_lang_korean),
                        fontSize = 11.5.sp,
                        color = InkPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // English
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLanguageChange("en") }
                ) {
                    RadioButton(
                        selected = currentLanguage == "en",
                        onClick = { onLanguageChange("en") },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stringResource(R.string.settings_lang_english),
                        fontSize = 11.5.sp,
                        color = InkPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
