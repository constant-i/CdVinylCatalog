package com.kai.cdvinylcatalog.feature.add.manual

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.ui.CdVinylCatalogTheme
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualAddScreen(
    state: ManualAddContract.State,
    onIntent: (ManualAddContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current

    val context = LocalContext.current

    fun createPhotoFile(): File {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun copyToAppStorage(sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "photos").apply { mkdirs() }
            val destFile = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            state.pendingPhotoPath?.let { path ->
                onIntent(ManualAddContract.Intent.OnStartCrop(path))
            }
        } else {
            onIntent(ManualAddContract.Intent.OnPendingPhotoCleared)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            copyToAppStorage(it)?.let { path ->
                onIntent(ManualAddContract.Intent.OnPhotoAdded(path))
            }
        }
    }

    val uCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when {
            result.resultCode == Activity.RESULT_OK -> {
                val croppedUri = result.data?.let { UCrop.getOutput(it) }
                // Конвертируем content:// URI в абсолютный путь файла
                val absolutePath = croppedUri?.let { uri ->
                    if (uri.scheme == "file") {
                        uri.path  // file:// URI — path уже абсолютный
                    } else {
                        // content:// URI — конвертируем через FileProvider
                        // Но проще — сохранить копию файла в наше хранилище
                        copyToAppStorage(uri)
                    }
                }
                absolutePath?.let { path ->
                    onIntent(ManualAddContract.Intent.OnCropFinished(path))
                }
            }
            result.resultCode == UCrop.RESULT_ERROR -> {
                val error = result.data?.let { UCrop.getError(it) }
                onIntent(ManualAddContract.Intent.OnCropCancelled)
            }
            else -> {
                onIntent(ManualAddContract.Intent.OnCropCancelled)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onIntent(ManualAddContract.Intent.OnCheckPendingPhoto)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        onIntent(ManualAddContract.Intent.OnCheckPendingPhoto)
    }

    LaunchedEffect(state.pendingCropPath) {
        val path = state.pendingCropPath ?: return@LaunchedEffect
        val sourceFile = File(path)
        if (!sourceFile.exists()) return@LaunchedEffect

        val sourceUri = getUriForFile(sourceFile)
        val destFile = createPhotoFile()
        val destUri = getUriForFile(destFile)

        val options = UCrop.Options().apply {
            setAspectRatioOptions(
                0,
                AspectRatio("Свободно", 0f, 0f),
                AspectRatio("1:1", 1f, 1f),
                AspectRatio("4:3", 4f, 3f),
                AspectRatio("16:9", 16f, 9f)
            )
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(true)
        }

        val intent = UCrop.of(sourceUri, destUri)
            .withOptions(options)
            .withMaxResultSize(1080, 1080)
            .getIntent(context)

        uCropLauncher.launch(intent)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditMode) "Редактирование" else "Добавить вручную")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onIntent(ManualAddContract.Intent.OnBackClicked)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Обязательные поля *", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.title,
                onValueChange = { onIntent(ManualAddContract.Intent.OnTitleChanged(it)) },
                label = { Text("Название *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.artist,
                onValueChange = { onIntent(ManualAddContract.Intent.OnArtistChanged(it)) },
                label = { Text("Исполнитель *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.year,
                onValueChange = { onIntent(ManualAddContract.Intent.OnYearChanged(it)) },
                label = { Text("Год") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            Text("Формат *", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Format.entries
                    .filter { it != Format.UNKNOWN }
                    .forEach { format ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = state.format == format,
                                onClick = {
                                    onIntent(ManualAddContract.Intent.OnFormatChanged(format))
                                }
                            )
                        ) {
                            RadioButton(
                                selected = state.format == format,
                                onClick = {
                                    onIntent(ManualAddContract.Intent.OnFormatChanged(format))
                                }
                            )
                            Text(format.name)
                        }
                    }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.label,
                onValueChange = { onIntent(ManualAddContract.Intent.OnLabelChanged(it)) },
                label = { Text("Лейбл") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.country,
                onValueChange = { onIntent(ManualAddContract.Intent.OnCountryChanged(it)) },
                label = { Text("Страна") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.barcode,
                onValueChange = { onIntent(ManualAddContract.Intent.OnBarcodeChanged(it)) },
                label = { Text("Штрихкод") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.notes,
                onValueChange = { onIntent(ManualAddContract.Intent.OnNotesChanged(it)) },
                label = { Text("Заметка") },
                placeholder = { Text("Например: подарок, подписана") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Фотографии (${state.photoPaths.size}/${ManualAddContract.State.MAX_PHOTOS})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            if (state.photoPaths.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.photoPaths) { path ->
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = File(path),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    onIntent(ManualAddContract.Intent.OnPhotoRemoved(path))
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val photoFile = createPhotoFile()
                        val uri = getUriForFile(photoFile)
                        onIntent(ManualAddContract.Intent.OnCameraLaunched(photoFile.absolutePath))
                        cameraLauncher.launch(uri)
                    },
                    enabled = state.canAddMorePhotos,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Камера")
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = state.canAddMorePhotos,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Галерея")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    focus.clearFocus()
                    keyboard?.hide()
                    onIntent(ManualAddContract.Intent.OnSaveClicked)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isValid && !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.isEditMode) "Обновить" else "Сохранить в коллекцию")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ManualAddScreenPreview() {
    CdVinylCatalogTheme {
        ManualAddScreen(
            state = ManualAddContract.State(
                title = "Nevermind",
                artist = "Nirvana",
                year = "1991",
                format = Format.CD
            ),
            onIntent = {}
        )
    }
}