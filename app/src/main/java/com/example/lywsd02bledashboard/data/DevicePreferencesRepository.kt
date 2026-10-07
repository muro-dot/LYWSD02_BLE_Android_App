package com.example.lywsd02bledashboard.data

import android.content.Context
import android.content.SharedPreferences
import com.example.lywsd02bledashboard.ble.BleProtocolParser
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
                list.add(
                    KnownDevice(
                        id = obj.getString("id"),
                        name = obj.optString("name", "LYWSD02"),
                        alias = obj.optString("alias", ""),
                        lastConnected = obj.optLong("lastConnected", 0L),
                        connectionCount = obj.optInt("connectionCount", 0),
                        lastTemperatureC = if (obj.has("lastTemp")) obj.getDouble("lastTemp").toFloat() else null,
                        lastHumidity = if (obj.has("lastHumidity")) obj.getInt("lastHumidity") else null,
                        lastBattery = if (obj.has("lastBattery")) obj.getInt("lastBattery") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.lastConnected }
    }

    /**
     * 특정 기기의 연결 성공 기록 갱신/추가
     */
    fun recordConnectionSuccess(
        id: String,
        name: String = "LYWSD02",
        temp: Float? = null,
        hum: Int? = null,
        bat: Int? = null
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
                lastBattery = bat ?: prev.lastBattery
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
                lastBattery = bat
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
     * 사용자 타임존 오프셋 분 (기본값: 스마트폰 시스템 현재 타임존)
     */
    var savedTimezoneMinutes: Int
        get() {
            val systemOffsetMinutes = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60000
            val defaultOffset = BleProtocolParser.nearestTimezoneOffset(systemOffsetMinutes)
            return prefs.getInt(KEY_TIMEZONE_MINUTES, defaultOffset)
        }
        set(value) = prefs.edit().putInt(KEY_TIMEZONE_MINUTES, value).apply()
}
