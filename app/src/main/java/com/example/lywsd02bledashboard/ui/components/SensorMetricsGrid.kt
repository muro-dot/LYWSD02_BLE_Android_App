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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.R
import com.example.lywsd02bledashboard.model.TemperatureUnit
import com.example.lywsd02bledashboard.theme.CyanAccent
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.InkSecondary
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
                text = stringResource(R.string.metric_live_title),
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
                        text = stringResource(R.string.metric_packet_count, updateCount),
                        fontSize = 11.sp,
                        color = InkMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 온도 & 습도 가로 2열 배치
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 온도 카드
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
                title = stringResource(R.string.metric_temp_title),
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
                title = stringResource(R.string.metric_humidity_title),
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

        Spacer(modifier = Modifier.height(8.dp))

        // 배터리 카드
        val displayBattery = if (battery != null) "$battery %" else "-- %"
        val batteryProgress = ((battery ?: 0) / 100f).coerceIn(0f, 1f)
        val animatedBattery by animateFloatAsState(targetValue = batteryProgress, label = "batProgress")

        val batHint = when {
            battery == null -> stringResource(R.string.metric_battery_waiting)
            battery <= 20 -> stringResource(R.string.metric_battery_low)
            battery <= 50 -> stringResource(R.string.metric_battery_fair)
            else -> stringResource(R.string.metric_battery_good)
        }
        val (batColor, batIcon) = when {
            battery == null -> Pair(InkMuted, Icons.Default.BatteryFull)
            battery <= 20 -> Pair(StatusRed, Icons.Default.BatteryAlert)
            battery <= 50 -> Pair(StatusAmber, Icons.Default.BatteryFull)
            else -> Pair(StatusGreen, Icons.Default.BatteryFull)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
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
                                .background(batColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = batIcon,
                                contentDescription = null,
                                tint = batColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.metric_battery_title),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkSecondary
                            )
                            Text(
                                text = batHint,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = batColor
                            )
                        }
                    }

                    Text(
                        text = displayBattery,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 배터리 프로그레스 바
                LinearProgressIndicator(
                    progress = { animatedBattery },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = batColor,
                    trackColor = SurfaceSoft
                )

                if (lastBatteryTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.metric_last_battery_updated, lastBatteryTime),
                        fontSize = 10.sp,
                        color = InkSecondary,
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
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkSecondary
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = InkPrimary,
                fontFamily = FontFamily.Monospace
            )

            if (progress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp)),
                    color = progressColor,
                    trackColor = SurfaceSoft
                )
            }

            if (timestamp != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.metric_last_updated, timestamp),
                    fontSize = 9.5.sp,
                    color = InkMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
