package com.example.lywsd02bledashboard.model

import java.util.UUID

/**
 * Xiaomi Mijia LYWSD02 기기와 통신하기 위한 BLE 서비스 및 캐릭터리스틱 UUID 상수 정의.
 * 웹 대시보드 원본(app.js)의 프로토콜 사양과 100% 동일하게 매핑합니다.
 */
object BleConstants {
    // 메인 서비스 UUID
    val SERVICE_UUID: UUID = UUID.fromString("ebe0ccb0-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 시간 읽기 및 쓰기 캐릭터리스틱
    val TIME_CHARACTERISTIC_UUID: UUID = UUID.fromString("ebe0ccb7-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 저장된 과거 기록 개수 확인 캐릭터리스틱
    val RECORD_COUNT_CHARACTERISTIC_UUID: UUID = UUID.fromString("ebe0ccb9-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 조회할 과거 기록의 시작 인덱스 지정 캐릭터리스틱
    val RECORD_INDEX_CHARACTERISTIC_UUID: UUID = UUID.fromString("ebe0ccba-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 과거 기록 데이터 스트리밍(Notification) 캐릭터리스틱
    val HISTORY_CHARACTERISTIC_UUID: UUID = UUID.fromString("ebe0ccbc-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 온도 단위 (°C / °F) 설정 및 조회 캐릭터리스틱
    val UNIT_CHARACTERISTIC_UUID: UUID = UUID.fromString("ebe0ccbe-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 실시간 온습도 측정값 스트리밍(Notification) 캐릭터리스틱
    val MEASUREMENTS_CHARACTERISTIC_UUID: UUID = UUID.fromString("ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 배터리 잔량 조회 캐릭터리스틱
    val BATTERY_CHARACTERISTIC_UUID: UUID = UUID.fromString("ebe0ccc4-7a0a-4b0c-8a1a-6ff2997da3a6")

    // 표준 클라이언트 특성 설정 디스크립터 (CCCD - Notification 활성화용)
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // 연결 재시도 딜레이 (ms 단위)
    val RETRY_DELAYS_MS = listOf(1500L, 3000L, 6000L, 10000L, 10000L, 10000L)

    // 전세계 주요 타임존 분(Minute) 오프셋 목록
    val TIMEZONE_OFFSETS = listOf(
        -720, -660, -600, -570, -540, -480, -420, -360, -300, -240, -210,
        -180, -150, -120, -60, 0, 60, 120, 180, 210, 240, 270, 300, 330,
        345, 360, 390, 420, 480, 525, 540, 570, 600, 630, 660, 720, 765,
        780, 825, 840
    )
}
