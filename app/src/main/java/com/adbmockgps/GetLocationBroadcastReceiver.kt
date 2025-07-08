package com.adbmockgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GetLocationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("ADBMockGPS", "=== BROADCAST RECEIVER TRIGGERED ===")
        Log.i("ADBMockGPS", "Action: ${intent.action}")

        val location = LocationRepository.getLocation()

        val resultData = if (location != null) {
            // Format the output string for easy parsing in scripts.
            // Format: LATITUDE,LONGITUDE,ALTITUDE
            "${location.latitude},${location.longitude},${location.altitude}"
        } else {
            "NA"
        }

        // This is the magic. It sets the result of the broadcast, which is
        // printed to the console by the `am broadcast` command.
        setResultData(resultData)
        Log.i("ADBMockGPS", "Result: $resultData")
    }
}