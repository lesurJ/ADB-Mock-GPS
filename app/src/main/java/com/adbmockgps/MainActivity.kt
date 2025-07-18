package com.adbmockgps

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.adbmockgps.ui.theme.ADBMockGPSTheme

class MainActivity : ComponentActivity() {

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private var hasLocationPermissions by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(false)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        checkPrerequisites()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        checkPrerequisites()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ADBMockGPSTheme {
                val lastBroadcastInfo by BroadcastStateRepository.lastBroadcast.collectAsState()
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

                LaunchedEffect(lifecycleState) {
                    if (lifecycleState == Lifecycle.State.RESUMED) {
                        Log.d("MainActivity", "App resumed, checking prerequisites.")
                        checkPrerequisites()
                    }
                }

                LocationScreen(
                    hasLocationPermissions=hasLocationPermissions,
                    hasNotificationPermission=hasNotificationPermission,
                    lastBroadcastInfo = lastBroadcastInfo,
                    onGrantLocationPermissions = {
                        locationPermissionLauncher.launch(locationPermissions)
                    },
                    onGrantNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenDeveloperOptions = {
                        AlertDialog.Builder(this)
                            .setTitle("Enable Mock Location")
                            .setMessage("To enable mock locations, please select this app in Developer Options > Select mock location app.")
                            .setPositiveButton("Open Developer Options") { _, _ ->
                                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                )
            }
        }
    }

    private fun checkPrerequisites() {
        hasLocationPermissions = locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        Log.d("MainActivity", "Location permissions granted: $hasLocationPermissions")

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

}
