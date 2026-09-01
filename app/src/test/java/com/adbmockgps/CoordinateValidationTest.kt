package com.adbmockgps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinateValidationTest {

    @Test
    fun `accepts coordinates within range`() {
        assertTrue(CoordinateValidation.isValid(51.5074, -0.1278))
        assertTrue(CoordinateValidation.isValid(0.0, 0.0))
    }

    @Test
    fun `accepts boundary values`() {
        assertTrue(CoordinateValidation.isValid(90.0, 180.0))
        assertTrue(CoordinateValidation.isValid(-90.0, -180.0))
    }

    @Test
    fun `rejects out-of-range latitude`() {
        assertFalse(CoordinateValidation.isValid(90.1, 0.0))
        assertFalse(CoordinateValidation.isValid(-90.1, 0.0))
    }

    @Test
    fun `rejects out-of-range longitude`() {
        assertFalse(CoordinateValidation.isValid(0.0, 180.1))
        assertFalse(CoordinateValidation.isValid(0.0, -180.1))
    }
}
