package com.adbmockgps

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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

    private var areLocationPermissionsGranted by mutableStateOf(false)
    private var isMockAppSelected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            areLocationPermissionsGranted = permissions.all { it.value }
            Log.d("MainActivity", "Permission result received. Granted: $areLocationPermissionsGranted")
            checkPrerequisites()
        }

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
                    arePermissionsGranted=areLocationPermissionsGranted,
                    isMockAppSelected=isMockAppSelected,
                    lastBroadcastInfo = lastBroadcastInfo,
                    onGrantPermissions = {
                        permissionRequestLauncher.launch(locationPermissions)
                    },
                    onSetMockApp = {
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
        areLocationPermissionsGranted = locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        Log.d("MainActivity", "Permissions granted: $areLocationPermissionsGranted")

        try {
            val mockLocationApp = Settings.Secure.getString(contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION)
            isMockAppSelected = mockLocationApp?.contains(packageName) == true
            Log.d("MainActivity", "Is this app the selected mock provider? $isMockAppSelected $mockLocationApp")
        } catch (e: Exception) {
            isMockAppSelected = false
            Log.e("MainActivity", "Could not determine mock location app.", e)
        }
    }

}
