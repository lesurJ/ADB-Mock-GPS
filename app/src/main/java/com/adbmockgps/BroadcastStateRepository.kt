package com.adbmockgps

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A simple data class to hold the information from the broadcast.
 */
data class LastBroadcastInfo(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timestamp: String
)

/**
 * A singleton object to hold the state of the last broadcast, mirroring the
 * architecture of the adb-mock-steps app. Using a StateFlow allows the UI to
 * reactively collect updates.
 */
object BroadcastStateRepository {
    private val _lastBroadcast = MutableStateFlow<LastBroadcastInfo?>(null)
    val lastBroadcast = _lastBroadcast.asStateFlow()

    fun updateLastBroadcast(latitude: Double, longitude: Double, altitude: Double?) {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        _lastBroadcast.value = LastBroadcastInfo(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            timestamp = currentTime.format(formatter)
        )
    }
}