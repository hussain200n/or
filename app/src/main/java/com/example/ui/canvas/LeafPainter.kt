package com.example.ui.canvas

import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.model.Leaf
import kotlin.math.min

object LeafPainter {

    /**
     * Draws a leaf centered at the local origin (0, 0) of the given DrawScope.
     * Absolute placement on screen is handled by the caller (LeafComponent positions
     * the enclosing composable at the leaf's model-space coordinates); this painter
     * only applies rotation and scale, relative to that local origin.
     */
    fun drawLeaf(
        drawScope: DrawScope,
        leaf: Leaf,
        isSelected: Boolean
    ) {
        drawScope.apply {
            withTransform({
                rotate(leaf.angle)
                scale(leaf.scale, leaf.scale)
            }) {
                val halfLen = 85f
                val halfWidth = 42f
                val sharpness = leaf.tipSharpness.coerceIn(0.5f, 2.5f)

                val leafPath = Path().apply {
                    moveTo(-halfLen, 0f)
                    cubicTo(
                        -halfLen * 0.4f, -halfWidth * 1.3f,
                        halfLen * (0.8f - 0.3f * sharpness), -halfWidth * 1.3f / (0.5f + 0.5f * sharpness),
                        halfLen, 0f
                    )
                    cubicTo(
                        halfLen * (0.8f - 0.3f * sharpness), halfWidth * 1.3f / (0.5f + 0.5f * sharpness),
                        -halfLen * 0.4f, halfWidth * 1.3f,
                        -halfLen, 0f
                    )
                    close()
                }

                val shadowPath = Path().apply {
                    moveTo(-halfLen + 4f, 5f)
                    cubicTo(
                        -halfLen * 0.4f + 4f, -halfWidth * 1.3f + 5f,
                        halfLen * (0.8f - 0.3f * sharpness) + 4f, -halfWidth * 1.3f / (0.5f + 0.5f * sharpness) + 5f,
                        halfLen + 4f, 5f
                    )
                    cubicTo(
                        halfLen * (0.8f - 0.3f * sharpness) + 4f, halfWidth * 1.3f / (0.5f + 0.5f * sharpness) + 5f,
                        -halfLen * 0.4f + 4f, halfWidth * 1.3f + 5f,
                        -halfLen + 4f, 5f
                    )
                    close()
                }
                drawPath(path = shadowPath, color = Color.Black.copy(alpha = 0.2f))

                val baseSelectedColor = try {
                    Color(android.graphics.Color.parseColor(leaf.gradientColorHex))
                } catch (e: Exception) {
                    Color(0xFF388E3C)
                }

                val darkShadowColor = Color(
                    red = (baseSelectedColor.red * 0.65f).coerceIn(0f, 1f),
                    green = (baseSelectedColor.green * 0.65f).coerceIn(0f, 1f),
                    blue = (baseSelectedColor.blue * 0.65f).coerceIn(0f, 1f),
                    alpha = 1f
                )
                val lightHighlightColor = Color(
                    red = (baseSelectedColor.red * 1.25f).coerceIn(0f, 1f),
                    green = (baseSelectedColor.green * 1.25f).coerceIn(0f, 1f),
                    blue = (baseSelectedColor.blue * 1.1f).coerceIn(0f, 1f),
                    alpha = 1f
                )

                val leafGradient = Brush.linearGradient(
                    colors = listOf(darkShadowColor, baseSelectedColor, lightHighlightColor),
                    start = Offset(-halfLen, 0f),
                    end = Offset(halfLen, 0f)
                )
                drawPath(path = leafPath, brush = leafGradient)

                val intensity = leaf.veinIntensity.coerceIn(0.1f, 3.0f)
                val mainVeinColor = Color(0xFFC5E1A5).copy(alpha = (0.6f * intensity).coerceIn(0.1f, 1.0f))
                val secondaryVeinColor = Color(0xFFC5E1A5).copy(alpha = (0.4f * intensity).coerceIn(0.05f, 0.9f))

                drawLine(
                    color = mainVeinColor,
                    start = Offset(-halfLen, 0f),
                    end = Offset(halfLen - 5f, 0f),
                    strokeWidth = 2.5f * intensity,
                    cap = StrokeCap.Round
                )

                val veinStroke = 1.3f * intensity
                drawLine(secondaryVeinColor, Offset(-halfLen * 0.5f, 0f), Offset(-halfLen * 0.3f, -halfWidth * 0.5f), strokeWidth = veinStroke)
                drawLine(secondaryVeinColor, Offset(-halfLen * 0.1f, 0f), Offset(halfLen * 0.1f, -halfWidth * 0.7f), strokeWidth = veinStroke)
                drawLine(secondaryVeinColor, Offset(halfLen * 0.3f, 0f), Offset(halfLen * 0.5f, -halfWidth * 0.5f), strokeWidth = veinStroke)
                drawLine(secondaryVeinColor, Offset(-halfLen * 0.5f, 0f), Offset(-halfLen * 0.3f, halfWidth * 0.5f), strokeWidth = veinStroke)
                drawLine(secondaryVeinColor, Offset(-halfLen * 0.1f, 0f), Offset(halfLen * 0.1f, halfWidth * 0.7f), strokeWidth = veinStroke)
                drawLine(secondaryVeinColor, Offset(halfLen * 0.3f, 0f), Offset(halfLen * 0.5f, halfWidth * 0.5f), strokeWidth = veinStroke)

                val leafOutlineColor = if (isSelected) Color(0xFFD4AF37) else Color(0xFF154020)
                val leafOutlineWidth = if (isSelected) 3.5f else 2f
                drawPath(
                    path = leafPath,
                    color = leafOutlineColor,
                    style = Stroke(width = leafOutlineWidth, cap = StrokeCap.Round)
                )

                drawLine(
                    color = Color(0xFF5D3215),
                    start = Offset(-halfLen - 12f, 0f),
                    end = Offset(-halfLen + 2f, 0f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )

                drawIntoCanvas { canvas ->
                    val customTypeface = when (leaf.fontFamily) {
                        "Serif" -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
                        "SansSerif" -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        "Monospace" -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        else -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    val namePaint = AndroidPaint().apply {
                        color = AndroidColor.parseColor("#FFFFFF")
                        textSize = leaf.fontSize
                        textAlign = AndroidPaint.Align.CENTER
                        isAntiAlias = true
                        typeface = customTypeface
                        setShadowLayer(4f, 1f, 1f, AndroidColor.parseColor("#CC000000"))
                    }

                    val nameText = leaf.name
                    if (nameText.length > 7) {
                        namePaint.textSize = min(leaf.fontSize, leaf.fontSize * (7f / nameText.length))
                    }

                    val textHeightOffset = (namePaint.descent() + namePaint.ascent()) / 2f
                    canvas.nativeCanvas.drawText(nameText, 0f, -textHeightOffset, namePaint)
                }
            }
        }
    }
}
