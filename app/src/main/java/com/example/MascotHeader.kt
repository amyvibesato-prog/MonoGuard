package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.CyberYellowLight

@Composable
fun MascotHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(115.dp)) {
            val w = size.width
            val cx = w / 2f
            // Position head so that the bottom collar aligns neatly with the bottom of this header
            val baseHeadY = size.height - 75f

            // 1. "GOOD APPS BETTER DAYS" slogan + crown graffiti to the right
            drawCrownAndSlogan(Offset(cx + 85f, baseHeadY - 32f))

            // 2. Yellow Golden Collar / Shoulder Plates (behind head)
            val collarLeft = Path().apply {
                moveTo(cx - 30f, baseHeadY + 45f)
                lineTo(cx - 80f, baseHeadY + 62f)
                lineTo(cx - 56f, baseHeadY + 92f)
                lineTo(cx - 15f, baseHeadY + 66f)
                close()
            }
            drawPath(collarLeft, color = CyberYellow)
            drawPath(collarLeft, color = Color(0xFF101214), style = Stroke(width = 3.5f))

            val collarRight = Path().apply {
                moveTo(cx + 30f, baseHeadY + 45f)
                lineTo(cx + 80f, baseHeadY + 62f)
                lineTo(cx + 56f, baseHeadY + 92f)
                lineTo(cx + 15f, baseHeadY + 66f)
                close()
            }
            drawPath(collarRight, color = CyberYellow)
            drawPath(collarRight, color = Color(0xFF101214), style = Stroke(width = 3.5f))

            // Neck center connector
            val neck = Path().apply {
                moveTo(cx - 18f, baseHeadY + 48f)
                lineTo(cx + 18f, baseHeadY + 48f)
                lineTo(cx + 14f, baseHeadY + 70f)
                lineTo(cx - 14f, baseHeadY + 70f)
                close()
            }
            drawPath(neck, color = Color(0xFF14171A))

            // 3. Cat Ears (Outer Dark Charcoal, Inner Golden/Teal)
            val leftEar = Path().apply {
                moveTo(cx - 58f, baseHeadY - 40f)
                lineTo(cx - 48f, baseHeadY - 80f)
                lineTo(cx - 14f, baseHeadY - 40f)
                close()
            }
            drawPath(leftEar, color = Color(0xFF14171A))
            drawPath(leftEar, color = Color(0xFF262C36), style = Stroke(width = 3f))

            val rightEar = Path().apply {
                moveTo(cx + 14f, baseHeadY - 40f)
                lineTo(cx + 48f, baseHeadY - 80f)
                lineTo(cx + 58f, baseHeadY - 40f)
                close()
            }
            drawPath(rightEar, color = Color(0xFF14171A))
            drawPath(rightEar, color = Color(0xFF262C36), style = Stroke(width = 3f))

            // 4. TV Monitor Outer Box
            val headW = 136f
            val headH = 98f
            val headLeft = cx - headW / 2f
            val headTop = baseHeadY - headH / 2f

            drawRoundRect(
                color = Color(0xFF14171A),
                topLeft = Offset(headLeft, headTop),
                size = Size(headW, headH),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Cyber border
            drawRoundRect(
                color = Color(0xFF252B35),
                topLeft = Offset(headLeft, headTop),
                size = Size(headW, headH),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 3.5f)
            )

            // 5. Monitor Inner Screen Bezel
            val screenW = 120f
            val screenH = 82f
            val screenLeft = cx - screenW / 2f
            val screenTop = baseHeadY - screenH / 2f

            drawRoundRect(
                color = Color(0xFF1E222A),
                topLeft = Offset(screenLeft, screenTop),
                size = Size(screenW, screenH),
                cornerRadius = CornerRadius(9f, 9f)
            )

            // Screen glass glare accent
            val glare = Path().apply {
                moveTo(screenLeft + 7f, screenTop + 7f)
                lineTo(screenLeft + 40f, screenTop + 7f)
                lineTo(screenLeft + 7f, screenTop + 40f)
                close()
            }
            drawPath(glare, color = Color.White.copy(alpha = 0.08f))

            // 6. Glowing Yellow Digital Eyes (mischievous squint/arch)
            val leftEye = Path().apply {
                moveTo(cx - 38f, baseHeadY - 12f)
                quadraticTo(cx - 25f, baseHeadY - 21f, cx - 12f, baseHeadY - 11f)
                quadraticTo(cx - 25f, baseHeadY - 16f, cx - 38f, baseHeadY - 12f)
                close()
            }
            drawPath(leftEye, color = CyberYellow)

            val rightEye = Path().apply {
                moveTo(cx + 12f, baseHeadY - 11f)
                quadraticTo(cx + 25f, baseHeadY - 21f, cx + 38f, baseHeadY - 12f)
                quadraticTo(cx + 25f, baseHeadY - 16f, cx + 12f, baseHeadY - 11f)
                close()
            }
            drawPath(rightEye, color = CyberYellow)

            // 7. Huge Mischievous Golden Smile with Teeth Grid
            val smileLeft = cx - 42f
            val smileTop = baseHeadY + 4f
            val smileW = 84f
            val smileH = 30f

            // Grin shape (curved crescent smile)
            val mouthPath = Path().apply {
                moveTo(smileLeft, smileTop + 3f)
                quadraticTo(cx, smileTop - 3f, smileLeft + smileW, smileTop + 3f)
                quadraticTo(cx + smileW * 0.35f, smileTop + smileH + 2f, cx, smileTop + smileH)
                quadraticTo(cx - smileW * 0.35f, smileTop + smileH + 2f, smileLeft, smileTop + 3f)
                close()
            }
            drawPath(mouthPath, color = CyberYellow)

            // Teeth separator bars (dark lines inside the golden smile)
            val toothCount = 7
            for (i in 1..toothCount) {
                val tx = smileLeft + (smileW / (toothCount + 1)) * i
                drawLine(
                    color = Color(0xFF14171A),
                    start = Offset(tx, smileTop + 2f),
                    end = Offset(tx, smileTop + smileH - 2f),
                    strokeWidth = 2.5f
                )
            }
            // Horizontal tooth split line
            val splitLine = Path().apply {
                moveTo(smileLeft + 3f, smileTop + smileH * 0.52f)
                quadraticTo(cx, smileTop + smileH * 0.48f, smileLeft + smileW - 3f, smileTop + smileH * 0.52f)
            }
            drawPath(splitLine, color = Color(0xFF14171A), style = Stroke(width = 2.2f))
        }
    }
}

private fun DrawScope.drawCrownAndSlogan(pos: Offset) {
    // Mini Crown
    val crown = Path().apply {
        moveTo(pos.x - 12f, pos.y - 10f)
        lineTo(pos.x - 14f, pos.y - 20f)
        lineTo(pos.x - 5f, pos.y - 13f)
        lineTo(pos.x, pos.y - 23f)
        lineTo(pos.x + 5f, pos.y - 13f)
        lineTo(pos.x + 14f, pos.y - 20f)
        lineTo(pos.x + 12f, pos.y - 10f)
        close()
    }
    drawPath(crown, color = CyberYellow, style = Stroke(width = 3f))

    // Doodle accent lines radiating from crown
    drawLine(color = CyberYellowLight, start = Offset(pos.x - 10f, pos.y - 25f), end = Offset(pos.x - 15f, pos.y - 30f), strokeWidth = 2f)
    drawLine(color = CyberYellowLight, start = Offset(pos.x, pos.y - 27f), end = Offset(pos.x, pos.y - 33f), strokeWidth = 2f)
    drawLine(color = CyberYellowLight, start = Offset(pos.x + 10f, pos.y - 25f), end = Offset(pos.x + 15f, pos.y - 30f), strokeWidth = 2f)
}
