package com.example.lywsd02bledashboard.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 이전에 연결했던 기기 정보 및 스냅샷 캐시
 */
data class KnownDevice(
    val id: String, // MAC 주소
    val name: String = "LYWSD02",
    val alias: String = "",
    val lastConnected: Long = 0L,
    val connectionCount: Int = 0,
    val lastTemperatureC: Float? = null,
    val lastHumidity: Int? = null,
    val lastBattery: Int? = null,
    val clockMode: ClockDisplayMode = ClockDisplayMode.MODE_24H,
    val lastUnit: TemperatureUnit = TemperatureUnit.CELSIUS
) {
    /**
     * UI에 표시될 대표 이름 (별칭이 있으면 별칭, 없으면 기기명 + 축약 ID)
     */
    fun displayName(): String {
        return if (alias.isNotBlank()) {
            alias
        } else {
            val shortId = id.replace(":", "").takeLast(6).uppercase()
            "$name · $shortId"
        }
    }

    /**
     * 마지막 연결 시각 표시 포맷
     */
    fun formattedLastConnected(): String {
        if (lastConnected == 0L) return "연결 기록 없음"
        val diffSeconds = (System.currentTimeMillis() - lastConnected) / 1000
        return when {
            diffSeconds < 60 -> "방금 전"
            diffSeconds < 3600 -> "${diffSeconds / 60}분 전"
            diffSeconds < 86400 -> "${diffSeconds / 3600}시간 전"
            else -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(lastConnected))
            }
        }
    }
}
