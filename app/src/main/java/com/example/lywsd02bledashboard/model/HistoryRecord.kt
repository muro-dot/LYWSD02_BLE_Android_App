package com.example.lywsd02bledashboard.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 센서 내부에 기록된 1시간 단위 온습도 통계 레코드 (14바이트 패킷 모델)
 */
data class HistoryRecord(
    val index: Long,
    val timestamp: Long,
    val maxTemperature: Float,
    val maxHumidity: Int,
    val minTemperature: Float,
    val minHumidity: Int
) {
    /**
     * 타임스탬프를 읽기 쉬운 로컬 날짜/시각 문자열로 변환합니다.
     */
    fun formattedDateTime(): String {
        val date = Date(timestamp * 1000)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    /**
     * CSV 행 문자열로 변환합니다.
     */
    fun toCsvRow(unit: TemperatureUnit): String {
        val maxTempConverted = if (unit == TemperatureUnit.FAHRENHEIT) (maxTemperature * 9f / 5f) + 32f else maxTemperature
        val minTempConverted = if (unit == TemperatureUnit.FAHRENHEIT) (minTemperature * 9f / 5f) + 32f else minTemperature
        val unitLabel = if (unit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"
        return "$index,${formattedDateTime()},$timestamp,${String.format(Locale.US, "%.2f", maxTempConverted)},$maxHumidity,${String.format(Locale.US, "%.2f", minTempConverted)},$minHumidity,$unitLabel"
    }

    companion object {
        fun csvHeader(): String {
            return "Index,FormattedTime,Timestamp,MaxTemperature,MaxHumidity,MinTemperature,MinHumidity,Unit"
        }
    }
}
