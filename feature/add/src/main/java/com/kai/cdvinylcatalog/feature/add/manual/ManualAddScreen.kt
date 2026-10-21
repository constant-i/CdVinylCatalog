package com.kai.cdvinylcatalog.feature.add.manual

import android.content.res.Configuration
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.feature.add.R
import java.io.File

/**
 * Форма добавления диска вручную.
 *
 * Экран ничего не знает о файлах, `FileProvider` и `ContentResolver`: все
 * операции с хранилищем выполняет ViewModel через доменный `PhotoStorage`,
 * а запуск камеры, галереи и кропа вынесен в [ManualAddRoute].
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualAddScreen(
    state: ManualAddContract.State,
    onIntent: (ManualAddContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
    onOpenCamera: () -> Unit = {},
    onOpenGallery: () -> Unit = {},
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode)
                            stringResource(R.string.add_manual_title_edit)
                        else
                            stringResource(R.string.add_manual_title_add)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(ManualAddContract.Intent.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
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
            Text(
                text = stringResource(R.string.add_manual_required_fields),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.title,
                onValueChange = { onIntent(ManualAddContract.Intent.OnTitleChanged(it)) },
                label = { Text(stringResource(R.string.add_manual_field_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.artist,
                onValueChange = { onIntent(ManualAddContract.Intent.OnArtistChanged(it)) },
                label = { Text(stringResource(R.string.add_manual_field_artist)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.year,
                onValueChange = { onIntent(ManualAddContract.Intent.OnYearChanged(it)) },
                label = { Text(stringResource(R.string.add_manual_field_year)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.add_manual_field_format),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            FormatSelector(
                selected = state.format,
                onSelect = { onIntent(ManualAddContract.Intent.OnFormatChanged(it)) }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.label,
                onValueChange = { onIntent(ManualAddContract.Intent.OnLabelChanged(it)) },
                label = { Text(stringResource(R.string.add_manual_field_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.country,
                onValueChange = { onIntent(ManualAddContract.Intent.OnCountryChanged(it)) },
                label = { Text(stringResource(R.string.add_manual_field_country)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.barcode,
                onValueChange = { onIntent(ManualAddContract.Intent.OnBarcodeChanged(it)) },
                label = { Text(stringResource(R.string.add_manual_field_barcode)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.notes,
                onValueChange = { onIntent(ManualAddContract.Intent.OnNotesChanged(it)) },
                label = { Text(stringResource(R.string.add_manual_field_notes)) },
                placeholder = { Text(stringResource(R.string.add_manual_field_notes_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(16.dp))

            PhotosSection(
                state = state,
                onIntent = onIntent,
                onOpenCamera = onOpenCamera,
                onOpenGallery = onOpenGallery
            )

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
                    Text(
                        if (state.isEditMode)
                            stringResource(R.string.add_manual_button_update)
                        else stringResource(R.string.add_manual_button_save)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormatSelector(
    selected: Format,
    onSelect: (Format) -> Unit
) {
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
                        selected = selected == format,
                        onClick = { onSelect(format) }
                    )
                ) {
                    RadioButton(
                        selected = selected == format,
                        onClick = { onSelect(format) }
                    )
                    Text(format.name)
                }
            }
    }
}

@Composable
private fun PhotosSection(
    state: ManualAddContract.State,
    onIntent: (ManualAddContract.Intent) -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit
) {
    Text(
        text = stringResource(
            R.string.add_manual_photos_count,
            state.photoPaths.size,
            ManualAddContract.State.MAX_PHOTOS
        ),
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(8.dp))

    if (state.photoPaths.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.photoPaths) { path ->
                PhotoThumbnail(
                    path = path,
                    onRemove = { onIntent(ManualAddContract.Intent.OnPhotoRemoved(path)) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onOpenCamera,
            enabled = state.canAddMorePhotos,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.add_manual_button_camera))
        }
        OutlinedButton(
            onClick = onOpenGallery,
            enabled = state.canAddMorePhotos,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.add_manual_button_gallery))
        }
    }
}

@Composable
private fun PhotoThumbnail(
    path: String,
    onRemove: () -> Unit
) {
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
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_delete),
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp)
            )
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
