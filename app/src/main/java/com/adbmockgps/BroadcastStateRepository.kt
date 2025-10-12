package com.adbmockgps

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

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
 * A singleton object to hold the state of the last broadcast.
 * Using a StateFlow allows the UI to reactively collect updates.
 */
@Singleton
class BroadcastStateRepository @Inject constructor(){
    private val _lastBroadcast = MutableStateFlow<LastBroadcastInfo?>(null)
    val lastBroadcast = _lastBroadcast.asStateFlow()

    fun updateLastBroadcast(latitude: Double, longitude: Double, altitude: Double?, timestamp: String) {
        _lastBroadcast.value = LastBroadcastInfo(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            timestamp = timestamp
        )
    }
}