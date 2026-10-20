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
                val uri = Uri.parse(clean)
                val scheme = uri.scheme?.lowercase()
                val host = uri.host?.lowercase()

                if ((scheme == "monovoice-guard" || scheme == "monovoice") &&
                    (host == "pair" || host.isNullOrBlank())
                ) {
                    val endpoint = uri.getQueryParameter("endpoint").orEmpty()
                    val secret = uri.getQueryParameter("secret").orEmpty()
                    val phone = uri.getQueryParameter("phone").orEmpty()
                    val device = uri.getQueryParameter("device").orEmpty()

                    GuardPairingData(
                        rawUri = clean,
                        endpoint = endpoint,
                        secret = secret,
                        phone = phone,
                        device = device
                    )
                } else null
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
