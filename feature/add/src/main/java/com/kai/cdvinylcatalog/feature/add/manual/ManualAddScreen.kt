package com.kai.cdvinylcatalog.feature.add.manual

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.ui.CdVinylCatalogTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualAddScreen(
    state: ManualAddContract.State,
    onIntent: (ManualAddContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current

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