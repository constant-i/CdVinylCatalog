package com.kai.cdvinylcatalog.feature.add.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kai.cdvinylcatalog.common.ui.component.CdTopBar
import com.kai.cdvinylcatalog.feature.add.R

@Composable
fun ScanScreen(
    onIntent: (ScanContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraPermissionHandler(
        onPermissionGranted = {
            ScanContent(
                onNavigateBack = onNavigateBack,
                onNavigateToCollection = onNavigateToCollection,
                onNavigateToSearch = onNavigateToSearch,
                modifier = modifier,
            ) {
                CameraPreview(
                    onBarcodeDetected = { barcode ->
                        onIntent(ScanContract.Intent.OnBarcodeScanned(barcode))
                    }
                )
                ScannerOverlay(modifier = Modifier.fillMaxSize())
            }
        },
        onPermissionDenied = {
            ScanContent(
                onNavigateBack = onNavigateBack,
                onNavigateToCollection = onNavigateToCollection,
                onNavigateToSearch = onNavigateToSearch,
                modifier = modifier,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.scan_permission_denied),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    )
}

/**
 * Статичная часть экрана сканера: фон, топ-бар и слот под содержимое.
 *
 * Содержимое вынесено в слот [content], потому что на устройстве это живая
 * камера (`PreviewView` внутри `AndroidView`), а в [ScanScreenPreview]
 * её нечем отрисовать.
 */
@Composable
private fun ScanContent(
    onNavigateBack: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        content()

        CdTopBar(
            title = stringResource(R.string.scan_title),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            onBack = onNavigateBack,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            actions = {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_search)
                    )
                }
                IconButton(onClick = onNavigateToCollection) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = stringResource(R.string.cd_collection)
                    )
                }
            }
        )
    }
}

/**
 * Экран сканера принудительно тёмный, поэтому превью одно,
 * а не пара Light/Dark, как на остальных экранах.
 */
@Preview(name = "Scanner", showBackground = false, widthDp = 360, heightDp = 720)
@Composable
private fun ScanScreenPreview() {
    ScanContent(
        onNavigateBack = {},
        onNavigateToCollection = {},
        onNavigateToSearch = {},
    ) {
        // Заглушка вместо камеры: в превью CameraX недоступен
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2B2B2B))
        )
        ScannerOverlay(modifier = Modifier.fillMaxSize())
    }
}
