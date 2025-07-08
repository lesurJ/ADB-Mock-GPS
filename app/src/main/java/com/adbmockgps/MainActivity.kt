package com.adbmockgps

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.adbmockgps.ui.theme.ADBMockGPSTheme


const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private var areLocationPermissionsGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        checkAndRequestPermissions()

        setContent {
            ADBMockGPSTheme {
                LocationScreen(locationManager, areLocationPermissionsGranted)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        areLocationPermissionsGranted = hasFineLocationPermission || hasCoarseLocationPermission

        if (!areLocationPermissionsGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Log.d("MainActivity", "Requesting location and activity permissions.")
        } else {
            Log.d("MainActivity", "Location permissions already granted.")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            areLocationPermissionsGranted = (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            Log.d("MainActivity", "onRequestPermissionsResult: Permissions granted = $areLocationPermissionsGranted")
            if (!areLocationPermissionsGranted) {
                Log.w("MainActivity", "Location permissions denied by the user.")
            }
        }
    }
}
