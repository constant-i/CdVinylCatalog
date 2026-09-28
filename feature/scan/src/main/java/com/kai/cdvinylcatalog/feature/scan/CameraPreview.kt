package com.kai.cdvinylcatalog.feature.scan

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Создаем BarcodeScanner
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    // 2. Создаем и запоминаем CameraController
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            // Привязываем контроллер к жизненному циклу
            bindToLifecycle(lifecycleOwner)
            // Выбираем заднюю камеру
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    // 3. Настраиваем анализатор для контроллера
    LaunchedEffect(cameraController, barcodeScanner) {
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                ContextCompat.getMainExecutor(context)
            ) { result ->
                val barcodes = result.getValue(barcodeScanner)
                barcodes?.firstOrNull()?.rawValue?.let { onBarcodeDetected(it) }
            }
        )
    }

    // 4. Отображаем PreviewView и связываем его с контроллером
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                // Ключевая строка: связываем PreviewView с контроллером
                controller = cameraController
            }
        }
    )
}