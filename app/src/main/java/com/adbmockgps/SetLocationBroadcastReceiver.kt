package com.adbmockgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import android.location.provider.ProviderProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Optimized GpsBroadcastReceiver that sets up the mock provider once
 * and only updates locations on subsequent calls.
 */
class SetLocationBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val GPS_LOCATION_PROVIDER = LocationManager.GPS_PROVIDER
        const val NETWORK_LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER
        const val ACTION_SET_LOCATION = "com.adbmockgps.SET_LOCATION"

        @Volatile
        private var isGpsProviderSetup = false
        @Volatile
        private var isNetworkProviderSetup = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SET_LOCATION) {
            return
        }

        val pendingResult = goAsync()
        scope.launch {
            try {
                Log.i("ADBMockGPS", "=== BROADCAST RECEIVER TRIGGERED ===")
                Log.i("ADBMockGPS", "Action: ${intent.action}")
                handleLocationIntent(context, intent)
            } catch (e: Exception) {
                Log.e("ADBMockGPS", "Error processing broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleLocationIntent(context: Context, intent: Intent) {
        // Extract latitude, longitude, and optional altitude from the intent extras
        val lat = intent.getStringExtra("lat")?.toDoubleOrNull()
        val lon = intent.getStringExtra("lon")?.toDoubleOrNull()
        val alt = intent.getStringExtra("alt")?.toDoubleOrNull()

        if (lat == null || lon == null) {
            Log.e("ADBMockGPS", "Invalid coordinates: lat=$lat, lon=$lon")
            return
        }

        Log.i("ADBMockGPS", "Received coordinates: lat=$lat, lon=$lon, alt=$alt")
        BroadcastStateRepository.updateLastBroadcast(lat, lon, alt)

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!isGpsProviderSetup) {
            if (setupTestProvider(locationManager, GPS_LOCATION_PROVIDER)) {
                isGpsProviderSetup = true
                Log.i("ADBMockGPS", "GPS test provider setup completed.")
            } else {
                Log.e("ADBMockGPS", "Failed to setup GPS test provider.")
                return
            }
        }

        if (!isNetworkProviderSetup) {
            if (setupTestProvider(locationManager, NETWORK_LOCATION_PROVIDER)) {
                isNetworkProviderSetup = true
                Log.i("ADBMockGPS", "Network test provider setup completed.")
            } else {
                Log.e("ADBMockGPS", "Failed to setup Network test provider.")
                return
            }
        }

        val serviceIntent = Intent(context, MockLocationService::class.java).apply {
            action = if (MockLocationService.isRunning) MockLocationService.ACTION_UPDATE_LOCATION else MockLocationService.ACTION_START
            putExtra("lat", lat)
            putExtra("lon", lon)
            alt?.let { putExtra("alt", it) }
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    private fun setupTestProvider(locationManager: LocationManager, providerName: String): Boolean {
        try {
            locationManager.removeTestProvider(providerName)
        } catch (e: Exception) {
            Log.d("ADBMockGPS", "No existing '$providerName' to remove or failed to remove.")
        }

        try {
            locationManager.addTestProvider(
                providerName,
                true,
                true,
                true,
                false,
                true,
                true,
                true,
                ProviderProperties.POWER_USAGE_LOW,
                ProviderProperties.ACCURACY_FINE
            )

            locationManager.setTestProviderEnabled(providerName, true)
            Log.i("ADBMockGPS", "'$providerName' setup successful")
            return true

        } catch (e: SecurityException) {
            Log.e("ADBMockGPS", "SecurityException for '$providerName': Mock location permission denied", e)
            return false
        } catch (e: Exception) {
            Log.e("ADBMockGPS", "Error setting up '$providerName': ${e.message}", e)
            return false
        }
    }

}