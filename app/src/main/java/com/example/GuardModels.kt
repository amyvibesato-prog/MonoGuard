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
        get() = endpoint.isNotBlank()

    companion object {
        fun fromUri(uriString: String): GuardPairingData? {
            val clean = uriString.trim()
            if (clean.isBlank()) return null
            return try {
                // 1. Try JSON parsing
                if (clean.startsWith("{") && clean.endsWith("}")) {
                    try {
                        val json = org.json.JSONObject(clean)
                        val endpoint = json.optString("endpoint", json.optString("url", json.optString("endpointUrl", "")))
                        val secret = json.optString("secret", json.optString("guardSecret", json.optString("code", "")))
                        val phone = json.optString("phone", "")
                        val device = json.optString("device", "")
                        if (endpoint.isNotBlank() || secret.isNotBlank()) {
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
                }

                val uri = Uri.parse(clean)
                val scheme = uri.scheme?.lowercase().orEmpty()

                var endpoint = uri.getQueryParameter("endpoint").orEmpty()
                var secret = uri.getQueryParameter("secret").orEmpty()
                var phone = uri.getQueryParameter("phone").orEmpty()
                var device = uri.getQueryParameter("device").orEmpty()

                if (endpoint.isBlank()) {
                    endpoint = uri.getQueryParameter("url").orEmpty().ifBlank {
                        uri.getQueryParameter("endpointUrl").orEmpty()
                    }
                }
                if (secret.isBlank()) {
                    secret = uri.getQueryParameter("key").orEmpty().ifBlank {
                        uri.getQueryParameter("code").orEmpty().ifBlank {
                            uri.getQueryParameter("guardSecret").orEmpty()
                        }
                    }
                }

                // If query params endpoint/secret were in the URI
                if (endpoint.isNotBlank() || secret.isNotBlank()) {
                    return GuardPairingData(
                        rawUri = clean,
                        endpoint = endpoint,
                        secret = secret,
                        phone = phone,
                        device = device
                    )
                }

                // If scheme is monovoice / monovoice-guard
                if (scheme == "monovoice-guard" || scheme == "monovoice") {
                    return GuardPairingData(
                        rawUri = clean,
                        endpoint = endpoint,
                        secret = secret,
                        phone = phone,
                        device = device
                    )
                }

                // If HTTP or HTTPS URL, use clean as endpoint
                if (scheme == "http" || scheme == "https") {
                    return GuardPairingData(
                        rawUri = clean,
                        endpoint = clean,
                        secret = secret,
                        phone = phone,
                        device = device
                    )
                }

                // Fallback for any other scanned text
                GuardPairingData(
                    rawUri = clean,
                    endpoint = clean,
                    secret = secret,
                    phone = phone,
                    device = device
                )
            } catch (_: Exception) {
                GuardPairingData(
                    rawUri = clean,
                    endpoint = clean,
                    secret = "",
                    phone = "",
                    device = ""
                )
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

interface MonoVoiceGuardApi {
    @GET
    suspend fun getIncidents(
        @Url url: String
    ): Response<IncidentsResponse>

    @POST
    suspend fun sendConnectivityTest(
        @Url url: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ConnectivityTestRequest
    ): Response<ConnectivityTestResponse>
}
