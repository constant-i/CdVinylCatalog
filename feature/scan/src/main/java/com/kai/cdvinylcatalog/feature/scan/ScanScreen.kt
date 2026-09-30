package com.kai.cdvinylcatalog.feature.scan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.core.model.DiscogsError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    state: ScanContract.State,
    onIntent: (ScanContract.Intent) -> Unit,
    onNavigateToCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Сканирование") },
                actions = {
                    // Кнопка "Моя коллекция"
                    TextButton(onClick = onNavigateToCollection) {
                        Text("Моя коллекция")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorContent(state.error) {
                        onIntent(ScanContract.Intent.OnErrorDismissed)
                    }
                }
            }
            state.foundRelease != null -> {
                ReleaseContent(
                    state = state,
                    onAddToCollection = { onIntent(ScanContract.Intent.OnAddToCollectionClicked) },
                    onScanAgain = { onIntent(ScanContract.Intent.OnScanAgainClicked) },
                    onNotesChanged = { notes -> onIntent(ScanContract.Intent.OnNotesChanged(notes)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                )
            }
            else -> {
                CameraPermissionHandler(
                    onPermissionGranted = {
                        CameraPreview(
                            onBarcodeDetected = { barcode ->
                                onIntent(ScanContract.Intent.OnBarcodeScanned(barcode))
                            }
                        )
                    },
                    onPermissionDenied = {
                        Text("Нет доступа к камере. Разрешите в настройках.")
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReleaseContent(
    state: ScanContract.State,
    onAddToCollection: () -> Unit,
    onScanAgain: () -> Unit,
    onNotesChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val release = state.foundRelease ?: return
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Обложка
        ReleaseCoverImage(coverUrl = release.coverImageUrl)
        Spacer(modifier = Modifier.height(8.dp))

        // Карточка с данными
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

        // Статус
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.justAdded -> {
                    Text(
                        text = "Добавлено: ${release.title}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                state.isAlreadyInCollection -> {
                    Text(
                        text = "Этот диск уже в коллекции!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onAddToCollection()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить в коллекцию")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onScanAgain()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сканировать ещё")
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.userNotes,
            onValueChange = onNotesChanged,
            label = { Text("Заметка (необязательно)") },
            placeholder = { Text("Например: подарок, подписана") },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ReleaseCoverImage(
    coverUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp
) {
    SubcomposeAsyncImage(
        model = coverUrl,
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Нет обложки",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
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