package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import java.util.concurrent.atomic.AtomicBoolean

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
    val isScanned = remember { AtomicBoolean(false) }
    val isDisposed = remember { AtomicBoolean(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val previewView = remember(context) {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(previewView, lifecycleOwner) {
        isDisposed.set(false)
        var boundCameraProvider: ProcessCameraProvider? = null
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            if (isDisposed.get()) return@addListener
            try {
                val cameraProvider = cameraProviderFuture.get()
                if (isDisposed.get()) return@addListener

                val cameraSelector = when {
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> {
                        cameraError = "Камеру не знайдено на цьому пристрої"
                        return@addListener
                    }
                }

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(1280, 720),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val hints = mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER to true,
                    DecodeHintType.CHARACTER_SET to "UTF-8"
                )
                val reader = MultiFormatReader().apply { setHints(hints) }

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    try {
                        if (!isScanned.get() && !isDisposed.get()) {
                            val scannedText = decodeQrFromImageProxy(imageProxy, reader)
                            if (!scannedText.isNullOrBlank()) {
                                val pairing = GuardPairingData.fromUri(scannedText)
                                if (pairing != null && pairing.isValid && isScanned.compareAndSet(false, true)) {
                                    Handler(Looper.getMainLooper()).post {
                                        if (!isDisposed.get()) {
                                            Toast.makeText(context, "QR-код успішно розпізнано!", Toast.LENGTH_SHORT).show()
                                            onQrScanned(pairing)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        Log.w("QrScanner", "Error processing camera frame", e)
                    } finally {
                        imageProxy.close()
                    }
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                boundCameraProvider = cameraProvider
                cameraError = null
                Log.d("QrScanner", "CameraX bound successfully with Preview and ImageAnalysis analyzer")
            } catch (e: Throwable) {
                Log.e("QrScanner", "Failed to start CameraX", e)
                cameraError = "Не вдалося запустити камеру"
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            isDisposed.set(true)
            try {
                boundCameraProvider?.unbindAll()
            } catch (e: Throwable) {
                Log.w("QrScanner", "Error unbinding CameraX", e)
            }
            cameraExecutor.shutdown()
        }
    }

    if (cameraError != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberCardSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cameraError!!,
                color = CyberTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
    } else {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
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
    return try {
        if (imageProxy.format != ImageFormat.YUV_420_888 && imageProxy.format != ImageFormat.NV21) {
            return null
        }

        val yPlane = imageProxy.planes.firstOrNull() ?: return null
        val yBuffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val pixelStride = yPlane.pixelStride
        val width = imageProxy.width
        val height = imageProxy.height
        if (width <= 0 || height <= 0) return null

        val packedY = extractPackedLuminance(yBuffer, width, height, rowStride, pixelStride)
        val rotation = imageProxy.imageInfo.rotationDegrees

        val (rotatedBytes, rotW, rotH) = rotateLuminanceBytes(packedY, width, height, rotation)
        val rotatedSource = PlanarYUVLuminanceSource(
            rotatedBytes,
            rotW,
            rotH,
            0,
            0,
            rotW,
            rotH,
            false
        )

        tryDecodeSource(rotatedSource, reader)?.let { return it }

        if (rotation != 0) {
            val unrotatedSource = PlanarYUVLuminanceSource(
                packedY,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )
            tryDecodeSource(unrotatedSource, reader)?.let { return it }
        }

        null
    } catch (e: Throwable) {
        Log.w("QrScanner", "Error decoding QR frame: ${e.message}")
        null
    }
}

private fun tryDecodeSource(source: LuminanceSource, reader: MultiFormatReader): String? {
    // 1. Standard HybridBinarizer
    try {
        val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
        Log.d("QrScanner", "Successfully decoded QR: ${result.text}")
        return result.text
    } catch (_: NotFoundException) {
    } catch (_: Throwable) {
    } finally {
        reader.reset()
    }

    // 2. GlobalHistogramBinarizer for low-contrast / screen glare frames
    try {
        val result = reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
        Log.d("QrScanner", "Successfully decoded QR (GlobalHistogram): ${result.text}")
        return result.text
    } catch (_: NotFoundException) {
    } catch (_: Throwable) {
    } finally {
        reader.reset()
    }

    // 3. Inverted luminance for light-on-dark QR codes
    try {
        val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source.invert())))
        Log.d("QrScanner", "Successfully decoded QR (Inverted): ${result.text}")
        return result.text
    } catch (_: NotFoundException) {
    } catch (_: Throwable) {
    } finally {
        reader.reset()
    }

    return null
}

internal fun extractPackedLuminance(
    buffer: java.nio.ByteBuffer,
    width: Int,
    height: Int,
    rowStride: Int,
    pixelStride: Int
): ByteArray {
    val dup = buffer.duplicate()
    dup.rewind()
    val limit = dup.limit()
    val output = ByteArray(width * height)

    if (pixelStride == 1 && rowStride == width && limit >= output.size) {
        dup.get(output, 0, output.size)
        return output
    }

    val rowBuffer = ByteArray(rowStride.coerceAtLeast(width))
    for (row in 0 until height) {
        val rowStart = row * rowStride
        if (rowStart >= limit) break
        val bytesAvailable = (limit - rowStart).coerceAtLeast(0)
        val rowDstOffset = row * width

        if (pixelStride == 1) {
            val copyLen = minOf(width, bytesAvailable)
            dup.position(rowStart)
            dup.get(output, rowDstOffset, copyLen)
        } else {
            val neededBytes = (width - 1) * pixelStride + 1
            val readLen = minOf(neededBytes, bytesAvailable)
            dup.position(rowStart)
            dup.get(rowBuffer, 0, readLen)
            for (col in 0 until width) {
                val srcIdx = col * pixelStride
                if (srcIdx < readLen) {
                    output[rowDstOffset + col] = rowBuffer[srcIdx]
                }
            }
        }
    }
    return output
}

internal fun rotateLuminanceBytes(
    data: ByteArray,
    width: Int,
    height: Int,
    rotationDegrees: Int
): Triple<ByteArray, Int, Int> {
    val normalized = ((rotationDegrees % 360) + 360) % 360
    return when (normalized) {
        90 -> {
            val out = ByteArray(width * height)
            for (y in 0 until height) {
                val rowOff = y * width
                val dstX = height - 1 - y
                for (x in 0 until width) {
                    out[x * height + dstX] = data[rowOff + x]
                }
            }
            Triple(out, height, width)
        }
        180 -> {
            val out = ByteArray(width * height)
            val total = width * height
            for (i in 0 until total) {
                out[total - 1 - i] = data[i]
            }
            Triple(out, width, height)
        }
        270 -> {
            val out = ByteArray(width * height)
            for (y in 0 until height) {
                val rowOff = y * width
                for (x in 0 until width) {
                    val dstY = width - 1 - x
                    out[dstY * height + y] = data[rowOff + x]
                }
            }
            Triple(out, height, width)
        }
        else -> Triple(data, width, height)
    }
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
