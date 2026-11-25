package com.example

import java.nio.ByteBuffer
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object GuardCrypto {
    const val STEP_SECONDS = 15

    /**
     * Calculates the 6-digit TOTP code for the current 15-second window
     * matching the website's GuardService implementation.
     */
    fun calculateCurrentCode(
        secretHex: String,
        nowMs: Long = System.currentTimeMillis(),
        timeOffsetMs: Long = 0L
    ): String {
        if (secretHex.isBlank()) return "000000"
        return try {
            val effectiveNowMs = nowMs + timeOffsetMs
            val counter = (effectiveNowMs / 1000L) / STEP_SECONDS.toLong()
            val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()

            val secretBytes = hexStringToByteArray(secretHex.trim())
            val keySpec = SecretKeySpec(secretBytes, "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256").apply {
                init(keySpec)
            }
            val hmac = mac.doFinal(counterBytes)

            // Dynamic Truncation (RFC 4226)
            val offset = (hmac[hmac.size - 1].toInt() and 0x0f)
            val binary = ((hmac[offset].toInt() and 0x7f) shl 24) or
                    ((hmac[offset + 1].toInt() and 0xff) shl 16) or
                    ((hmac[offset + 2].toInt() and 0xff) shl 8) or
                    (hmac[offset + 3].toInt() and 0xff)

            val otp = binary % 1000000
            String.format(Locale.US, "%06d", otp)
        } catch (_: Exception) {
            "000000"
        }
    }

    /**
     * Calculates remaining seconds in the current 15-second window
     */
    fun getSecondsRemaining(
        nowMs: Long = System.currentTimeMillis(),
        timeOffsetMs: Long = 0L
    ): Int {
        val effectiveNowMs = nowMs + timeOffsetMs
        val seconds = (((effectiveNowMs / 1000L) % STEP_SECONDS) + STEP_SECONDS) % STEP_SECONDS
        val remaining = STEP_SECONDS - seconds.toInt()
        return if (remaining == 0) STEP_SECONDS else remaining
    }

    /**
     * Milliseconds elapsed inside the 15-second window (0..14999ms)
     * For buttery smooth circular progress animation
     */
    fun getWindowProgressFraction(
        nowMs: Long = System.currentTimeMillis(),
        timeOffsetMs: Long = 0L
    ): Float {
        val effectiveNowMs = nowMs + timeOffsetMs
        val stepMs = STEP_SECONDS * 1000L
        val windowMs = ((effectiveNowMs % stepMs) + stepMs) % stepMs
        return windowMs.toFloat() / stepMs.toFloat()
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val clean = s.filter { it.isLetterOrDigit() }
        val len = clean.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len - 1) {
            data[i / 2] = ((Character.digit(clean[i], 16) shl 4) + Character.digit(clean[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
