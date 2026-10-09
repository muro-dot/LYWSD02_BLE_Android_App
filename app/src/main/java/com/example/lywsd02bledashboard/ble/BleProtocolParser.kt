package com.example.lywsd02bledashboard.ble

import com.example.lywsd02bledashboard.model.BleConstants
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.model.HistoryRecord
import com.example.lywsd02bledashboard.model.TemperatureUnit
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Xiaomi Mijia LYWSD02 BLE 바이트 패킷 인코딩/디코딩 유틸리티.
 * 원작 웹 대시보드(app.js)의 통신 프로토콜을 그대로 따릅니다.
 */
object BleProtocolParser {

    /**
     * 실시간 온습도 측정 데이터(3바이트) 파싱
     * - byte 0..1 : Int16 LE / 100 = 섭씨 온도
     * - byte 2    : UInt8 = 상대 습도 (%)
     */
    fun parseMeasurement(bytes: ByteArray): Pair<Float, Int>? {
        if (bytes.size < 3) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val tempRaw = buffer.short.toInt()
        val tempCelsius = tempRaw / 100.0f
        val humidity = bytes[2].toInt() and 0xFF
        return Pair(tempCelsius, humidity)
    }

    /**
     * 배터리 잔량(1바이트) 파싱
     */
    fun parseBattery(bytes: ByteArray): Int? {
        if (bytes.isEmpty()) return null
        return bytes[0].toInt() and 0xFF
    }

    /**
     * 온도 단위(1바이트) 파싱
     * 0x01: 화씨(°F), 0xFF(또는 그 외): 섭씨(°C)
     */
    fun parseUnit(bytes: ByteArray): TemperatureUnit {
        if (bytes.isEmpty()) return TemperatureUnit.CELSIUS
        return if (bytes[0].toInt() and 0xFF == 0x01) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
    }

    /**
     * 온도 단위 쓰기 패킷 생성
     */
    fun encodeUnit(unit: TemperatureUnit): ByteArray {
        return if (unit == TemperatureUnit.FAHRENHEIT) {
            byteArrayOf(0x01)
        } else {
            byteArrayOf(0xFF.toByte())
        }
    }

    /**
     * 시간 데이터(4~5바이트 또는 7바이트) 파싱 및 기기 시간/오차 계산 결과
     */
    data class ParsedTimeResult(
        val localEpochSeconds: Long,
        val formattedTime: String,
        val deviceTimezoneMinutes: Int,
        val driftSeconds: Long,
        val detectedClockMode: ClockDisplayMode? = null
    )

    /**
     * 가장 가까운 표준 타임존 오프셋(분) 찾기
     */
    fun nearestTimezoneOffset(minutes: Int): Int {
        return BleConstants.TIMEZONE_OFFSETS.minByOrNull { Math.abs(it - minutes) } ?: 0
    }

    /**
     * 현재 스마트폰 시스템의 실제 타임존 분 오프셋 반환 (서머타임 반영 및 표준 오프셋 매칭)
     */
    fun getSystemTimezoneOffsetMinutes(): Int {
        val systemOffsetMinutes = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60000
        return nearestTimezoneOffset(systemOffsetMinutes)
    }

    /**
     * 기기 시간 캐릭터리스틱 값 읽기 결과 분석
     */
    fun parseTime(
        bytes: ByteArray,
        desiredOffsetMinutes: Int,
        isTwelveHour: Boolean
    ): ParsedTimeResult? {
        if (bytes.size < 4) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val timestamp = buffer.int.toLong() and 0xFFFFFFFFL
        val timezoneHours = if (bytes.size >= 5) bytes[4].toInt() else 0

        // 기기 응답에 7바이트 이상 포함되어 있는 경우 12h(0xAA) / 24h(0x00) 모드 감지 (LYWSD02MMC/커스텀 펌웨어 지원)
        val detectedClockMode = if (bytes.size >= 7) {
            when (bytes[6].toInt() and 0xFF) {
                0xAA -> ClockDisplayMode.MODE_12H
                0x00 -> ClockDisplayMode.MODE_24H
                else -> null
            }
        } else {
            null
        }

        // 기기 로컬 Epoch = 타임스탬프 + 타임존 시간 * 3600
        val localEpoch = timestamp + timezoneHours * 3600L
        val nowEpoch = System.currentTimeMillis() / 1000L

        // 드리프트(오차 초): 기기 시간 - 현재 스마트폰 시간(목표 타임존 반영)
        val drift = localEpoch - nowEpoch - (desiredOffsetMinutes * 60L)

        // 기기 실제 타임존 추론 (30분/45분 단위 타임존 및 정수 시간 지원)
        val approximateOffset = Math.round((localEpoch - nowEpoch) / 60.0).toInt()
        val inferredOffset = nearestTimezoneOffset(approximateOffset)
        val deviceTimezoneMinutes = if (Math.abs(approximateOffset - inferredOffset) <= 5) {
            inferredOffset
        } else {
            timezoneHours * 60
        }

        // 기기 시계 표시 포맷팅 (감지된 모드가 있으면 최우선 반영)
        val effectiveTwelveHour = if (detectedClockMode != null) {
            detectedClockMode == ClockDisplayMode.MODE_12H
        } else {
            isTwelveHour
        }

        val formattedTime = formatEpoch(localEpoch, effectiveTwelveHour)

        return ParsedTimeResult(
            localEpochSeconds = localEpoch,
            formattedTime = formattedTime,
            deviceTimezoneMinutes = deviceTimezoneMinutes,
            driftSeconds = drift,
            detectedClockMode = detectedClockMode
        )
    }
    
    /**
     * Epoch 초를 12시간/24시간 형식 문자열로 변환
     */
    fun formatEpoch(epochSeconds: Long, isTwelveHour: Boolean): String {
        val date = Date(epochSeconds * 1000L)
        val pattern = if (isTwelveHour) "hh:mm:ss a" else "HH:mm:ss"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(date)
    }

    /**
     * 시간 동기화용 5바이트 페이로드 생성
     * [0..3]: uint32 Little Endian timestamp
     * [4]: int8 timezone hour offset
     */
    fun encodeTimeSync(
        desiredOffsetMinutes: Int,
        manualOffsetMinutes: Int = 0,
        automatic: Boolean = false
    ): ByteArray {
        val timezoneByte = Math.floor(desiredOffsetMinutes / 60.0).toInt().toByte()
        val minuteRemainder = desiredOffsetMinutes - (timezoneByte.toInt() * 60)
        val correctionSeconds = if (automatic) 0 else manualOffsetMinutes * 60
        val nowEpoch = System.currentTimeMillis() / 1000L
        val targetTimestamp = nowEpoch + (minuteRemainder * 60L) + correctionSeconds

        val buffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(targetTimestamp.toInt())
        buffer.put(timezoneByte)
        return buffer.array()
    }

    /**
     * 12시간 / 24시간 표시 모드 변경용 7바이트 페이로드 생성 (LYWSD02MMC 지원)
     * [6]: 0xAA (12h) 또는 0x00 (24h)
     */
    fun encodeClockMode(mode: ClockDisplayMode): ByteArray {
        val payload = ByteArray(7)
        payload[6] = if (mode == ClockDisplayMode.MODE_12H) 0xAA.toByte() else 0x00.toByte()
        return payload
    }

    /**
     * 과거 기록 개수(8바이트) 파싱
     * [0..3]: totalRecords (총 레코드 수)
     * [4..7]: storedRecords (기기에 현재 보관된 레코드 수)
     */
    fun parseRecordCount(bytes: ByteArray): Pair<Long, Long>? {
        if (bytes.size < 8) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val totalRecords = buffer.int.toLong() and 0xFFFFFFFFL
        val storedRecords = buffer.int.toLong() and 0xFFFFFFFFL
        return Pair(totalRecords, storedRecords)
    }

    /**
     * 조회 시작 인덱스 설정용 4바이트 페이로드 생성
     */
    fun encodeRecordIndex(startIndex: Long): ByteArray {
        val buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(startIndex.toInt())
        return buffer.array()
    }

    /**
     * 과거 온습도 기록(14바이트) 파싱
     */
    fun parseHistoryRecord(bytes: ByteArray): HistoryRecord? {
        if (bytes.size < 14) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val index = buffer.int.toLong() and 0xFFFFFFFFL
        val timestamp = buffer.int.toLong() and 0xFFFFFFFFL

        val maxTempRaw = buffer.short.toInt()
        val maxTemp = maxTempRaw / 100.0f
        val maxHumidity = buffer.get().toInt() and 0xFF

        val minTempRaw = buffer.short.toInt()
        val minTemp = minTempRaw / 100.0f
        val minHumidity = buffer.get().toInt() and 0xFF

        return HistoryRecord(
            index = index,
            timestamp = timestamp,
            maxTemperature = maxTemp,
            maxHumidity = maxHumidity,
            minTemperature = minTemp,
            minHumidity = minHumidity
        )
    }

    /**
     * 분 단위 타임존을 "UTC+09:00" 형식으로 변환
     */
    fun formatTimezoneOffset(totalMinutes: Int): String {
        val sign = if (totalMinutes >= 0) "+" else "-"
        val absolute = Math.abs(totalMinutes)
        val hours = String.format(Locale.US, "%02d", absolute / 60)
        val minutes = String.format(Locale.US, "%02d", absolute % 60)
        return "UTC$sign$hours:$minutes"
    }

    /**
     * 오차(초) 텍스트 변환 ("동기화됨", "+X초 빠름", "-X초 느림")
     */
    fun formatDriftText(driftSeconds: Long?): String {
        if (driftSeconds == null) return "알 수 없음"
        val absSeconds = Math.abs(driftSeconds)
        return when {
            absSeconds <= 1 -> "동기화됨 (오차 1초 이내)"
            driftSeconds > 0 -> "+${absSeconds}초 빠름"
            else -> "-${absSeconds}초 느림"
        }
    }
}
