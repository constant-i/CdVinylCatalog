package com.kai.cdvinylcatalog.feature.add.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.component.ReleaseInfo
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.catalog.Track
import com.kai.cdvinylcatalog.feature.add.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Содержимое экрана деталей релиза.
 *
 * Общая часть — обложки, заголовок, метаданные и треклист — приходит из
 * [ReleaseInfo] (`:common-ui`). Своё, специфичное для этого экрана, добавляется
 * хвостовым слотом: статус коллекции, поле заметки и кнопки действий.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ReleaseContent(
    state: ReleaseDetailsContract.State,
    release: Release,
    onIntent: (ReleaseDetailsContract.Intent) -> Unit,
    onKeyboardDismiss: () -> Unit,
    bringIntoViewRequester: BringIntoViewRequester,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ReleaseInfo(
            release = release
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            CollectionStatus(state = state)

            NotesField(
                notes = state.userNotes,
                onNotesChanged = { onIntent(ReleaseDetailsContract.Intent.OnNotesChanged(it)) },
                bringIntoViewRequester = bringIntoViewRequester,
                coroutineScope = coroutineScope
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButtons(
                isInCollection = state.isInCollection,
                onAdd = {
                    onKeyboardDismiss()
                    onIntent(ReleaseDetailsContract.Intent.OnAddClicked)
                },
                onBack = {
                    onKeyboardDismiss()
                    onIntent(ReleaseDetailsContract.Intent.OnBackClicked)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CollectionStatus(state: ReleaseDetailsContract.State) {
    when {
        state.justAdded -> {
            Text(
                text = stringResource(R.string.details_added_successfully),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        state.isInCollection -> {
            Text(
                text = stringResource(R.string.details_already_in_collection),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesField(
    notes: String,
    onNotesChanged: (String) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester,
    coroutineScope: CoroutineScope
) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChanged,
        label = { Text(stringResource(R.string.details_note_label)) },
        placeholder = { Text(stringResource(R.string.details_note_placeholder)) },
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                // Поле у низа экрана: без прокрутки клавиатура его перекроет
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        maxLines = 3
    )
}

@Composable
private fun ActionButtons(
    isInCollection: Boolean,
    onAdd: () -> Unit,
    onBack: () -> Unit
) {
    Button(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            stringResource(
                if (isInCollection) R.string.details_add_another_copy
                else R.string.details_add_to_collection
            )
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(
        onClick = onBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.common_back))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(name = "Содержимое релиза", showBackground = true, locale = "ru", heightDp = 900)
@Composable
private fun ReleaseContentPreview() {
    CdVinylCatalogTheme {
        ReleaseContent(
            state = ReleaseDetailsContract.State(isLoading = false),
            release = previewReleaseContent,
            onIntent = {},
            onKeyboardDismiss = {},
            bringIntoViewRequester = remember { BringIntoViewRequester() },
            coroutineScope = rememberCoroutineScope()
        )
    }
}

private val previewReleaseContent = Release(
    id = 1L,
    title = "Once",
    artist = "Nightwish",
    year = 2004,
    barcode = "727361129125",
    coverImageUrl = null,
    label = "Nuclear Blast",
    rawFormat = "CD, Album",
    country = "Europe",
    releaseDate = null,
    catalogNumber = "NB 1291-2",
    notes = null,
    tracklist = listOf(
        Track(position = "1", title = "Dark Chest Of Wonders", duration = "4:28"),
        Track(position = "2", title = "Wish I Had An Angel", duration = "4:03"),
        Track(position = "3", title = "Nemo", duration = "4:36")
    )
)
