package com.example.lywsd02bledashboard

import com.example.lywsd02bledashboard.ble.BleProtocolParser
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.model.HistoryRecord
import com.example.lywsd02bledashboard.model.TemperatureUnit
import org.junit.Assert.assertEquals
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
    }
}
