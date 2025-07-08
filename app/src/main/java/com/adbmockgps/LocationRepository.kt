package com.adbmockgps

import android.location.Location
import java.util.concurrent.atomic.AtomicReference

/**
 * A simple in-memory singleton to hold the last known location set by the app.
 * Using AtomicReference to ensure thread-safe updates and reads, as broadcasts
 * can run on different threads.
 */
object LocationRepository {
    private val currentLocation = AtomicReference<Location?>(null)

    fun updateLocation(location: Location) {
        currentLocation.set(location)
    }

    fun getLocation(): Location? {
        return currentLocation.get()
    }
}