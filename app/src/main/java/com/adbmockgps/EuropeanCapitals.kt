package com.adbmockgps

/**
 * A European capital city with coordinates used to seed the mock GPS location.
 */
data class Capital(
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)

/**
 * Major European capitals, ordered by city population (most populous first).
 * The UI only offers the first [TOP_COUNT].
 */
object EuropeanCapitals {
    val CAPITALS = listOf(
        Capital("Amsterdam", "Netherlands", 52.367573, 4.904139, 2.0),
        Capital("Berlin", "Germany", 52.520008, 13.404954, 34.0),
        Capital("Bucharest", "Romania", 44.426767, 26.102538, 71.0),
        Capital("Budapest", "Hungary", 47.497913, 19.040236, 102.0),
        Capital("Kyiv", "Ukraine", 50.450100, 30.523400, 179.0),
        Capital("London", "United Kingdom", 51.507351, -0.127758, 11.0),
        Capital("Madrid", "Spain", 40.416775, -3.703790, 657.0),
        Capital("Moscow", "Russia", 55.755826, 37.617300, 156.0),
        Capital("Paris", "France", 48.856614, 2.352222, 35.0),
        Capital("Rome", "Italy", 41.902782, 12.496366, 21.0),
        Capital("Vienna", "Austria", 48.210033, 16.363449, 171.0),
        Capital("Warsaw", "Poland", 52.229676, 21.012229, 100.0),
    )
}
