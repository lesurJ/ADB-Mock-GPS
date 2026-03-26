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
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Optimized GpsBroadcastReceiver that sets up the mock provider once
 * and only updates locations on subsequent calls.
 */
@AndroidEntryPoint
class SetLocationBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var broadcastStateRepository: BroadcastStateRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val GPS_LOCATION_PROVIDER = LocationManager.GPS_PROVIDER
        const val NETWORK_LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER
        const val FUSED_LOCATION_PROVIDER = "fused"
        
        const val ACTION_SET_LOCATION = "com.adbmockgps.SET_LOCATION"

        @Volatile
        private var providersSetup = mutableSetOf<String>()

        fun clearProvidersSetup() {
            synchronized(providersSetup) {
                providersSetup.clear()
            }
        }
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
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        if (lat == null || lon == null) {
            Log.e("ADBMockGPS", "Invalid coordinates: lat=$lat, lon=$lon")
            return
        }

        Log.i("ADBMockGPS", "Received coordinates: lat=$lat, lon=$lon, alt=$alt")
        broadcastStateRepository.updateLastBroadcast(lat, lon, alt, currentTime.format(formatter))

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val providersToSetup = listOf(
            GPS_LOCATION_PROVIDER, 
            NETWORK_LOCATION_PROVIDER, 
            FUSED_LOCATION_PROVIDER
        )

        synchronized(providersSetup) {
            for (provider in providersToSetup) {
                if (!providersSetup.contains(provider)) {
                    if (setupTestProvider(locationManager, provider)) {
                        providersSetup.add(provider)
                        Log.i("ADBMockGPS", "$provider test provider setup completed.")
                    } else {
                        Log.e("ADBMockGPS", "Failed to setup $provider test provider.")
                    }
                }
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
            // This is expected if it wasn't already a test provider
        }

        try {
            val requiresNetwork = providerName == NETWORK_LOCATION_PROVIDER || providerName == FUSED_LOCATION_PROVIDER
            val requiresSatellite = providerName == GPS_LOCATION_PROVIDER || providerName == FUSED_LOCATION_PROVIDER
            val requiresCell = providerName == NETWORK_LOCATION_PROVIDER || providerName == FUSED_LOCATION_PROVIDER

            locationManager.addTestProvider(
                providerName,
                requiresNetwork,
                requiresSatellite,
                requiresCell,
                false, // hasMonetaryCost
                true,  // supportsAltitude
                true,  // supportsSpeed
                true,  // supportsBearing
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
