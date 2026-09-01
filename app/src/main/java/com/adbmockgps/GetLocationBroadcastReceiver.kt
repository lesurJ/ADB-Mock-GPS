package com.adbmockgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GetLocationBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var broadcastStateRepository: BroadcastStateRepository

    companion object {
        const val ACTION_GET_LOCATION = "com.adbmockgps.GET_LOCATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_GET_LOCATION) {
            return
        }

        Log.i(LOG_TAG, "=== BROADCAST RECEIVER TRIGGERED ===")
        Log.i(LOG_TAG, "Action: ${intent.action}")

        val resultData = broadcastStateRepository.lastBroadcast.value.toResultString()

        setResultData(resultData)
        Log.i(LOG_TAG, "Result: $resultData")
    }
}