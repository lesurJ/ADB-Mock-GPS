package com.adbmockgps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class MockLocationService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var currentAlt: Double? = null

    private lateinit var locationManager: LocationManager

    companion object {
        const val ACTION_START = "com.adbmockgps.ACTION_START"
        const val ACTION_STOP = "com.adbmockgps.ACTION_STOP"
        const val ACTION_UPDATE_LOCATION = "com.adbmockgps.ACTION_UPDATE_LOCATION"

        private const val NOTIFICATION_CHANNEL_ID = "MockLocationChannel"
        private const val NOTIFICATION_ID = 1

        @Volatile
        var isRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_UPDATE_LOCATION -> {
                currentLat = intent.getDoubleExtra("lat", currentLat ?: 0.0)
                currentLon = intent.getDoubleExtra("lon", currentLon ?: 0.0)
                // Use hasExtra to differentiate between not present and a value of 0.0
                if (intent.hasExtra("alt")) {
                    currentAlt = intent.getDoubleExtra("alt", 0.0)
                }

                Log.i("MockLocationService", "Service received location: $currentLat, $currentLon, $currentAlt")

                if (!isRunning) {
                    startForeground(NOTIFICATION_ID, createNotification())
                    startLocationPulsing()
                    isRunning = true
                    Log.i("MockLocationService", "Service started and is now pulsing location.")
                }
            }
            ACTION_STOP -> {
                Log.i("MockLocationService", "Service stopping.")
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startLocationPulsing() {
        scope.launch {
            while (isActive) {
                val lat = currentLat
                val lon = currentLon
                if (lat != null && lon != null) {
                    setMockLocation(SetLocationBroadcastReceiver.GPS_LOCATION_PROVIDER, lat, lon, currentAlt)
                    setMockLocation(SetLocationBroadcastReceiver.NETWORK_LOCATION_PROVIDER, lat, lon, currentAlt)
                }
                delay(1500) // Pulse location every 1.5 sec
            }
        }
    }

    private fun setMockLocation(provider: String, lat: Double, lon: Double, alt: Double?) {
        val mockLocation = Location(provider).apply {
            latitude = lat
            longitude = lon
            alt?.let { altitude = it }
            accuracy = 1.0f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }
        try {
            locationManager.setTestProviderLocation(provider, mockLocation)
        } catch (e: Exception) {
            Log.e("MockLocationService", "Failed to set mock location for $provider", e)
        }
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, MockLocationService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ADB Mock GPS Active")
            .setContentText("Mocking location. Tap to open app.")
            //.setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop Mocking", pendingStopIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Mock Location Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        isRunning = false
        try {
            locationManager.removeTestProvider(SetLocationBroadcastReceiver.GPS_LOCATION_PROVIDER)
            locationManager.removeTestProvider(SetLocationBroadcastReceiver.NETWORK_LOCATION_PROVIDER)
        } catch (e: Exception) {
            Log.e("MockLocationService", "Failed to remove test providers on destroy", e)
        }
        Log.i("MockLocationService", "Service destroyed, providers removed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}