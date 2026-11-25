package com.example

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberTextDark
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

val CyberCardShape = GenericShape { size, _ ->
    val cut = 28f
    moveTo(cut, 0f)
    lineTo(size.width - cut, 0f)
    lineTo(size.width, cut)
    lineTo(size.width, size.height - cut)
    lineTo(size.width - cut, size.height)
    lineTo(cut, size.height)
    lineTo(0f, size.height - cut)
    lineTo(0f, cut)
    close()
}

val CyberButtonShape = GenericShape { size, _ ->
    val cut = 14f
    moveTo(cut, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - cut)
    lineTo(size.width - cut, size.height)
    lineTo(0f, size.height)
    lineTo(0f, cut)
    close()
}

@Composable
fun GuardCard(
    isPaired: Boolean,
    secretKey: String,
    phoneNumber: String,
    timeOffsetMs: Long = 0L,
    onOpenScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var currentCode by remember { mutableStateOf("000000") }
    var secondsLeft by remember { mutableIntStateOf(15) }
    var progressFraction by remember { mutableFloatStateOf(0f) }

    // Live 100ms ticker for 15s Steam Guard TOTP
    LaunchedEffect(isPaired, secretKey, timeOffsetMs) {
        if (!isPaired || secretKey.isBlank()) {
            currentCode = "000000"
            secondsLeft = GuardCrypto.STEP_SECONDS
            progressFraction = 0f
            return@LaunchedEffect
        }
        while (isActive) {
            val now = System.currentTimeMillis()
            currentCode = GuardCrypto.calculateCurrentCode(secretKey, now, timeOffsetMs)
            secondsLeft = GuardCrypto.getSecondsRemaining(now, timeOffsetMs)
            progressFraction = GuardCrypto.getWindowProgressFraction(now, timeOffsetMs)
            delay(100L)
        }
    }

    val timerColor by animateColorAsState(
        targetValue = if (secondsLeft <= 4) Color(0xFFEF4444) else CyberYellow,
        animationSpec = tween(300),
        label = "timerColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CyberCardShape)
            .background(CyberCardDark)
            .drawBehind {
                val cut = 28f
                val cornerSize = 22f

                // Corners
                val tl = Path().apply {
                    moveTo(0f, cut); lineTo(cut, 0f); lineTo(cut + cornerSize, 0f); lineTo(0f, cut + cornerSize); close()
                }
                drawPath(tl, color = CyberYellow)

                val tr = Path().apply {
                    moveTo(size.width - cut - cornerSize, 0f); lineTo(size.width - cut, 0f); lineTo(size.width, cut); lineTo(size.width, cut + cornerSize); close()
                }
                drawPath(tr, color = CyberYellow)

                val bl = Path().apply {
                    moveTo(0f, size.height - cut - cornerSize); lineTo(0f, size.height - cut); lineTo(cut, size.height); lineTo(cut + cornerSize, size.height); close()
                }
                drawPath(bl, color = CyberYellow)

                val br = Path().apply {
                    moveTo(size.width - cut - cornerSize, size.height); lineTo(size.width - cut, size.height); lineTo(size.width, size.height - cut); lineTo(size.width, size.height - cut - cornerSize); close()
                }
                drawPath(br, color = CyberYellow)
            }
            .padding(horizontal = 22.dp, vertical = 26.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isPaired) CyberSuccess else CyberYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STEAM GUARD РЕЖИМ",
                        color = CyberTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }

                Surface(
                    color = if (isPaired) CyberSuccess.copy(alpha = 0.15f) else Color(0xFF3F1D1D),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPaired) CyberSuccess.copy(alpha = 0.5f) else Color(0xFFEF4444)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPaired) CyberSuccess else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isPaired) "АКТИВНИЙ" else "НЕ ПРИВ’ЯЗАНО",
                            color = if (isPaired) CyberSuccess else Color(0xFFEF4444),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!isPaired) {
                // Unpaired state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CyberYellow,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Додаток не підключено до сайту",
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Для генерації 15-секундних кодів підтвердження відскануйте QR-код в адмін-панелі сайту.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(46.dp)
                            .clip(CyberButtonShape)
                            .background(CyberYellow)
                            .clickable { onOpenScanner() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = CyberTextDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Сканувати QR зв’язки",
                                color = CyberTextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            } else {
                // Paired active Steam Guard State
                // Phone number badge
                if (phoneNumber.isNotBlank()) {
                    Surface(
                        color = CyberCardSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = CyberYellow,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = phoneNumber,
                                color = CyberTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 15-second Circular Progress Ring & Timer
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f - progressFraction },
                        modifier = Modifier.size(126.dp),
                        color = timerColor,
                        strokeWidth = 7.dp,
                        trackColor = Color(0xFF1E2836),
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${secondsLeft}s",
                            color = timerColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "ТАЙМЕР",
                            color = CyberTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Code Display with Tap to Copy
                val formattedCode = if (currentCode.length == 6) {
                    "${currentCode.substring(0, 3)} ${currentCode.substring(3)}"
                } else currentCode

                Surface(
                    color = CyberCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, timerColor.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboardManager.setText(AnnotatedString(currentCode))
                            Toast.makeText(context, "Код $currentCode скопійовано!", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formattedCode,
                            color = CyberYellow,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Копіювати",
                                tint = CyberTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Натисніть для копіювання",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Код діє строго 15 секунд. Введіть його у спливаюче вікно на сайті для підтвердження змін.",
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
