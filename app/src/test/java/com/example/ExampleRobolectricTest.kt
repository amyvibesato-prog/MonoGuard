package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MonoVoice", appName)
    }

    @Test
    fun `parse monovoice guard pair uri`() {
        val uri = "monovoice-guard://pair?endpoint=https%3A%2F%2Fmonovoice.app&secret=a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2&phone=%2B380971234567&device=MyPhone"
        val parsed = GuardPairingData.fromUri(uri)

        assertNotNull(parsed)
        assertEquals("https://monovoice.app", parsed?.endpoint)
        assertEquals("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2", parsed?.secret)
        assertEquals("+380971234567", parsed?.phone)
        assertEquals("MyPhone", parsed?.device)
        assertTrue(parsed?.isValid == true)
    }

    @Test
    fun `test guard 15-second totp calculation`() {
        val secret = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20"
        val code = GuardCrypto.calculateCurrentCode(secret, 1700000000000L)
        assertNotNull(code)
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })

        val secondsLeft = GuardCrypto.getSecondsRemaining(1700000000000L)
        assertTrue(secondsLeft in 1..15)
    }

    @Test
    fun `retrofit can create MonoVoiceGuardApi and serialize ConnectivityTestRequest`() {
        val moshi = com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://monovoice.app/")
            .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(MonoVoiceGuardApi::class.java)
        assertNotNull(service)

        val request = ConnectivityTestRequest(
            testId = "test_12345",
            appVersion = "2.0.0",
            sentAt = "2026-10-20T12:00:00.000Z"
        )

        val jsonAdapter = moshi.adapter(ConnectivityTestRequest::class.java)
        val json = jsonAdapter.toJson(request)

        assertTrue(json.contains("\"testId\":\"test_12345\""))
        assertTrue(json.contains("\"appVersion\":\"2.0.0\""))
        assertTrue(json.contains("\"sentAt\":\"2026-10-20T12:00:00.000Z\""))
    }
}
