package com.qodein.core.designsystem.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class CouponShape(private val cornerRadius: Float, private val cutoutRadius: Float, private val stubWidthPx: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            val perforationX = width - stubWidthPx

            // Start from top-left
            moveTo(0f, cornerRadius)

            // Top-left corner
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerRadius, 2 * cornerRadius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Top edge to cutout start
            lineTo(perforationX - cutoutRadius, 0f)

            // TOP circular cutout (using cubicTo for precise control)
            cubicTo(
                x1 = perforationX - cutoutRadius / 2,
                y1 = cutoutRadius,
                x2 = perforationX + cutoutRadius / 2,
                y2 = cutoutRadius,
                x3 = perforationX + cutoutRadius,
                y3 = 0f,
            )

            // Continue top edge
            lineTo(width - cornerRadius, 0f)

            // Top-right corner
            arcTo(
                rect = Rect(width - 2 * cornerRadius, 0f, width, 2 * cornerRadius),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Right edge
            lineTo(width, height - cornerRadius)

            // Bottom-right corner
            arcTo(
                rect = Rect(width - 2 * cornerRadius, height - 2 * cornerRadius, width, height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Bottom edge to cutout start
            lineTo(perforationX + cutoutRadius, height)

            // BOTTOM circular cutout (using cubicTo for precise control)
            cubicTo(
                x1 = perforationX + cutoutRadius / 2,
                y1 = height - cutoutRadius,
                x2 = perforationX - cutoutRadius / 2,
                y2 = height - cutoutRadius,
                x3 = perforationX - cutoutRadius,
                y3 = height,
            )

            // Continue bottom edge
            lineTo(cornerRadius, height)

            // Bottom-left corner
            arcTo(
                rect = Rect(0f, height - 2 * cornerRadius, 2 * cornerRadius, height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Left edge back to start
            lineTo(0f, cornerRadius)

            close()
        }

        return Outline.Generic(path)
    }
}
