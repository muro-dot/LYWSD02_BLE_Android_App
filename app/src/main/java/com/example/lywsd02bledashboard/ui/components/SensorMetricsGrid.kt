package com.example.lywsd02bledashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.model.TemperatureUnit
import com.example.lywsd02bledashboard.theme.CyanAccent
import com.example.lywsd02bledashboard.theme.DeepTeal
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.StatusAmber
import com.example.lywsd02bledashboard.theme.StatusGreen
import com.example.lywsd02bledashboard.theme.StatusRed
import com.example.lywsd02bledashboard.theme.SurfaceSoft
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary
import java.util.Locale

/**
 * 실시간 온습도 및 배터리 측정 메트릭 카드 그리드
 */
@Composable
fun SensorMetricsGrid(
    temperatureC: Float?,
    humidity: Int?,
    battery: Int?,
    unit: TemperatureUnit,
    lastMeasurementTime: String?,
    lastBatteryTime: String?,
    updateCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "실시간 센서 측정",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            if (updateCount > 0) {
                Surface(
                    color = SurfaceSoft,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "수신 횟수: $updateCount 회",
                        fontSize = 11.sp,
                        color = InkMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 온도 & 습도 가로 2열 배치 (상하 길이 동일하게 맞춤)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 온도 카드 (0°C ~ 40°C 기준 프로그레스 바 포함하여 습도 카드와 대칭 높이 유지)
            val displayTemp = if (temperatureC != null) {
                if (unit == TemperatureUnit.FAHRENHEIT) {
                    val f = (temperatureC * 9f / 5f) + 32f
                    String.format(Locale.US, "%.1f °F", f)
                } else {
                    String.format(Locale.US, "%.1f °C", temperatureC)
                }
            } else {
                "--.- °C"
            }
            val tempProgress = if (temperatureC != null) {
                ((temperatureC - 0f) / 40f).coerceIn(0f, 1f)
            } else 0f
            val animatedTemp by animateFloatAsState(targetValue = tempProgress, label = "tempProgress")

            MetricCard(
                title = "현재 온도",
                value = displayTemp,
                icon = Icons.Default.DeviceThermostat,
                iconTint = TealPrimary,
                progress = animatedTemp,
                progressColor = TealPrimary,
                timestamp = lastMeasurementTime,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 습도 카드
            val displayHumidity = if (humidity != null) "$humidity %" else "-- %"
            val humidityProgress = ((humidity ?: 0) / 100f).coerceIn(0f, 1f)
            val animatedHumidity by animateFloatAsState(targetValue = humidityProgress, label = "humProgress")

            MetricCard(
                title = "상대 습도",
                value = displayHumidity,
                icon = Icons.Default.WaterDrop,
                iconTint = CyanAccent,
                progress = animatedHumidity,
                progressColor = CyanAccent,
                timestamp = lastMeasurementTime,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 배터리 카드
        val displayBattery = if (battery != null) "$battery %" else "-- %"
        val batteryProgress = ((battery ?: 0) / 100f).coerceIn(0f, 1f)
        val animatedBattery by animateFloatAsState(targetValue = batteryProgress, label = "batProgress")

        val (batColor, batIcon, batHint) = when {
            battery == null -> Triple(InkMuted, Icons.Default.BatteryFull, "측정 대기 중")
            battery <= 20 -> Triple(StatusRed, Icons.Default.BatteryAlert, "배터리 부족 (교체 권장)")
            battery <= 50 -> Triple(StatusAmber, Icons.Default.BatteryFull, "양호")
            else -> Triple(StatusGreen, Icons.Default.BatteryFull, "충분함")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                .background(batColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = batIcon,
                                contentDescription = null,
                                tint = batColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "배터리 잔량",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = InkMuted
                            )
                            Text(
                                text = batHint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = batColor
                            )
                        }
                    }

                    Text(
                        text = displayBattery,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 배터리 프로그레스 바
                LinearProgressIndicator(
                    progress = { animatedBattery },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = batColor,
                    trackColor = SurfaceSoft
                )

                if (lastBatteryTime != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "마지막 갱신: $lastBatteryTime",
                        fontSize = 11.sp,
                        color = InkMuted,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    timestamp: String?,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    progressColor: Color = TealPrimary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkMuted
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = InkPrimary,
                fontFamily = FontFamily.Monospace
            )

            if (progress != null) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = SurfaceSoft
                )
            }

            if (timestamp != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "갱신: $timestamp",
                    fontSize = 10.sp,
                    color = InkMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
