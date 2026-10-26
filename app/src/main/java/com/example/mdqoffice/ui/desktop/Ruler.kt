package com.example.mdqoffice.ui.desktop

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.mdqoffice.model.DocParagraph
import com.example.mdqoffice.model.MdqDocument
import com.example.mdqoffice.theme.OfficeBluePrimary
import com.example.mdqoffice.theme.RulerBackground
import com.example.mdqoffice.theme.RulerBorder

@Composable
fun DesktopHorizontalRuler(
    document: MdqDocument,
    selectedParagraph: DocParagraph?,
    onSetLeftMargin: (Float) -> Unit,
    onSetRightMargin: (Float) -> Unit,
    onSetFirstLineIndent: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalWidthCm = document.paperSize.widthCm
    val leftMarginCm = document.margins.leftCm
    val rightMarginCm = document.margins.rightCm
    val firstLineIndentCm = selectedParagraph?.indentFirstLineCm ?: 0f

    val numberPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            textSize = 20f
            color = android.graphics.Color.rgb(105, 110, 118)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = AndroidPaint.Align.CENTER
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(RulerBackground)
            .border(width = 0.5.dp, color = RulerBorder)
            .testTag("horizontal_ruler")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalWidthCm) {
                    detectTapGestures { offset ->
                        val clickedRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        val clickedCm = clickedRatio * totalWidthCm

                        if (clickedCm < totalWidthCm / 2) {
                            if (offset.y < size.height / 2) {
                                val newIndent = (clickedCm - leftMarginCm).coerceIn(0f, 5f)
                                onSetFirstLineIndent(newIndent)
                            } else {
                                val newMargin = clickedCm.coerceIn(0.8f, 5f)
                                onSetLeftMargin(newMargin)
                            }
                        } else {
                            val newRightMargin = (totalWidthCm - clickedCm).coerceIn(0.8f, 5f)
                            onSetRightMargin(newRightMargin)
                        }
                    }
                }
                .pointerInput(totalWidthCm) {
                    detectDragGestures { change, _ ->
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        val posCm = ratio * totalWidthCm
                        if (posCm < totalWidthCm / 2) {
                            val newMargin = posCm.coerceIn(0.8f, 5f)
                            onSetLeftMargin(newMargin)
                        } else {
                            val newRight = (totalWidthCm - posCm).coerceIn(0.8f, 5f)
                            onSetRightMargin(newRight)
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            val leftMarginPx = (leftMarginCm / totalWidthCm) * canvasW
            val rightMarginPx = ((totalWidthCm - rightMarginCm) / totalWidthCm) * canvasW

            // 1. Shaded non-printable area (Left Margin)
            drawRect(
                color = Color(0xFFE5E9EE),
                topLeft = Offset(0f, 0f),
                size = Size(leftMarginPx, canvasH)
            )

            // 2. White active printable area
            drawRect(
                color = Color.White,
                topLeft = Offset(leftMarginPx, 0f),
                size = Size(rightMarginPx - leftMarginPx, canvasH)
            )

            // 3. Shaded non-printable area (Right Margin)
            drawRect(
                color = Color(0xFFE5E9EE),
                topLeft = Offset(rightMarginPx, 0f),
                size = Size(canvasW - rightMarginPx, canvasH)
            )

            // 4. Centimeter tick marks and numbers
            val maxCm = totalWidthCm.toInt()
            for (cm in 1..maxCm) {
                val tickX = (cm.toFloat() / totalWidthCm) * canvasW

                // Centimeter tick
                drawLine(
                    color = Color(0xFF8C929A),
                    start = Offset(tickX, canvasH - 5f),
                    end = Offset(tickX, canvasH),
                    strokeWidth = 1f
                )

                // Half-centimeter tick
                val halfX = ((cm - 0.5f) / totalWidthCm) * canvasW
                drawLine(
                    color = Color(0xFFBAC0C8),
                    start = Offset(halfX, canvasH - 3f),
                    end = Offset(halfX, canvasH),
                    strokeWidth = 0.8f
                )

                // Draw centimeter number (every 1 cm if space permits, or every 2 cm)
                if (cm % 2 == 0 || canvasW > 600f) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "$cm",
                        tickX,
                        canvasH - 7f,
                        numberPaint
                    )
                }
            }

            // 5. Left Margin Marker
            drawUpTriangle(
                center = Offset(leftMarginPx, canvasH),
                width = 6f,
                height = 5f,
                color = OfficeBluePrimary
            )

            // 6. First Line Indent Marker
            val firstLineIndentPx = leftMarginPx + ((firstLineIndentCm / totalWidthCm) * canvasW)
            drawDownTriangle(
                center = Offset(firstLineIndentPx, 0f),
                width = 6f,
                height = 5f,
                color = Color(0xFF0C2E63)
            )

            // 7. Right Margin Marker
            drawUpTriangle(
                center = Offset(rightMarginPx, canvasH),
                width = 6f,
                height = 5f,
                color = OfficeBluePrimary
            )
        }
    }
}

@Composable
fun DesktopVerticalRuler(
    document: MdqDocument,
    onSetTopMargin: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHeightCm = document.paperSize.heightCm
    val topMarginCm = document.margins.topCm
    val bottomMarginCm = document.margins.bottomCm

    val vertNumberPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            textSize = 17f
            color = android.graphics.Color.rgb(105, 110, 118)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = AndroidPaint.Align.CENTER
        }
    }

    Canvas(
        modifier = modifier
            .width(14.dp)
            .fillMaxHeight()
            .background(RulerBackground)
            .border(width = 0.5.dp, color = RulerBorder)
            .pointerInput(totalHeightCm) {
                detectTapGestures { offset ->
                    val clickedRatio = (offset.y / size.height).coerceIn(0f, 1f)
                    val clickedCm = clickedRatio * totalHeightCm
                    if (clickedCm < totalHeightCm / 2) {
                        onSetTopMargin(clickedCm.coerceIn(0.8f, 6f))
                    }
                }
            }
            .testTag("vertical_ruler")
    ) {
        val canvasW = size.width
        val canvasH = size.height

        val topMarginPx = (topMarginCm / totalHeightCm) * canvasH
        val bottomMarginPx = ((totalHeightCm - bottomMarginCm) / totalHeightCm) * canvasH

        // Top margin shaded
        drawRect(
            color = Color(0xFFE5E9EE),
            topLeft = Offset(0f, 0f),
            size = Size(canvasW, topMarginPx)
        )

        // White printable vertical area
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, topMarginPx),
            size = Size(canvasW, bottomMarginPx - topMarginPx)
        )

        // Bottom margin shaded
        drawRect(
            color = Color(0xFFE5E9EE),
            topLeft = Offset(0f, bottomMarginPx),
            size = Size(canvasW, canvasH - bottomMarginPx)
        )

        // Centimeter tick marks & numbers
        val maxCm = totalHeightCm.toInt()
        for (cm in 1..maxCm) {
            val tickY = (cm.toFloat() / totalHeightCm) * canvasH
            drawLine(
                color = Color(0xFF8C929A),
                start = Offset(canvasW - 4f, tickY),
                end = Offset(canvasW, tickY),
                strokeWidth = 0.8f
            )

            if (cm % 2 == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "$cm",
                    canvasW / 2f - 2f,
                    tickY + 4f,
                    vertNumberPaint
                )
            }
        }
    }
}

private fun DrawScope.drawUpTriangle(center: Offset, width: Float, height: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - height)
        lineTo(center.x - width / 2f, center.y)
        lineTo(center.x + width / 2f, center.y)
        close()
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawDownTriangle(center: Offset, width: Float, height: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - width / 2f, center.y)
        lineTo(center.x + width / 2f, center.y)
        lineTo(center.x, center.y + height)
        close()
    }
    drawPath(path = path, color = color)
}
