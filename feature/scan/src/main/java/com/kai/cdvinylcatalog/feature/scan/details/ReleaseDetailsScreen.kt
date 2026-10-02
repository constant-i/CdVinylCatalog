package com.kai.cdvinylcatalog.feature.scan.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.model.Track
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReleaseDetailsScreen(
    state: ReleaseDetailsContract.State,
    onIntent: (ReleaseDetailsContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.release?.title ?: "Детали") },
                navigationIcon = {
                    IconButton(onClick = {
                        onIntent(ReleaseDetailsContract.Intent.OnBackClicked)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            state.release != null -> {
                ReleaseContent(
                    state = state,
                    release = state.release,
                    onIntent = onIntent,
                    onKeyboardDismiss = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    bringIntoViewRequester = bringIntoViewRequester,
                    coroutineScope = coroutineScope,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                )
            }
        }
    }

    // Диалог выбора формата
    if (state.showFormatPicker) {
        FormatPickerDialog(
            selectedFormat = state.selectedFormat,
            onFormatSelected = { onIntent(ReleaseDetailsContract.Intent.OnFormatSelected(it)) },
            onConfirm = { onIntent(ReleaseDetailsContract.Intent.OnConfirmAdd) },
            onDismiss = { onIntent(ReleaseDetailsContract.Intent.OnFormatPickerDismissed) }
        )
    }

    // Диалог "добавить ещё копию?"
    if (state.askAddAnother && state.release != null) {
        AlertDialog(
            onDismissRequest = {
                onIntent(ReleaseDetailsContract.Intent.OnAddAnotherDismissed)
            },
            title = { Text("Уже в коллекции") },
            text = {
                Text("«${state.release.title}» уже есть в вашей коллекции. Добавить ещё одну копию?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onIntent(ReleaseDetailsContract.Intent.OnAddAnotherConfirmed)
                }) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onIntent(ReleaseDetailsContract.Intent.OnAddAnotherDismissed)
                }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReleaseContent(
    state: ReleaseDetailsContract.State,
    release: Release,
    onIntent: (ReleaseDetailsContract.Intent) -> Unit,
    onKeyboardDismiss: () -> Unit,
    bringIntoViewRequester: BringIntoViewRequester,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Галерея (перелистывание)
        val images = if (release.imageUrls.isNotEmpty()) {
            release.imageUrls
        } else {
            listOfNotNull(release.coverImageUrl)
        }
        CoverImageGallery(images = images)
        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок
        Text(
            text = release.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = release.artist,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Метаданные
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                MetadataRow("Год", release.year?.toString())
                MetadataRow("Формат", release.rawFormat)
                MetadataRow("Лейбл", release.label)
                MetadataRow("Страна", release.country)
                MetadataRow("Catalog#", release.catalogNumber)
            }
        }

        // Треклист
        if (release.tracklist.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Треклист",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    release.tracklist.forEach { track ->
                        TrackRow(track)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Статус
        if (state.justAdded) {
            Text(
                text = "Добавлено в коллекцию",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else if (state.isInCollection) {
            Text(
                text = "Этот диск уже в коллекции!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Заметка
        OutlinedTextField(
            value = state.userNotes,
            onValueChange = { onIntent(ReleaseDetailsContract.Intent.OnNotesChanged(it)) },
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
        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки
        Button(
            onClick = {
                onKeyboardDismiss()
                onIntent(ReleaseDetailsContract.Intent.OnAddClicked)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isInCollection) "Добавить ещё копию" else "Добавить в коллекцию")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = {
                onKeyboardDismiss()
                onIntent(ReleaseDetailsContract.Intent.OnBackClicked)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CoverImageGallery(images: List<String>) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Нет обложки",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        pageCount = { images.size }
    )

    Column {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        ) { page ->
            SubcomposeAsyncImage(
                model = images[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = "Ошибка загрузки",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }

        if (images.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TrackRow(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = track.position,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(40.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        track.duration?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FormatPickerDialog(
    selectedFormat: Format,
    onFormatSelected: (Format) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите формат") },
        text = {
            Column {
                Format.entries
                    .filter { it != Format.UNKNOWN }
                    .forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFormat == format,
                                onClick = { onFormatSelected(format) }
                            )
                            Text(text = format.name)
                        }
                    }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}