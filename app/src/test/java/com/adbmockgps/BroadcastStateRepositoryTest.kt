package com.adbmockgps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BroadcastStateRepositoryTest {

    @Test
    fun `initial state is null`() {
        val repository = BroadcastStateRepository()
        assertNull(repository.lastBroadcast.value)
    }

    @Test
    fun `updateLastBroadcast publishes the new state`() {
        val repository = BroadcastStateRepository()

        repository.updateLastBroadcast(51.5074, -0.1278, 35.0, "2026-09-01 12:00:00")

        val state = repository.lastBroadcast.value
        assertEquals(51.5074, state?.latitude)
        assertEquals(-0.1278, state?.longitude)
        assertEquals(35.0, state?.altitude)
        assertEquals("2026-09-01 12:00:00", state?.timestamp)
    }

    @Test
    fun `toResultString formats latitude, longitude and altitude`() {
        val info = LastBroadcastInfo(51.5074, -0.1278, 35.0, "2026-09-01 12:00:00")
        assertEquals("51.5074,-0.1278,35.0", info.toResultString())
    }

    @Test
    fun `toResultString leaves altitude empty rather than the string 'null'`() {
        val info = LastBroadcastInfo(51.5074, -0.1278, null, "2026-09-01 12:00:00")
        assertEquals("51.5074,-0.1278,", info.toResultString())
    }

    @Test
    fun `toResultString returns NA when there is no broadcast yet`() {
        val info: LastBroadcastInfo? = null
        assertEquals("NA", info.toResultString())
    }
}
