package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `reject qr uri without secret or arbitrary text`() {
        assertNull(GuardPairingData.fromUri("https://monovoice.app"))
        assertNull(GuardPairingData.fromUri("monovoice-guard://pair?endpoint=https%3A%2F%2Fmonovoice.app"))
        assertNull(GuardPairingData.fromUri("{\"endpoint\":\"https://monovoice.app\"}"))
        assertNull(GuardPairingData.fromUri("random arbitrary qr text"))
        assertNull(GuardPairingData.fromUri("mailto:test@example.com"))

        val invalidData = GuardPairingData(
            rawUri = "https://monovoice.app",
            endpoint = "https://monovoice.app",
            secret = "",
            phone = "",
            device = ""
        )
        assertFalse(invalidData.isValid)
    }

    @Test
    fun `guard storage saves offset and clears all pairing fields`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = GuardStorage.getInstance(context)

        storage.savePairing(
            endpoint = "https://custom.monovoice.app/",
            secret = "0102030405060708",
            phone = "+380971234567",
            device = "Pixel 9"
        )
        storage.serverTimeOffsetMs = 4500L

        assertTrue(storage.isPaired)
        assertEquals("https://custom.monovoice.app", storage.endpointUrl)
        assertEquals("Pixel 9", storage.deviceName)
        assertEquals(4500L, storage.serverTimeOffsetMs)

        storage.clearPairing()

        assertFalse(storage.isPaired)
        assertEquals("", storage.secretKey)
        assertEquals("", storage.phoneNumber)
        assertEquals(0L, storage.pairedAt)
        assertEquals(0L, storage.serverTimeOffsetMs)
        assertEquals("Android Phone", storage.deviceName)
        assertEquals(
            "https://ais-pre-hcrs5wd7lpf4dflwp6mw2k-53018745059.europe-west2.run.app",
            storage.endpointUrl
        )
    }

    @Test
    fun `test guard 15-second totp calculation and time offset`() {
        val secret = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20"
        val baseNowMs = 1700000000000L
        val offsetMs = 15000L

        val code = GuardCrypto.calculateCurrentCode(secret, baseNowMs)
        assertNotNull(code)
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })

        val shiftedCode = GuardCrypto.calculateCurrentCode(secret, baseNowMs, offsetMs)
        val directFutureCode = GuardCrypto.calculateCurrentCode(secret, baseNowMs + offsetMs, 0L)
        assertEquals(directFutureCode, shiftedCode)

        val secondsLeft = GuardCrypto.getSecondsRemaining(baseNowMs)
        assertTrue(secondsLeft in 1..15)
        assertEquals(
            GuardCrypto.getSecondsRemaining(baseNowMs + 5000L),
            GuardCrypto.getSecondsRemaining(baseNowMs, 5000L)
        )
        assertEquals(
            GuardCrypto.getWindowProgressFraction(baseNowMs + 3500L),
            GuardCrypto.getWindowProgressFraction(baseNowMs, 3500L),
            0.0001f
        )
    }

    @Test
    fun `retrofit can create MonoVoiceGuardApi and serialize models`() {
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

        val timeAdapter = moshi.adapter(GuardTimeResponse::class.java)
        val parsedTime = timeAdapter.fromJson("""{"success":true,"serverTimeMs":1700000123456,"stepSeconds":15}""")
        assertNotNull(parsedTime)
        assertTrue(parsedTime!!.success)
        assertEquals(1700000123456L, parsedTime.serverTimeMs)
        assertEquals(15, parsedTime.stepSeconds)
    }

    @Test
    fun `extractPackedLuminance and rotateLuminanceBytes decode qr across all rotations`() {
        val payload = "monovoice-guard://pair?endpoint=https%3A%2F%2Fmonovoice.app&secret=0102030405060708"
        val bitMatrix = com.google.zxing.qrcode.QRCodeWriter().encode(
            payload,
            com.google.zxing.BarcodeFormat.QR_CODE,
            160,
            160
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val rowStride = width + 32 // padded row stride, last row truncated without trailing padding
        val bufferSize = rowStride * (height - 1) + width
        val rawY = ByteArray(bufferSize)
        for (y in 0 until height) {
            for (x in 0 until width) {
                rawY[y * rowStride + x] = if (bitMatrix[x, y]) 0x00.toByte() else 0xFF.toByte()
            }
        }

        val packed = extractPackedLuminance(
            buffer = java.nio.ByteBuffer.wrap(rawY),
            width = width,
            height = height,
            rowStride = rowStride,
            pixelStride = 1
        )
        assertEquals(width * height, packed.size)

        val reader = com.google.zxing.MultiFormatReader().apply {
            setHints(
                mapOf(
                    com.google.zxing.DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE),
                    com.google.zxing.DecodeHintType.TRY_HARDER to true
                )
            )
        }

        for (rotation in listOf(0, 90, 180, 270)) {
            val (rotated, rotW, rotH) = rotateLuminanceBytes(packed, width, height, rotation)
            val source = com.google.zxing.PlanarYUVLuminanceSource(
                rotated, rotW, rotH, 0, 0, rotW, rotH, false
            )
            val result = reader.decodeWithState(
                com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
            )
            reader.reset()
            assertEquals(payload, result.text)
        }
    }
}
