package com.adbmockgps

object CoordinateValidation {
    private val LATITUDE_RANGE = -90.0..90.0
    private val LONGITUDE_RANGE = -180.0..180.0

    fun isValid(latitude: Double, longitude: Double): Boolean =
        latitude in LATITUDE_RANGE && longitude in LONGITUDE_RANGE
}
