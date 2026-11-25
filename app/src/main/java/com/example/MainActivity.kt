package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberTextDark
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.MonoVoiceTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {

    private lateinit var storage: GuardStorage
    private val isPairedState = mutableStateOf(false)
    private val secretKeyState = mutableStateOf("")
    private val phoneNumberState = mutableStateOf("")
    private val endpointUrlState = mutableStateOf("")
    private val deviceNameState = mutableStateOf("")
    private val timeOffsetMsState = mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storage = GuardStorage.getInstance(this)
        refreshStateFromStorage()

        // Handle initial intent if app opened via monovoice-guard:// link
        handleIntent(intent)

        setContent {
            MonoVoiceTheme {
                val isPaired by isPairedState
                val secretKey by secretKeyState
                val phoneNumber by phoneNumberState
                val endpointUrl by endpointUrlState
                val deviceName by deviceNameState
                val timeOffsetMs by timeOffsetMsState

                LaunchedEffect(isPaired, endpointUrl) {
                    if (isPaired && endpointUrl.isNotBlank()) {
                        syncServerTime(endpointUrl)
                    }
                }

                MonoVoiceGuardScreen(
                    isPaired = isPaired,
                    secretKey = secretKey,
                    phoneNumber = phoneNumber,
                    endpointUrl = endpointUrl,
                    deviceName = deviceName,
                    timeOffsetMs = timeOffsetMs,
                    onSavePairing = { data ->
                        if (!data.isValid) return@MonoVoiceGuardScreen
                        storage.savePairing(data.endpoint, data.secret, data.phone, data.device)
                        refreshStateFromStorage()
                        Toast.makeText(this, "MonoVoice Guard успішно підключено!", Toast.LENGTH_LONG).show()
                    },
                    onUnpair = {
                        storage.clearPairing()
                        refreshStateFromStorage()
                        Toast.makeText(this, "Прив’язку пристрою скинуто", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        val parsed = GuardPairingData.fromUri(data.toString())
        if (parsed != null && parsed.isValid) {
            storage.savePairing(parsed.endpoint, parsed.secret, parsed.phone, parsed.device)
            refreshStateFromStorage()
            Toast.makeText(this, "MonoVoice Guard успішно прив’язано!", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshStateFromStorage() {
        isPairedState.value = storage.isPaired
        secretKeyState.value = storage.secretKey
        phoneNumberState.value = storage.phoneNumber
        endpointUrlState.value = storage.endpointUrl
        deviceNameState.value = storage.deviceName
        timeOffsetMsState.longValue = storage.serverTimeOffsetMs
    }

    private suspend fun syncServerTime(endpoint: String) {
        val cleanEndpoint = endpoint.trim().removeSuffix("/")
        if (cleanEndpoint.isBlank()) return
        try {
            val offset = withContext(Dispatchers.IO) {
                val url = "$cleanEndpoint/api/admin/guard/time"
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val okHttp = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("$cleanEndpoint/")
                    .client(okHttp)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                val api = retrofit.create(MonoVoiceGuardApi::class.java)

                val t0 = System.currentTimeMillis()
                val response = api.getServerTime(url)
                val t1 = System.currentTimeMillis()

                val body = response.body()
                if (response.isSuccessful && body != null && body.success && body.serverTimeMs > 0L) {
                    body.serverTimeMs - (t0 + (t1 - t0) / 2L)
                } else {
                    null
                }
            }
            if (offset != null && storage.isPaired) {
                storage.serverTimeOffsetMs = offset
                timeOffsetMsState.longValue = offset
            }
        } catch (_: Exception) {
            // Fail silently and keep existing offset when offline
        }
    }
}

@Composable
fun MonoVoiceGuardScreen(
    isPaired: Boolean,
    secretKey: String,
    phoneNumber: String,
    endpointUrl: String,
    deviceName: String,
    timeOffsetMs: Long = 0L,
    onSavePairing: (GuardPairingData) -> Unit,
    onUnpair: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showQrScannerDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Guard Code, 1 = History, 2 = Settings

    Box(modifier = Modifier.fillMaxSize()) {
        // Cyberpunk Street Art Artwork Background
        GraffitiBackground()

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.statusBars
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
                TopCyberGuardBar(
                    onOpenQrScanner = { showQrScannerDialog = true },
                    onPaste = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            val parsed = GuardPairingData.fromUri(clip)
                            if (parsed != null && parsed.isValid) {
                                onSavePairing(parsed)
                            } else {
                                Toast.makeText(context, "Текст у буфері не є monovoice-guard://pair", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Буфер обміну порожній", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // The Cyber Cat Mascot Header
                MascotHeader(modifier = Modifier.padding(bottom = 2.dp))

                // Tab Selector Bar (Guard / Історія / Налаштування)
                GuardTabBar(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Tab 0: Steam Guard 15-Second Code
                            GuardCard(
                                isPaired = isPaired,
                                secretKey = secretKey,
                                phoneNumber = phoneNumber,
                                timeOffsetMs = timeOffsetMs,
                                onOpenScanner = { showQrScannerDialog = true }
                            )
                        }
                        1 -> {
                            // Tab 1: Incidents & Security History ("Коли і де")
                            GuardIncidentHistoryView(
                                endpointUrl = endpointUrl
                            )
                        }
                        2 -> {
                            // Tab 2: Settings & Stage 1 Connectivity Test
                            GuardSettingsCard(
                                isPaired = isPaired,
                                phoneNumber = phoneNumber,
                                deviceName = deviceName,
                                endpointUrl = endpointUrl,
                                onUnpair = onUnpair
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Camera QR Scanner Dialog
        if (showQrScannerDialog) {
            QrCameraScannerDialog(
                onQrScanned = { scannedData ->
                    onSavePairing(scannedData)
                    showQrScannerDialog = false
                },
                onDismiss = { showQrScannerDialog = false }
            )
        }
    }
}

@Composable
fun TopCyberGuardBar(
    onOpenQrScanner: () -> Unit,
    onPaste: () -> Unit
) {
    Surface(
        color = CyberCardDark.copy(alpha = 0.94f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberYellow.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CyberYellow,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "MonoVoice Guard",
                    color = CyberTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Scan QR button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberYellow)
                        .clickable { onOpenQrScanner() }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Сканувати QR",
                            tint = CyberTextDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Сканувати",
                            color = CyberTextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Paste button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCardSurface)
                        .clickable { onPaste() }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Вставити",
                            tint = CyberYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Вставити",
                            color = CyberYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuardTabBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        color = CyberCardSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3844)),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton(
                title = "Код (Guard)",
                icon = Icons.Default.Shield,
                isSelected = selectedTab == 0,
                onClick = { onSelectTab(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "Історія дій",
                icon = Icons.Default.History,
                isSelected = selectedTab == 1,
                onClick = { onSelectTab(1) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "Зв’язок",
                icon = Icons.Default.Settings,
                isSelected = selectedTab == 2,
                onClick = { onSelectTab(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberYellow else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CyberTextDark else CyberTextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                color = if (isSelected) CyberTextDark else CyberTextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
            )
        }
    }
}
