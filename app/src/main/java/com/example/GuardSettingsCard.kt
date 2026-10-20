package com.example

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberTextDark
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberYellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

@Composable
fun GuardSettingsCard(
    isPaired: Boolean,
    phoneNumber: String,
    deviceName: String,
    endpointUrl: String,
    onUnpair: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isTestingConn by remember { mutableStateOf(false) }
    var connTestResult by remember { mutableStateOf<String?>(null) }

    val runConnectivityTest = {
        scope.launch {
            isTestingConn = true
            connTestResult = null
            try {
                val cleanEndpoint = endpointUrl.trim().removeSuffix("/")
                val testUrl = "$cleanEndpoint/api/admin/device-auth/connectivity-test"

                val testId = "test_" + UUID.randomUUID().toString().replace("-", "").take(10)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val sentAt = isoFormat.format(Date())

                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val okHttp = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("$cleanEndpoint/")
                    .client(okHttp)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()

                val api = retrofit.create(MonoVoiceGuardApi::class.java)
                val response = withContext(Dispatchers.IO) {
                    api.sendConnectivityTest(testUrl, request = ConnectivityTestRequest(testId, "2.0.0", sentAt))
                }

                if (response.isSuccessful) {
                    connTestResult = "✅ Сервер доступний! Відповідь отримано."
                } else {
                    connTestResult = "⚠️ HTTP ${response.code()}: помилка сервера"
                }
            } catch (e: Exception) {
                connTestResult = "❌ Не вдалося зв’язатися: ${e.localizedMessage ?: "Перевірте мережу"}"
            } finally {
                isTestingConn = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CyberCardShape)
            .background(CyberCardDark)
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyberYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "НАЛАШТУВАННЯ ЗВ’ЯЗКУ",
                    color = CyberTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phone
            Surface(
                color = CyberCardSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Номер телефону:", color = CyberTextSecondary, fontSize = 12.sp)
                    }
                    Text(
                        text = if (phoneNumber.isNotBlank()) phoneNumber else "Не вказано",
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Device
            Surface(
                color = CyberCardSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Smartphone, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Назва пристрою:", color = CyberTextSecondary, fontSize = 12.sp)
                    }
                    Text(
                        text = deviceName.ifBlank { "Android Phone" },
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Endpoint
            Surface(
                color = CyberCardSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Серверний Endpoint:", color = CyberTextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = endpointUrl,
                        color = CyberYellow,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Connectivity Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(CyberButtonShape)
                    .background(CyberCardSurface)
                    .border(1.dp, CyberYellow.copy(alpha = 0.6f), CyberButtonShape)
                    .clickable(enabled = !isTestingConn) { runConnectivityTest() },
                contentAlignment = Alignment.Center
            ) {
                if (isTestingConn) {
                    CircularProgressIndicator(color = CyberYellow, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Radio, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Тест доступності сервера (Stage 1)", color = CyberYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (connTestResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = connTestResult!!,
                    color = if (connTestResult!!.startsWith("✅")) CyberSuccess else Color(0xFFF87171),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isPaired) {
                Spacer(modifier = Modifier.height(16.dp))
                // Unpair button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(CyberButtonShape)
                        .background(Color(0xFF3F1D1D))
                        .clickable { onUnpair() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Скинути прив’язку на цьому телефоні", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
