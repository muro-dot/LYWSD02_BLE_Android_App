package com.example.lywsd02bledashboard.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 로그 중요도 및 유형
 */
enum class LogType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * 대시보드 로그 콘솔에 표시되는 개별 로그 항목
 */
data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val type: LogType = LogType.INFO,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
