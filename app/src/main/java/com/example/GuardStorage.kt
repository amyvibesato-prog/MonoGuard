package com.example

import android.content.Context
import android.content.SharedPreferences

class GuardStorage private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isPaired: Boolean
        get() = prefs.getBoolean(KEY_IS_PAIRED, false)
        private set(value) = prefs.edit().putBoolean(KEY_IS_PAIRED, value).apply()

    var secretKey: String
        get() = prefs.getString(KEY_SECRET, "").orEmpty()
        private set(value) = prefs.edit().putString(KEY_SECRET, value).apply()

    var phoneNumber: String
        get() = prefs.getString(KEY_PHONE, "").orEmpty()
        private set(value) = prefs.edit().putString(KEY_PHONE, value).apply()

    var endpointUrl: String
        get() = prefs.getString(KEY_ENDPOINT, "https://ais-pre-hcrs5wd7lpf4dflwp6mw2k-53018745059.europe-west2.run.app").orEmpty()
        private set(value) = prefs.edit().putString(KEY_ENDPOINT, value).apply()

    var deviceName: String
        get() = prefs.getString(KEY_DEVICE_NAME, "Android Phone").orEmpty()
        private set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value).apply()

    var pairedAt: Long
        get() = prefs.getLong(KEY_PAIRED_AT, 0L)
        private set(value) = prefs.edit().putLong(KEY_PAIRED_AT, value).apply()

    var serverTimeOffsetMs: Long
        get() = prefs.getLong(KEY_TIME_OFFSET_MS, 0L)
        set(value) = prefs.edit().putLong(KEY_TIME_OFFSET_MS, value).apply()

    fun savePairing(endpoint: String, secret: String, phone: String, device: String) {
        prefs.edit()
            .putBoolean(KEY_IS_PAIRED, true)
            .putString(KEY_ENDPOINT, endpoint.trim().removeSuffix("/"))
            .putString(KEY_SECRET, secret.trim())
            .putString(KEY_PHONE, phone.trim())
            .putString(KEY_DEVICE_NAME, device.trim().ifBlank { "Android Phone" })
            .putLong(KEY_PAIRED_AT, System.currentTimeMillis())
            .apply()
    }

    fun clearPairing() {
        prefs.edit()
            .putBoolean(KEY_IS_PAIRED, false)
            .remove(KEY_SECRET)
            .remove(KEY_PHONE)
            .remove(KEY_ENDPOINT)
            .remove(KEY_DEVICE_NAME)
            .remove(KEY_PAIRED_AT)
            .remove(KEY_TIME_OFFSET_MS)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "monovoice_guard_prefs"
        private const val KEY_IS_PAIRED = "guard_is_paired"
        private const val KEY_SECRET = "guard_secret_key"
        private const val KEY_PHONE = "guard_phone_number"
        private const val KEY_ENDPOINT = "guard_endpoint_url"
        private const val KEY_DEVICE_NAME = "guard_device_name"
        private const val KEY_PAIRED_AT = "guard_paired_at"
        private const val KEY_TIME_OFFSET_MS = "guard_time_offset_ms"

        @Volatile
        private var INSTANCE: GuardStorage? = null

        fun getInstance(context: Context): GuardStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GuardStorage(context).also { INSTANCE = it }
            }
        }
    }
}
