package com.adbmockgps

import android.location.LocationManager

/**
 * Single source of truth for the location providers this app mocks.
 */
object LocationProviders {
    const val GPS = LocationManager.GPS_PROVIDER
    const val NETWORK = LocationManager.NETWORK_PROVIDER
    const val FUSED = "fused"
    const val PASSIVE = LocationManager.PASSIVE_PROVIDER

    val ALL = listOf(GPS, NETWORK, FUSED, PASSIVE)
}
