package com.example.lywsd02bledashboard.data

import android.content.Context
import android.content.SharedPreferences
import com.example.lywsd02bledashboard.ble.BleProtocolParser
import com.example.lywsd02bledashboard.model.ClockDisplayMode
import com.example.lywsd02bledashboard.model.KnownDevice
import com.example.lywsd02bledashboard.model.TemperatureUnit
import org.json.JSONArray
import org.json.JSONObject
import java.util.TimeZone

/**
 * 기기 별칭, 연결 히스토리, 사용자 환경 설정을 로컬에 보관하는 저장소.
 * 웹 대시보드의 localStorage(lywsd02-device-history-v1, lywsd02-device-aliases-v1)와 동일한 목적을 가집니다.
 */
class DevicePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lywsd02_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_KNOWN_DEVICES = "known_devices_json"
        private const val KEY_AUTO_SYNC_CLOCK = "auto_sync_clock"
        private const val KEY_DEFAULT_UNIT = "default_unit"
        private const val KEY_TIMEZONE_MINUTES = "timezone_minutes"
        private const val KEY_USE_SYSTEM_TIMEZONE = "use_system_timezone"
    }

    /**
     * 저장된 모든 기기 목록 불러오기
     */
    fun getKnownDevices(): List<KnownDevice> {
        val jsonString = prefs.getString(KEY_KNOWN_DEVICES, null) ?: return emptyList()
        val list = mutableListOf<KnownDevice>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val clockModeStr = obj.optString("clockMode", ClockDisplayMode.MODE_24H.name)
                val parsedClockMode = try {
                    ClockDisplayMode.valueOf(clockModeStr)
                } catch (e: Exception) {
                    ClockDisplayMode.MODE_24H
                }

                val lastUnitStr = obj.optString("lastUnit", TemperatureUnit.CELSIUS.name)
                val parsedLastUnit = try {
                    TemperatureUnit.valueOf(lastUnitStr)
                } catch (e: Exception) {
                    TemperatureUnit.CELSIUS
                }

                list.add(
                    KnownDevice(
                        id = obj.getString("id"),
                        name = obj.optString("name", "LYWSD02"),
                        alias = obj.optString("alias", ""),
                        lastConnected = obj.optLong("lastConnected", 0L),
                        connectionCount = obj.optInt("connectionCount", 0),
                        lastTemperatureC = if (obj.has("lastTemp")) obj.getDouble("lastTemp").toFloat() else null,
                        lastHumidity = if (obj.has("lastHumidity")) obj.getInt("lastHumidity") else null,
                        lastBattery = if (obj.has("lastBattery")) obj.getInt("lastBattery") else null,
                        clockMode = parsedClockMode,
                        lastUnit = parsedLastUnit
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.lastConnected }
    }

    /**
     * 특정 기기 ID(MAC 주소)에 저장된 사용자 별칭(Alias)을 조회합니다.
     */
    fun getDeviceAlias(id: String): String? {
        val known = getKnownDevices().find { it.id.equals(id, ignoreCase = true) }
        return known?.alias?.takeIf { it.isNotBlank() }
    }

    /**
     * 특정 기기의 연결 성공 기록 갱신/추가
     */
    fun recordConnectionSuccess(
        id: String,
        name: String = "LYWSD02",
        temp: Float? = null,
        hum: Int? = null,
        bat: Int? = null,
        clockMode: ClockDisplayMode? = null,
        unit: TemperatureUnit? = null
    ) {
        val currentList = getKnownDevices().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == id }

        val updated = if (existingIndex >= 0) {
            val prev = currentList[existingIndex]
            prev.copy(
                name = if (name.isNotBlank()) name else prev.name,
                lastConnected = System.currentTimeMillis(),
                connectionCount = prev.connectionCount + 1,
                lastTemperatureC = temp ?: prev.lastTemperatureC,
                lastHumidity = hum ?: prev.lastHumidity,
                lastBattery = bat ?: prev.lastBattery,
                clockMode = clockMode ?: prev.clockMode,
                lastUnit = unit ?: prev.lastUnit
            )
        } else {
            KnownDevice(
                id = id,
                name = name,
                alias = "",
                lastConnected = System.currentTimeMillis(),
                connectionCount = 1,
                lastTemperatureC = temp,
                lastHumidity = hum,
                lastBattery = bat,
                clockMode = clockMode ?: ClockDisplayMode.MODE_24H,
                lastUnit = unit ?: TemperatureUnit.CELSIUS
            )
        }

        if (existingIndex >= 0) {
            currentList[existingIndex] = updated
        } else {
            currentList.add(0, updated)
        }

        saveKnownDevices(currentList)
    }

    /**
     * 특정 기기의 별칭(Alias) 수정 ("거실", "안방" 등)
     */
    fun updateDeviceAlias(id: String, alias: String) {
        val currentList = getKnownDevices().toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(alias = alias)
            saveKnownDevices(currentList)
        }
    }

    /**
     * 특정 기기의 시계 표시 모드 (12시간 / 24시간) 저장
     */
    fun updateDeviceClockMode(id: String, mode: ClockDisplayMode) {
        val currentList = getKnownDevices().toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(clockMode = mode)
            saveKnownDevices(currentList)
        }
    }

    /**
     * 특정 기기의 온도 단위 (°C / °F) 저장
     */
    fun updateDeviceUnit(id: String, unit: TemperatureUnit) {
        val currentList = getKnownDevices().toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(lastUnit = unit)
            saveKnownDevices(currentList)
        }
    }

    /**
     * 기기 목록에서 삭제
     */
    fun removeKnownDevice(id: String) {
        val currentList = getKnownDevices().filterNot { it.id == id }
        saveKnownDevices(currentList)
    }

    private fun saveKnownDevices(devices: List<KnownDevice>) {
        try {
            val array = JSONArray()
            for (device in devices) {
                val obj = JSONObject().apply {
                    put("id", device.id)
                    put("name", device.name)
                    put("alias", device.alias)
                    put("lastConnected", device.lastConnected)
                    put("connectionCount", device.connectionCount)
                    device.lastTemperatureC?.let { put("lastTemp", it.toDouble()) }
                    device.lastHumidity?.let { put("lastHumidity", it) }
                    device.lastBattery?.let { put("lastBattery", it) }
                    put("clockMode", device.clockMode.name)
                    put("lastUnit", device.lastUnit.name)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_KNOWN_DEVICES, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 자동 시계 동기화 활성화 여부
     */
    var isAutoSyncClockEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC_CLOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC_CLOCK, value).apply()

    /**
     * 기본 온도 단위 (°C / °F)
     */
    var defaultTemperatureUnit: TemperatureUnit
        get() {
            val name = prefs.getString(KEY_DEFAULT_UNIT, TemperatureUnit.CELSIUS.name)
            return try {
                TemperatureUnit.valueOf(name ?: TemperatureUnit.CELSIUS.name)
            } catch (e: Exception) {
                TemperatureUnit.CELSIUS
            }
        }
        set(value) = prefs.edit().putString(KEY_DEFAULT_UNIT, value.name).apply()

    /**
     * 스마트폰 시스템 타임존 자동 사용 여부 (기본값: true)
     */
    var useSystemTimezone: Boolean
        get() = prefs.getBoolean(KEY_USE_SYSTEM_TIMEZONE, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_SYSTEM_TIMEZONE, value).apply()

    /**
     * 사용자 타임존 오프셋 분
     * useSystemTimezone이 true이면 항상 스마트폰 시스템 현재 타임존을 실시간으로 반환합니다.
     */
    var savedTimezoneMinutes: Int
        get() {
            if (useSystemTimezone) {
                return BleProtocolParser.getSystemTimezoneOffsetMinutes()
            }
            return prefs.getInt(KEY_TIMEZONE_MINUTES, BleProtocolParser.getSystemTimezoneOffsetMinutes())
        }
        set(value) {
            prefs.edit().putInt(KEY_TIMEZONE_MINUTES, value).apply()
        }
}
