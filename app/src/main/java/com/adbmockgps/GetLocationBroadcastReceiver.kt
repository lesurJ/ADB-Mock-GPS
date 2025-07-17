package com.adbmockgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GetLocationBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_GET_LOCATION = "com.adbmockgps.GET_LOCATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_GET_LOCATION) {
            return
        }

        Log.i("ADBMockGPS", "=== BROADCAST RECEIVER TRIGGERED ===")
        Log.i("ADBMockGPS", "Action: ${intent.action}")

        val lastBroadcast = BroadcastStateRepository.lastBroadcast.value
        val resultData = if (lastBroadcast != null) {
            "${lastBroadcast.latitude},${lastBroadcast.longitude},${lastBroadcast.altitude}"
        } else {
            "NA"
        }

        setResultData(resultData)
        Log.i("ADBMockGPS", "Result: $resultData")
    }
}