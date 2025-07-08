package com.adbmockgps

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class LocationState(
    val latitude: String = "Waiting for location...",
    val longitude: String = "Waiting for location...",
    val altitude: String = "N/A",
    val lastUpdate: String = "Never",
    val error: String? = null
)

@Composable
fun rememberLocationState(
    locationManager: LocationManager,
    permissionsGranted: Boolean
): LocationState {
    var locationState by remember {
        mutableStateOf(LocationState())
    }

    DisposableEffect(permissionsGranted) {
        if (!permissionsGranted) {
            locationState = LocationState(
                latitude = "Permission denied",
                longitude = "Permission denied",
                altitude = "Permission denied",
                lastUpdate = "N/A",
                error = "Permissions not granted"
            )
            return@DisposableEffect onDispose { }
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val currentTime = LocalDateTime.now()
                val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                locationState = LocationState(
                    latitude = String.format("%.6f", location.latitude),
                    longitude = String.format("%.6f", location.longitude),
                    altitude = if (location.hasAltitude()) String.format("%.1f m", location.altitude) else "N/A",
                    lastUpdate = formattedTime,
                    error = null
                )
            }

            override fun onStatusChanged(
                p0: String?,
                p1: Int,
                p2: Bundle?
            ) {
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Try get last known location immediately
        try {
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let {
                locationState = LocationState(
                    latitude = String.format("%.6f", it.latitude),
                    longitude = String.format("%.6f", it.longitude),
                    altitude = if (it.hasAltitude()) String.format("%.1f m", it.altitude) else "N/A",
                    lastUpdate = "Last known",
                    error = null
                )
            }
        } catch (e: SecurityException) {
            locationState = locationState.copy(error = "SecurityException: ${e.message}")
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                listener
            )
        } catch (e: SecurityException) {
            locationState = LocationState(
                latitude = "Permission denied",
                longitude = "Permission denied",
                altitude = "Permission denied",
                lastUpdate = "N/A",
                error = "SecurityException requesting updates: ${e.message}"
            )
        }

        onDispose {
            locationManager.removeUpdates(listener)
        }
    }

    return locationState
}

@Composable
fun LocationCard(locationState: LocationState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📍 Location",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LocationInfoColumn("Latitude", locationState.latitude, Color.Green)
                Spacer(Modifier.width(16.dp))
                LocationInfoColumn("Longitude", locationState.longitude, Color.Green)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LocationInfoColumn("Altitude", locationState.altitude, Color.Cyan)
                LocationInfoColumn("Last Update", locationState.lastUpdate, Color.Gray)
            }

            locationState.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LocationInfoColumn(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AdbCommandsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📱 ADB Commands",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Set location: adb shell am broadcast -a com.adbmockgps.SET_LOCATION --es lat \"40.7128\" --es lon \"-74.0060\" [--es alt \"10.0\"] -f 0x01000000",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Get location: adb shell am broadcast -a com.adbmockgps.GET_LOCATION -f 0x01000000",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun LocationScreen(
    locationManager: LocationManager,
    permissionsGranted: Boolean
) {
    val locationState = rememberLocationState(locationManager, permissionsGranted)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "ADB Mock GPS",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            LocationCard(locationState)

            AdbCommandsCard()
        }
    }
}
