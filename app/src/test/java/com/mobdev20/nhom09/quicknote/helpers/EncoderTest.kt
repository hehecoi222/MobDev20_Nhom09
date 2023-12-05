package com.mobdev20.nhom09.quicknote.helpers

import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EncoderTest {
    @Test
    fun testEncode() {
        val input = "Hello, World!"
        val expectedEncodedValue = "SGVsbG8sIFdvcmxkIQ=="
        val actualEncodedValue = Encoder.encode(input)
        assertEquals(expectedEncodedValue, actualEncodedValue)
    }

    @Test
    fun testDecode() {
        val input = "SGVsbG8sIFdvcmxkIQ=="
        val expectedDecodedValue = "Hello, World!"
        val actualDecodedValue = Encoder.decode(input)
        assertEquals(expectedDecodedValue, actualDecodedValue)
    }
}