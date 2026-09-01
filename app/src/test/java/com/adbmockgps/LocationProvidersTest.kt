package com.adbmockgps

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationProvidersTest {

    @Test
    fun `ALL contains exactly the four mocked providers, no duplicates`() {
        val expected = setOf(
            LocationProviders.GPS,
            LocationProviders.NETWORK,
            LocationProviders.FUSED,
            LocationProviders.PASSIVE
        )
        assertEquals(4, expected.size)
        assertEquals(expected, LocationProviders.ALL.toSet())
        assertEquals(LocationProviders.ALL.size, LocationProviders.ALL.toSet().size)
    }
}
