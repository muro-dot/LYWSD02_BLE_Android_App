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
        Column(modifier = Modifier.padding(16.dp)) {
            // 카드 상단 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_card_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.settings_card_subtitle),
                        fontSize = 11.sp,
                        color = InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. 센서 액정 온도 단위 설정 섹션
            Text(
                text = stringResource(R.string.settings_temp_unit_title),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedUnit == TemperatureUnit.CELSIUS,
                        onClick = { selectedUnit = TemperatureUnit.CELSIUS },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(
                        text = stringResource(R.string.settings_unit_celsius),
                        fontSize = 13.sp,
                        color = InkPrimary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    RadioButton(
                        selected = selectedUnit == TemperatureUnit.FAHRENHEIT,
                        onClick = { selectedUnit = TemperatureUnit.FAHRENHEIT },
                        colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(
                        text = stringResource(R.string.settings_unit_fahrenheit),
                        fontSize = 13.sp,
                        color = InkPrimary
                    )
                }

                Button(
                    onClick = { onSaveUnit(selectedUnit) },
                    enabled = isConnected && !isUpdating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.settings_btn_saving),
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.settings_btn_save_unit),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLine.copy(alpha = 0.7f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // 2. 앱 표시 언어 수동 전환 섹션
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_language_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.settings_language_subtitle),
                        fontSize = 11.sp,
                        color = InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 시스템 기본 (Follow System)
                RadioButton(
                    selected = currentLanguage == "system",
                    onClick = { onLanguageChange("system") },
                    colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(
                    text = stringResource(R.string.settings_lang_system),
                    fontSize = 12.sp,
                    color = InkPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 한국어
                RadioButton(
                    selected = currentLanguage == "ko",
                    onClick = { onLanguageChange("ko") },
                    colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(
                    text = stringResource(R.string.settings_lang_korean),
                    fontSize = 12.sp,
                    color = InkPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                // English
                RadioButton(
                    selected = currentLanguage == "en",
                    onClick = { onLanguageChange("en") },
                    colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(
                    text = stringResource(R.string.settings_lang_english),
                    fontSize = 12.sp,
                    color = InkPrimary
                )
            }
        }
    }
}
