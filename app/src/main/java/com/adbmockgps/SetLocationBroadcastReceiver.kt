package com.adbmockgps

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import android.util.Log
import android.location.provider.ProviderProperties


/**
 * Optimized GpsBroadcastReceiver that sets up the mock provider once
 * and only updates locations on subsequent calls.
 */
class SetLocationBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val MOCK_PROVIDER_NAME = LocationManager.GPS_PROVIDER
        const val ACTION_SET_LOCATION = "com.adbmockgps.SET_LOCATION"

        // Track if the test provider has been set up
        @Volatile
        private var isTestProviderSetup = false
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("ADBMockGPS", "=== BROADCAST RECEIVER TRIGGERED ===")
        Log.i("ADBMockGPS", "Action: ${intent.action}")

        when (intent.action) {
            ACTION_SET_LOCATION -> handleLocationIntent(context, intent)
            else -> Log.w("ADBMockGPS", "Unknown action: ${intent.action}")
        }
    }

    private fun handleLocationIntent(context: Context, intent: Intent) {
        // Extract latitude, longitude, and optional altitude from the intent extras
        val lat = intent.getStringExtra("lat")?.toDoubleOrNull()
        val lon = intent.getStringExtra("lon")?.toDoubleOrNull()
        val alt = intent.getStringExtra("alt")?.toDoubleOrNull() // Optional altitude

        if (lat == null || lon == null) {
            Log.e("ADBMockGPS", "Invalid coordinates: lat=$lat, lon=$lon")
            return
        }

        Log.i("ADBMockGPS", "Received coordinates: lat=$lat, lon=$lon, alt=$alt")

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Setup test provider only if it hasn't been setup yet
        if (!isTestProviderSetup) {
            if (setupTestProvider(locationManager)) {
                isTestProviderSetup = true
                Log.i("ADBMockGPS", "Test provider setup completed")
            } else {
                Log.e("ADBMockGPS", "Failed to setup test provider")
                return
            }
        }

        // Create and set the mock location (this is the only thing that happens on repeat calls)
        setMockLocation(locationManager, lat, lon, alt)
    }

    private fun setupTestProvider(locationManager: LocationManager): Boolean {
        try {
            // Remove any existing test provider (only needed once)
            try {
                locationManager.removeTestProvider(MOCK_PROVIDER_NAME)
                Log.d("ADBMockGPS", "Removed existing test provider")
            } catch (e: Exception) {
                Log.d("ADBMockGPS", "No existing test provider to remove")
            }

            // Add the test provider
            locationManager.addTestProvider(
                MOCK_PROVIDER_NAME,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                ProviderProperties.POWER_USAGE_LOW,
                ProviderProperties.ACCURACY_FINE
            )

            // Enable the test provider
            locationManager.setTestProviderEnabled(MOCK_PROVIDER_NAME, true)
            Log.i("ADBMockGPS", "Test provider setup successful")
            return true

        } catch (e: SecurityException) {
            Log.e("ADBMockGPS", "SecurityException: Mock location permission denied", e)
            return false
        } catch (e: Exception) {
            Log.e("ADBMockGPS", "Error setting up test provider: ${e.message}", e)
            return false
        }
    }

    private fun setMockLocation(locationManager: LocationManager, lat: Double, lon: Double, alt: Double?) {
        val mockLocation = Location(MOCK_PROVIDER_NAME).apply {
            latitude = lat
            longitude = lon
            alt?.let { altitude = it }
            accuracy = 1.0f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }

        try {
            locationManager.setTestProviderLocation(MOCK_PROVIDER_NAME, mockLocation)
            LocationRepository.updateLocation(mockLocation)
            Log.i("ADBMockGPS", "Mock location updated to ($lat, $lon${alt?.let { ", $it" } ?: ""})")
        } catch (e: SecurityException) {
            Log.e("ADBMockGPS", "SecurityException: Failed to set mock location", e)
            // Reset the setup flag so we can try again next time
            isTestProviderSetup = false
        } catch (e: Exception) {
            Log.e("ADBMockGPS", "Error setting mock location: ${e.message}", e)
        }
    }
}