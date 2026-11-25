package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberTextDark
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberYellow
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCameraScannerDialog(
    onQrScanned: (GuardPairingData) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CyberCardShape)
                .background(CyberCardDark)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.qr_scanner_title),
                            color = CyberTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = stringResource(id = R.string.qr_scanner_instruction),
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardSurface)
                            .border(1.dp, CyberYellow.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                            .testTag("qr_scanner_close_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрити",
                            tint = CyberYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (hasCameraPermission) {
                    // Live CameraX Preview View
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, CyberYellow, RoundedCornerShape(14.dp))
                    ) {
                        CameraXPreview(
                            onQrScanned = { pairing ->
                                vibrateFeedback(context)
                                onQrScanned(pairing)
                                onDismiss()
                            }
                        )

                        // Cyberpunk Scanner Reticle & Laser Overlay
                        ScannerOverlay()
                    }
                } else {
                    // Permission Request Fallback View
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCardSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(id = R.string.qr_camera_permission_needed),
                                color = CyberTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CyberButtonShape)
                                    .background(CyberYellow)
                                    .clickable { permissionLauncher.launch(Manifest.permission.CAMERA) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.btn_grant_camera_permission),
                                    color = CyberTextDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel Button
                Surface(
                    color = CyberCardSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberYellow.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Скасувати сканування",
                            color = CyberYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraXPreview(
    onQrScanned: (GuardPairingData) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isScanned by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val hints = mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER to true,
                    DecodeHintType.CHARACTER_SET to "UTF-8"
                )
                val reader = MultiFormatReader().apply { setHints(hints) }

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (!isScanned) {
                        val scannedText = decodeQrFromImageProxy(imageProxy, reader)
                        if (!scannedText.isNullOrBlank()) {
                            val pairing = GuardPairingData.fromUri(scannedText)
                            if (pairing != null && pairing.isValid) {
                                isScanned = true
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    Toast.makeText(context, "QR-код успішно розпізнано!", Toast.LENGTH_SHORT).show()
                                }
                                onQrScanned(pairing)
                            }
                        }
                    }
                    imageProxy.close()
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    Log.d("QrScanner", "CameraX bound successfully with Preview and ImageAnalysis analyzer")
                } catch (e: Exception) {
                    Log.e("QrScanner", "Failed to bind CameraX lifecycle", e)
                }
            }, ContextCompat.getMainExecutor(context))
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserYProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height
                val boxSize = minOf(w, h) * 0.65f
                val left = (w - boxSize) / 2f
                val top = (h - boxSize) / 2f
                val right = left + boxSize
                val bottom = top + boxSize
                val cornerLen = 32f

                // Corner Reticles
                val p = Path().apply {
                    // Top-Left
                    moveTo(left, top + cornerLen)
                    lineTo(left, top)
                    lineTo(left + cornerLen, top)

                    // Top-Right
                    moveTo(right - cornerLen, top)
                    lineTo(right, top)
                    lineTo(right, top + cornerLen)

                    // Bottom-Right
                    moveTo(right, bottom - cornerLen)
                    lineTo(right, bottom)
                    lineTo(right - cornerLen, bottom)

                    // Bottom-Left
                    moveTo(left + cornerLen, bottom)
                    lineTo(left, bottom)
                    lineTo(left, bottom - cornerLen)
                }

                drawPath(
                    path = p,
                    color = CyberYellow,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                )

                // Laser Scan Line
                val currentLaserY = top + (boxSize * laserYProgress)
                drawLine(
                    color = CyberYellow,
                    start = Offset(left + 10f, currentLaserY),
                    end = Offset(right - 10f, currentLaserY),
                    strokeWidth = 3f
                )
            }
    )
}

private fun decodeQrFromImageProxy(imageProxy: ImageProxy, reader: MultiFormatReader): String? {
    if (imageProxy.format != ImageFormat.YUV_420_888 && imageProxy.format != ImageFormat.NV21) {
        return null
    }

    val yPlane = imageProxy.planes[0]
    val yBuffer = yPlane.buffer
    val rowStride = yPlane.rowStride
    val width = imageProxy.width
    val height = imageProxy.height

    val bytes = ByteArray(yBuffer.remaining())
    yBuffer.get(bytes)

    // PlanarYUVLuminanceSource dataWidth MUST be rowStride to account for padding
    val source = PlanarYUVLuminanceSource(
        bytes,
        rowStride,
        height,
        0,
        0,
        width,
        height,
        false
    )

    val rotation = imageProxy.imageInfo.rotationDegrees
    val rotatedSource: LuminanceSource = if (rotation == 90 || rotation == 180 || rotation == 270) {
        var s: LuminanceSource = source
        val times = (360 - rotation) / 90
        for (i in 0 until (times % 4)) {
            s = s.rotateCounterClockwise()
        }
        s
    } else {
        source
    }

    // Try decoding rotated source with HybridBinarizer
    try {
        val bitmap = BinaryBitmap(HybridBinarizer(rotatedSource))
        val result = reader.decode(bitmap)
        Log.d("QrScanner", "Successfully decoded QR: ${result.text}")
        return result.text
    } catch (_: Exception) {
    } finally {
        reader.reset()
    }

    // Try decoding rotated source with GlobalHistogramBinarizer
    try {
        val bitmap = BinaryBitmap(GlobalHistogramBinarizer(rotatedSource))
        val result = reader.decode(bitmap)
        Log.d("QrScanner", "Successfully decoded QR (GlobalHistogram): ${result.text}")
        return result.text
    } catch (_: Exception) {
    } finally {
        reader.reset()
    }

    // Fallback: try unrotated source if rotation was non-zero
    if (rotation != 0) {
        try {
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decode(bitmap)
            Log.d("QrScanner", "Successfully decoded QR (unrotated): ${result.text}")
            return result.text
        } catch (_: Exception) {
        } finally {
            reader.reset()
        }
    }

    return null
}

private fun vibrateFeedback(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    } catch (_: Exception) {
    }
}
