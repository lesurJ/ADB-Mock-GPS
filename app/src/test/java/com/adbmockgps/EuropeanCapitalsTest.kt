package com.adbmockgps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EuropeanCapitalsTest {

    @Test
    fun `every capital has in-range coordinates`() {
        EuropeanCapitals.CAPITALS.forEach { capital ->
            assertTrue(
                "${capital.city} has out-of-range coordinates",
                CoordinateValidation.isValid(capital.latitude, capital.longitude)
            )
        }
    }

    @Test
    fun `capital cities are unique`() {
        val cities = EuropeanCapitals.CAPITALS.map { it.city }
        assertEquals(cities.size, cities.toSet().size)
    }
}
