package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.CyberTealDark
import com.example.ui.theme.CyberYellow

@Composable
fun GraffitiBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Base vibrant teal fill
        drawRect(color = CyberTeal)

        // 2. Diagonal yellow energy speed stripes
        val stripeColor = CyberYellow.copy(alpha = 0.85f)
        val stripeColorThin = CyberYellow.copy(alpha = 0.65f)

        // Stripe 1 (top-right diagonal band)
        val p1 = Path().apply {
            moveTo(w * 0.45f, 0f)
            lineTo(w * 0.65f, 0f)
            lineTo(w, h * 0.28f)
            lineTo(w, h * 0.42f)
            close()
        }
        drawPath(p1, color = stripeColor)

        // Stripe 2 (thin accent line parallel)
        val p2 = Path().apply {
            moveTo(w * 0.35f, 0f)
            lineTo(w * 0.40f, 0f)
            lineTo(w, h * 0.50f)
            lineTo(w, h * 0.46f)
            close()
        }
        drawPath(p2, color = stripeColorThin)

        // Stripe 3 (mid-left through bottom-right diagonal band)
        val p3 = Path().apply {
            moveTo(0f, h * 0.42f)
            lineTo(0f, h * 0.52f)
            lineTo(w * 0.72f, h)
            lineTo(w * 0.58f, h)
            close()
        }
        drawPath(p3, color = stripeColor)

        // Stripe 4 (lower parallel thin stripe)
        val p4 = Path().apply {
            moveTo(0f, h * 0.56f)
            lineTo(0f, h * 0.60f)
            lineTo(w * 0.88f, h)
            lineTo(w * 0.82f, h)
            close()
        }
        drawPath(p4, color = stripeColorThin)

        // 3. White Sparkle Stars (4-point sparkle)
        drawSparkleStar(Offset(w * 0.22f, h * 0.18f), size = 38f)
        drawSparkleStar(Offset(w * 0.92f, h * 0.22f), size = 46f)
        drawSparkleStar(Offset(w * 0.72f, h * 0.83f), size = 42f)
        drawSparkleStar(Offset(w * 0.88f, h * 0.94f), size = 34f)
        drawSparkleStar(Offset(w * 0.08f, h * 0.34f), size = 26f)

        // 4. Street art graffiti doodles (dark charcoal outline)
        val doodleColor = Color(0xFF132A26).copy(alpha = 0.75f)

        // Doodle Lock at top-right
        drawDoodleLock(Offset(w * 0.80f, h * 0.07f), doodleColor)

        // Doodle Star / Cross at top-right
        drawDoodleStar(Offset(w * 0.92f, h * 0.06f), doodleColor)

        // Doodle "YES!" or "!" at right edge
        drawDoodleExclamation(Offset(w * 0.92f, h * 0.16f), doodleColor)

        // Doodle Robot head / cat face at bottom-left
        drawDoodleFace(Offset(w * 0.10f, h * 0.76f), doodleColor)
        drawDoodleCrossEyes(Offset(w * 0.12f, h * 0.91f), doodleColor)

        // Doodle tag / question at bottom-right
        drawDoodleQuestion(Offset(w * 0.90f, h * 0.74f), doodleColor)
    }
}

private fun DrawScope.drawSparkleStar(center: Offset, size: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, color = Color.White)
}

private fun DrawScope.drawDoodleLock(pos: Offset, color: Color) {
    // Shackle
    val shackle = Path().apply {
        moveTo(pos.x - 14f, pos.y)
        cubicTo(pos.x - 14f, pos.y - 18f, pos.x + 14f, pos.y - 18f, pos.x + 14f, pos.y)
    }
    drawPath(shackle, color = color, style = Stroke(width = 4.5f))
    // Body
    drawRoundRect(
        color = color,
        topLeft = Offset(pos.x - 20f, pos.y),
        size = androidx.compose.ui.geometry.Size(40f, 32f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 4.5f)
    )
    // Keyhole
    drawCircle(color = color, radius = 3.5f, center = Offset(pos.x, pos.y + 12f))
    drawLine(color = color, start = Offset(pos.x, pos.y + 12f), end = Offset(pos.x, pos.y + 22f), strokeWidth = 3.5f)
}

private fun DrawScope.drawDoodleStar(pos: Offset, color: Color) {
    val p = Path().apply {
        moveTo(pos.x, pos.y - 14f)
        lineTo(pos.x + 4f, pos.y - 4f)
        lineTo(pos.x + 14f, pos.y)
        lineTo(pos.x + 4f, pos.y + 4f)
        lineTo(pos.x, pos.y + 14f)
        lineTo(pos.x - 4f, pos.y + 4f)
        lineTo(pos.x - 14f, pos.y)
        lineTo(pos.x - 4f, pos.y - 4f)
        close()
    }
    drawPath(p, color = color, style = Stroke(width = 4f))
}

private fun DrawScope.drawDoodleExclamation(pos: Offset, color: Color) {
    drawLine(color = color, start = Offset(pos.x - 6f, pos.y - 18f), end = Offset(pos.x + 6f, pos.y + 8f), strokeWidth = 4.5f)
    drawLine(color = color, start = Offset(pos.x + 8f, pos.y - 14f), end = Offset(pos.x + 16f, pos.y + 12f), strokeWidth = 4.5f)
    drawCircle(color = color, radius = 3f, center = Offset(pos.x + 10f, pos.y + 20f))
}

private fun DrawScope.drawDoodleFace(pos: Offset, color: Color) {
    // TV head doodle
    drawRoundRect(
        color = color,
        topLeft = Offset(pos.x - 24f, pos.y - 18f),
        size = androidx.compose.ui.geometry.Size(48f, 36f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 4f)
    )
    // Cat ears
    val leftEar = Path().apply {
        moveTo(pos.x - 20f, pos.y - 18f)
        lineTo(pos.x - 16f, pos.y - 30f)
        lineTo(pos.x - 6f, pos.y - 18f)
    }
    drawPath(leftEar, color = color, style = Stroke(width = 4f))
    val rightEar = Path().apply {
        moveTo(pos.x + 6f, pos.y - 18f)
        lineTo(pos.x + 16f, pos.y - 30f)
        lineTo(pos.x + 20f, pos.y - 18f)
    }
    drawPath(rightEar, color = color, style = Stroke(width = 4f))

    // Grin
    val grin = Path().apply {
        moveTo(pos.x - 14f, pos.y + 4f)
        quadraticTo(pos.x, pos.y + 12f, pos.x + 14f, pos.y + 4f)
    }
    drawPath(grin, color = color, style = Stroke(width = 3.5f))
}

private fun DrawScope.drawDoodleCrossEyes(pos: Offset, color: Color) {
    drawCircle(color = color, radius = 22f, center = pos, style = Stroke(width = 4f))
    // X eyes
    drawLine(color = color, start = Offset(pos.x - 12f, pos.y - 8f), end = Offset(pos.x - 4f, pos.y), strokeWidth = 3.5f)
    drawLine(color = color, start = Offset(pos.x - 4f, pos.y - 8f), end = Offset(pos.x - 12f, pos.y), strokeWidth = 3.5f)
    drawLine(color = color, start = Offset(pos.x + 4f, pos.y - 8f), end = Offset(pos.x + 12f, pos.y), strokeWidth = 3.5f)
    drawLine(color = color, start = Offset(pos.x + 12f, pos.y - 8f), end = Offset(pos.x + 4f, pos.y), strokeWidth = 3.5f)
    // Smile
    val smile = Path().apply {
        moveTo(pos.x - 10f, pos.y + 6f)
        quadraticTo(pos.x, pos.y + 16f, pos.x + 10f, pos.y + 6f)
    }
    drawPath(smile, color = color, style = Stroke(width = 3.5f))
}

private fun DrawScope.drawDoodleQuestion(pos: Offset, color: Color) {
    val q = Path().apply {
        moveTo(pos.x - 8f, pos.y - 12f)
        cubicTo(pos.x - 8f, pos.y - 20f, pos.x + 8f, pos.y - 20f, pos.x + 8f, pos.y - 10f)
        cubicTo(pos.x + 8f, pos.y - 4f, pos.x, pos.y - 2f, pos.x, pos.y + 6f)
    }
    drawPath(q, color = color, style = Stroke(width = 4f))
    drawCircle(color = color, radius = 2.5f, center = Offset(pos.x, pos.y + 14f))
}
