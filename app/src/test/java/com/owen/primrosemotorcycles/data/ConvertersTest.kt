package com.owen.primrosemotorcycles.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromJobStatus_validStatus_returnsString() {
        assertEquals("OPEN", converters.fromJobStatus(JobStatus.OPEN))
        assertEquals("IN_PROGRESS", converters.fromJobStatus(JobStatus.IN_PROGRESS))
        assertEquals("DONE", converters.fromJobStatus(JobStatus.DONE))
    }

    @Test
    fun fromJobStatus_null_returnsNull() {
        assertEquals(null, converters.fromJobStatus(null))
    }

    @Test
    fun toJobStatus_validString_returnsStatus() {
        assertEquals(JobStatus.OPEN, converters.toJobStatus("OPEN"))
        assertEquals(JobStatus.COLLECTED, converters.toJobStatus("COLLECTED"))
    }

    @Test
    fun toJobStatus_invalidString_returnsDefaultOpen() {
        assertEquals(JobStatus.OPEN, converters.toJobStatus("INVALID_STATUS"))
    }

    @Test
    fun toJobStatus_null_returnsNull() {
        assertEquals(null, converters.toJobStatus(null))
    }
}
