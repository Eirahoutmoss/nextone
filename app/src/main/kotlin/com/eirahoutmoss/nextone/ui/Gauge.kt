package com.eirahoutmoss.nextone.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Yatay ibre: −50…+50 cent. Yeşil bölge ±[zoneCents]. İbre [deviation] null ise ortada,
 * soluk durur. Bölge dışında ibre turuncu, içinde yeşil.
 */
@Composable
fun Gauge(deviation: Double?, zoneCents: Double, modifier: Modifier = Modifier) {
    val target = (deviation ?: 0.0).coerceIn(-50.0, 50.0).toFloat()
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "ibre",
    )
    val scheme = MaterialTheme.colorScheme
    val inZone = deviation != null && kotlin.math.abs(deviation) <= zoneCents

    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val pxPerCent = (w * 0.92f) / 100f
        val baseY = h * 0.62f

        // Yeşil bölge
        val zw = (zoneCents * pxPerCent).toFloat()
        drawRoundRect(
            color = NexColors.InTune.copy(alpha = if (inZone) 0.35f else 0.16f),
            topLeft = Offset(cx - zw, baseY - h * 0.42f),
            size = Size(2 * zw, h * 0.52f),
            cornerRadius = CornerRadius(6f, 6f),
        )

        // Ölçek çizgileri: her 5 cent kısa, her 10 cent uzun
        for (c in -50..50 step 5) {
            val x = cx + c * pxPerCent
            val long = c % 10 == 0
            val len = if (c == 0) h * 0.40f else if (long) h * 0.22f else h * 0.12f
            drawLine(
                color = scheme.onSurfaceVariant.copy(alpha = if (c == 0) 0.9f else 0.45f),
                start = Offset(x, baseY),
                end = Offset(x, baseY - len),
                strokeWidth = if (c == 0) 4f else 2f,
            )
        }

        // İbre
        val nx = cx + animated * pxPerCent
        val needleColor = when {
            deviation == null -> scheme.onSurfaceVariant.copy(alpha = 0.3f)
            inZone -> NexColors.InTune
            else -> NexColors.Sharp
        }
        drawLine(
            color = needleColor,
            start = Offset(nx, baseY + h * 0.12f),
            end = Offset(nx, baseY - h * 0.50f),
            strokeWidth = 10f,
            cap = StrokeCap.Round,
        )
        drawCircle(color = needleColor, radius = 12f, center = Offset(nx, baseY + h * 0.12f))
    }
}
