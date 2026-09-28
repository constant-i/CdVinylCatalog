package com.kai.cdvinylcatalog.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.core.model.DiscogsError

/**
 * "Глупый" UI — только рисует то, что приходит в state.
 * Никакой логики здесь нет.
 */
@Composable
fun ScanScreen(
    state: ScanContract.State,
    onIntent: (ScanContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }
            state.error != null -> {
                ErrorContent(
                    error = state.error,
                    onDismiss = { onIntent(ScanContract.Intent.OnErrorDismissed) }
                )
            }
            state.foundRelease != null -> {
                ReleaseContent(
                    state = state,
                    onAddToCollection = { onIntent(ScanContract.Intent.OnAddToCollectionClicked) },
                    onScanAgain = { onIntent(ScanContract.Intent.OnScanAgainClicked) }
                )
            }
            else -> {
                ScannerPlaceholder(
                    onBarcodeScanned = { barcode ->
                        onIntent(ScanContract.Intent.OnBarcodeScanned(barcode))
                    }
                )
            }
        }
    }
}

@Composable
private fun ScannerPlaceholder(onBarcodeScanned: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Наведите камеру на штрихкод",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Временная кнопка для теста — потом заменим на CameraX
        Button(onClick = { onBarcodeScanned("720642442524") }) {
            Text("Симулировать сканирование")
        }
    }
}

@Composable
private fun ReleaseContent(
    state: ScanContract.State,
    onAddToCollection: () -> Unit,
    onScanAgain: () -> Unit
) {
    val release = state.foundRelease ?: return
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = release.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = release.artist, style = MaterialTheme.typography.titleMedium)
                release.year?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Год: $it", style = MaterialTheme.typography.bodyMedium)
                }
                release.rawFormat?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Формат: $it", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isAlreadyInCollection) {
            Text(
                text = "Этот диск уже в коллекции!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = onAddToCollection) {
            Text("Добавить в коллекцию")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onScanAgain) {
            Text("Сканировать ещё")
        }
    }
}

@Composable
private fun ErrorContent(
    error: DiscogsError,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error.toHumanReadable(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onDismiss) {
            Text("Понятно")
        }
    }
}

/**
 * Превращает доменную ошибку в текст для пользователя.
 */
private fun DiscogsError.toHumanReadable(): String = when (this) {
    DiscogsError.NoInternet -> "Нет подключения к интернету"
    DiscogsError.Timeout -> "Сервер не отвечает, попробуйте позже"
    DiscogsError.Unauthorized -> "Проблема с токеном Discogs"
    DiscogsError.RateLimited -> "Слишком много запросов, подождите минуту"
    DiscogsError.NotFound -> "Релиз не найден"
    is DiscogsError.ServerError -> "Ошибка сервера: $code"
    is DiscogsError.Unknown -> "Что-то пошло не так: ${message ?: "неизвестная ошибка"}"
}