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
 * 대시보드 로그 콘솔에 표시되는 개별 로그 항목 (다국어 지원)
 */
data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val messageKo: String,
    val messageEn: String = messageKo,
    val type: LogType = LogType.INFO,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 단일 메시지로 생성 시 하위 호환성 지원 생성자
     */
    constructor(
        id: String = UUID.randomUUID().toString(),
        message: String,
        type: LogType = LogType.INFO,
        timestamp: Long = System.currentTimeMillis()
    ) : this(
        id = id,
        messageKo = message,
        messageEn = message,
        type = type,
        timestamp = timestamp
    )

    /**
     * 기존 코드와의 하위 호환성을 위한 기본 message 프로퍼티
     */
    val message: String
        get() = if (Locale.getDefault().language == "ko") messageKo else messageEn

    /**
     * UI의 현재 로케일에 맞춰 최적의 메시지 반환
     */
    fun getMessage(isKorean: Boolean): String {
        return if (isKorean) messageKo else messageEn
    }

    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

