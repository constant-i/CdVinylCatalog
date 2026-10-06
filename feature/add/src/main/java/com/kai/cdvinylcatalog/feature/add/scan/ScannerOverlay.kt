package com.kai.cdvinylcatalog.feature.add.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Overlay для сканера: затемнение вокруг окна сканирования + угловые маркеры.
 */
@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val windowSize = size.minDimension * 0.72f
            val left = (size.width - windowSize) / 2f
            val top = (size.height - windowSize) / 2f
            val right = left + windowSize
            val bottom = top + windowSize

            val overlayColor = Color.Black.copy(alpha = 0.6f)

            // Затемнение: 4 полосы вокруг окна
            drawRect(overlayColor, size = Size(size.width, top))                          // сверху
            drawRect(overlayColor, topLeft = Offset(0f, bottom), size = Size(size.width, size.height - bottom)) // снизу
            drawRect(overlayColor, topLeft = Offset(0f, top), size = Size(left, windowSize))                    // слева
            drawRect(overlayColor, topLeft = Offset(right, top), size = Size(size.width - right, windowSize))   // справа

            // Угловые маркеры
            val cornerLength = 48.dp.toPx()
            val strokeWidth = 5.dp.toPx()
            val cornerColor = Color.White

            // Верхний левый
            drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)
            // Верхний правый
            drawLine(cornerColor, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
            drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)
            // Нижний левый
            drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
            drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)
            // Нижний правый
            drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
            drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)
        }
    }
}