package com.example.lywsd02bledashboard

import com.example.lywsd02bledashboard.ble.BleProtocolParser
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.model.HistoryRecord
import com.example.lywsd02bledashboard.model.TemperatureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Xiaomi Mijia LYWSD02 BLE 통신 프로토콜 인코딩/디코딩 단위 테스트
 */
class BleProtocolParserTest {

    @Test
    fun testParseMeasurement_validPayload() {
        // 온도 24.50°C (2450 = 0x0992 -> LE: 0x92, 0x09), 습도 55% (0x37)
        val payload = byteArrayOf(0x92.toByte(), 0x09.toByte(), 0x37.toByte())
        val result = BleProtocolParser.parseMeasurement(payload)

        assertNotNull(result)
        assertEquals(24.50f, result!!.first, 0.01f)
        assertEquals(55, result.second)
    }

    @Test
    fun testParseMeasurement_negativeTemperature() {
        // 영하 -5.25°C (-525 = -0x020D -> 0xFEF3 -> LE: 0xF3, 0xFE), 습도 80% (0x50)
        val tempRaw: Short = -525
        val buffer = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(tempRaw)
        buffer.put(80.toByte())

        val result = BleProtocolParser.parseMeasurement(buffer.array())
        assertNotNull(result)
        assertEquals(-5.25f, result!!.first, 0.01f)
        assertEquals(80, result.second)
    }

    @Test
    fun testParseBattery() {
        val payload = byteArrayOf(87.toByte())
        val level = BleProtocolParser.parseBattery(payload)
        assertEquals(87, level)
    }

    @Test
    fun testUnitParsingAndEncoding() {
        // 0x01 -> Fahrenheit
        assertEquals(TemperatureUnit.FAHRENHEIT, BleProtocolParser.parseUnit(byteArrayOf(0x01)))
        // 0xFF -> Celsius
        assertEquals(TemperatureUnit.CELSIUS, BleProtocolParser.parseUnit(byteArrayOf(0xFF.toByte())))

        // Encode Fahrenheit
        val fBytes = BleProtocolParser.encodeUnit(TemperatureUnit.FAHRENHEIT)
        assertEquals(1, fBytes.size)
        assertEquals(0x01.toByte(), fBytes[0])

        // Encode Celsius
        val cBytes = BleProtocolParser.encodeUnit(TemperatureUnit.CELSIUS)
        assertEquals(1, cBytes.size)
        assertEquals(0xFF.toByte(), cBytes[0])
    }

    @Test
    fun testEncodeTimeSync() {
        val desiredOffsetMinutes = 540 // KST UTC+9
        val payload = BleProtocolParser.encodeTimeSync(
            desiredOffsetMinutes = desiredOffsetMinutes,
            manualOffsetMinutes = 0,
            automatic = true
        )

        assertEquals(5, payload.size)
        // 5번째 바이트는 타임존 오프셋 시간 (540 / 60 = 9)
        assertEquals(9.toByte(), payload[4])
    }

    @Test
    fun testEncodeClockMode() {
        val mode12 = BleProtocolParser.encodeClockMode(ClockDisplayMode.MODE_12H)
        assertEquals(7, mode12.size)
        assertEquals(0xAA.toByte(), mode12[6])

        val mode24 = BleProtocolParser.encodeClockMode(ClockDisplayMode.MODE_24H)
        assertEquals(7, mode24.size)
        assertEquals(0x00.toByte(), mode24[6])
    }

    @Test
    fun testParseRecordCount() {
        // totalRecords: 1000, storedRecords: 96
        val buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(1000)
        buffer.putInt(96)

        val result = BleProtocolParser.parseRecordCount(buffer.array())
        assertNotNull(result)
        assertEquals(1000L, result!!.first)
        assertEquals(96L, result.second)
    }

    @Test
    fun testEncodeRecordIndex() {
        val indexPayload = BleProtocolParser.encodeRecordIndex(905L)
        assertEquals(4, indexPayload.size)

        val buffer = ByteBuffer.wrap(indexPayload).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(905, buffer.int)
    }

    @Test
    fun testParseHistoryRecord() {
        // index: 42, timestamp: 1700000000, maxTemp: 28.5°C, maxHum: 65%, minTemp: 19.2°C, minHum: 40%
        val buffer = ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(42)
        buffer.putInt(1700000000)
        buffer.putShort(2850)
        buffer.put(65.toByte())
        buffer.putShort(1920)
        buffer.put(40.toByte())

        val record = BleProtocolParser.parseHistoryRecord(buffer.array())
        assertNotNull(record)
        assertEquals(42L, record!!.index)
        assertEquals(1700000000L, record.timestamp)
        assertEquals(28.50f, record.maxTemperature, 0.01f)
        assertEquals(65, record.maxHumidity)
        assertEquals(19.20f, record.minTemperature, 0.01f)
        assertEquals(40, record.minHumidity)

        // CSV 포맷 검증
        val csvRow = record.toCsvRow(TemperatureUnit.CELSIUS)
        assertTrue(csvRow.contains("28.50"))
        assertTrue(csvRow.contains("65"))
        assertTrue(csvRow.contains("19.20"))
        assertTrue(csvRow.contains("40"))
        assertTrue(csvRow.contains("°C"))
    }

    @Test
    fun testFormatTimezoneOffset() {
        assertEquals("UTC+09:00", BleProtocolParser.formatTimezoneOffset(540))
        assertEquals("UTC-05:00", BleProtocolParser.formatTimezoneOffset(-300))
        assertEquals("UTC+00:00", BleProtocolParser.formatTimezoneOffset(0))
        assertEquals("UTC+05:30", BleProtocolParser.formatTimezoneOffset(330))
        assertEquals("UTC-03:30", BleProtocolParser.formatTimezoneOffset(-210))
    }

    @Test
    fun testNearestTimezoneOffset() {
        assertEquals(540, BleProtocolParser.nearestTimezoneOffset(540))
        assertEquals(540, BleProtocolParser.nearestTimezoneOffset(538)) // 2분 오차는 540에 매칭
        assertEquals(330, BleProtocolParser.nearestTimezoneOffset(330)) // 인도 5시간 30분
        assertEquals(-300, BleProtocolParser.nearestTimezoneOffset(-300))
        assertEquals(0, BleProtocolParser.nearestTimezoneOffset(0))
    }

    @Test
    fun testEncodeTimeSync_fractionalAndNegativeTimezones() {
        // 1. 인도 UTC+5:30 (330분)
        val payloadIndia = BleProtocolParser.encodeTimeSync(desiredOffsetMinutes = 330, automatic = true)
        assertEquals(5.toByte(), payloadIndia[4]) // floor(330/60) = 5

        // 2. 음수 분단위 타임존 UTC-3:30 (-210분)
        val payloadNegative = BleProtocolParser.encodeTimeSync(desiredOffsetMinutes = -210, automatic = true)
        assertEquals((-4).toByte(), payloadNegative[4]) // floor(-210/60) = -4

        // 3. 음수 정수 타임존 UTC-5:00 (-300분)
        val payloadEst = BleProtocolParser.encodeTimeSync(desiredOffsetMinutes = -300, automatic = true)
        assertEquals((-5).toByte(), payloadEst[4]) // floor(-300/60) = -5
    }

    @Test
    fun testParseTime_timezoneInference() {
        val nowEpoch = System.currentTimeMillis() / 1000L
        val buffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(nowEpoch.toInt())
        buffer.put(9.toByte()) // UTC+9

        val result = BleProtocolParser.parseTime(
            bytes = buffer.array(),
            desiredOffsetMinutes = 540,
            isTwelveHour = false
        )

        assertNotNull(result)
        assertEquals(540, result!!.deviceTimezoneMinutes)
        // 오차는 1초 이내여야 함
        assertTrue(Math.abs(result.driftSeconds) <= 2)
    }

    @Test
    fun testGetSystemTimezoneOffsetMinutes() {
        val offset = BleProtocolParser.getSystemTimezoneOffsetMinutes()
        // 반환된 오프셋은 유효한 표준 타임존 목록(TIMEZONE_OFFSETS)에 포함되어야 함
        assertTrue(com.example.lywsd02bledashboard.model.BleConstants.TIMEZONE_OFFSETS.contains(offset))
    }

    @Test
    fun testParseTime_detects12HourModeFrom7Bytes() {
        val nowEpoch = System.currentTimeMillis() / 1000L
        val buffer = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(nowEpoch.toInt())
        buffer.put(9.toByte()) // UTC+9
        buffer.put(0.toByte())
        buffer.put(0xAA.toByte()) // 12H 모드 플래그

        val result = BleProtocolParser.parseTime(
            bytes = buffer.array(),
            desiredOffsetMinutes = 540,
            isTwelveHour = false
        )

        assertNotNull(result)
        assertEquals(ClockDisplayMode.MODE_12H, result!!.detectedClockMode)
        val hasMarker = result.formattedTime.contains("AM") || 
                        result.formattedTime.contains("PM") ||
                        result.formattedTime.contains("오전") ||
                        result.formattedTime.contains("오후")
        assertTrue(hasMarker)
    }

    @Test
    fun testParseTime_detects24HourModeFrom7Bytes() {
        val nowEpoch = System.currentTimeMillis() / 1000L
        val buffer = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(nowEpoch.toInt())
        buffer.put(9.toByte()) // UTC+9
        buffer.put(0.toByte())
        buffer.put(0x00.toByte()) // 24H 모드 플래그

        val result = BleProtocolParser.parseTime(
            bytes = buffer.array(),
            desiredOffsetMinutes = 540,
            isTwelveHour = true
        )

        assertNotNull(result)
        assertEquals(ClockDisplayMode.MODE_24H, result!!.detectedClockMode)
        val hasMarker = result.formattedTime.contains("AM") || 
                        result.formattedTime.contains("PM") ||
                        result.formattedTime.contains("오전") ||
                        result.formattedTime.contains("오후")
        assertFalse(hasMarker)
    }

    @Test
    fun testParseTime_standard5BytesHasNullDetectedMode() {
        val nowEpoch = System.currentTimeMillis() / 1000L
        val buffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(nowEpoch.toInt())
        buffer.put(9.toByte()) // UTC+9

        val result = BleProtocolParser.parseTime(
            bytes = buffer.array(),
            desiredOffsetMinutes = 540,
            isTwelveHour = false
        )

        assertNotNull(result)
        assertEquals(null, result!!.detectedClockMode)
    }

    @Test
    fun testFormatEpoch_twelveHourAndTwentyFourHour() {
        // 1700000000L = 2023-11-14 22:13:20 UTC
        val epoch = 1700000000L
        val formatted24 = BleProtocolParser.formatEpoch(epoch, isTwelveHour = false)
        val formatted12 = BleProtocolParser.formatEpoch(epoch, isTwelveHour = true)

        assertEquals("22:13:20", formatted24)
        val hasMarker = formatted12.contains("PM") || formatted12.contains("오후")
        assertTrue(hasMarker)
        assertTrue(formatted12.contains("10:13:20"))
    }
}
