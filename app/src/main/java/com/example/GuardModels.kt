package com.example

import android.net.Uri
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

data class GuardPairingData(
    val rawUri: String,
    val endpoint: String,
    val secret: String,
    val phone: String,
    val device: String
) {
    val isValid: Boolean
        get() = endpoint.isNotBlank() && secret.isNotBlank()

    companion object {
        fun fromUri(uriString: String): GuardPairingData? {
            val clean = uriString.trim()
            if (clean.isBlank()) return null
            return try {
                // 1. Try JSON parsing
                if (clean.startsWith("{") && clean.endsWith("}")) {
                    try {
                        val json = org.json.JSONObject(clean)
                        val endpoint = json.optString("endpoint", json.optString("url", json.optString("endpointUrl", ""))).trim()
                        val secret = json.optString("secret", json.optString("guardSecret", json.optString("code", ""))).trim()
                        val phone = json.optString("phone", "").trim()
                        val device = json.optString("device", "").trim()
                        if (endpoint.isNotBlank() && secret.isNotBlank()) {
                            return GuardPairingData(
                                rawUri = clean,
                                endpoint = endpoint,
                                secret = secret,
                                phone = phone,
                                device = device
                            )
                        }
                    } catch (_: Exception) {
                    }
                    return null
                }

                val uri = Uri.parse(clean)
                if (uri.isOpaque) return null

                var endpoint = uri.getQueryParameter("endpoint").orEmpty().trim()
                var secret = uri.getQueryParameter("secret").orEmpty().trim()
                val phone = uri.getQueryParameter("phone").orEmpty().trim()
                val device = uri.getQueryParameter("device").orEmpty().trim()

                if (endpoint.isBlank()) {
                    endpoint = uri.getQueryParameter("url").orEmpty().trim().ifBlank {
                        uri.getQueryParameter("endpointUrl").orEmpty().trim()
                    }
                }
                if (secret.isBlank()) {
                    secret = uri.getQueryParameter("key").orEmpty().trim().ifBlank {
                        uri.getQueryParameter("code").orEmpty().trim().ifBlank {
                            uri.getQueryParameter("guardSecret").orEmpty().trim()
                        }
                    }
                }

                if (endpoint.isNotBlank() && secret.isNotBlank()) {
                    GuardPairingData(
                        rawUri = clean,
                        endpoint = endpoint,
                        secret = secret,
                        phone = phone,
                        device = device
                    )
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class SecurityIncidentItem(
    val id: String = "",
    val timestamp: String = "",
    val clientIp: String = "",
    val location: String = "Невідомо",
    val userAgent: String = "",
    val actionType: String = "",
    val targetLabel: String = "",
    val reason: String = "EXPIRED_TIMEOUT",
    val details: String = ""
)

@JsonClass(generateAdapter = true)
data class IncidentsResponse(
    val success: Boolean = true,
    val count: Int = 0,
    val incidents: List<SecurityIncidentItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ConnectivityTestRequest(
    val testId: String,
    val appVersion: String = "2.0.0",
    val sentAt: String
)

@JsonClass(generateAdapter = true)
data class ConnectivityTestResponse(
    val success: Boolean? = true,
    val receivedAt: String? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class GuardTimeResponse(
    val success: Boolean = true,
    val serverTimeMs: Long = 0L,
    val stepSeconds: Int = 15
)

interface MonoVoiceGuardApi {
    @GET
    suspend fun getIncidents(
        @Url url: String
    ): Response<IncidentsResponse>

    @GET
    suspend fun getServerTime(
        @Url url: String
    ): Response<GuardTimeResponse>

    @POST
    suspend fun sendConnectivityTest(
        @Url url: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ConnectivityTestRequest
    ): Response<ConnectivityTestResponse>
}
