package com.kai.cdvinylcatalog.feature.add.details

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kai.cdvinylcatalog.common.ui.CdVinylCatalogTheme
import com.kai.cdvinylcatalog.common.ui.asText
import com.kai.cdvinylcatalog.common.ui.component.CdTopBar
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.feature.add.R

/**
 * Экран деталей релиза из Discogs.
 *
 * Здесь только каркас: топ-бар, состояния загрузки и ошибки, диалоги.
 * Содержимое вынесено в [ReleaseContent], топ-бар — в [CdTopBar],
 * диалоги — в [FormatPickerDialog] и [AddAnotherCopyDialog].
 */
@OptIn(ExperimentalFoundationApi::class)
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
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CdTopBar(
                title = state.release?.title ?: stringResource(R.string.details_title_default),
                onBack = { onIntent(ReleaseDetailsContract.Intent.OnBackClicked) }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))

            state.error != null -> ErrorState(
                message = state.error.asText(context),
                modifier = Modifier.padding(padding)
            )

            state.release != null -> ReleaseContent(
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

    if (state.showFormatPicker) {
        FormatPickerDialog(
            selectedFormat = state.selectedFormat,
            onFormatSelected = {
                onIntent(ReleaseDetailsContract.Intent.OnFormatSelected(it))
            },
            onConfirm = { onIntent(ReleaseDetailsContract.Intent.OnConfirmAdd) },
            onDismiss = { onIntent(ReleaseDetailsContract.Intent.OnFormatPickerDismissed) }
        )
    }

    if (state.askAddAnother && state.release != null) {
        AddAnotherCopyDialog(
            releaseTitle = state.release.title,
            onConfirm = { onIntent(ReleaseDetailsContract.Intent.OnAddAnotherConfirmed) },
            onDismiss = { onIntent(ReleaseDetailsContract.Intent.OnAddAnotherDismissed) }
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReleaseDetailsScreenPreview() {
    CdVinylCatalogTheme {
        ReleaseDetailsScreen(
            state = ReleaseDetailsContract.State(
                isLoading = false,
                release = previewRelease
            ),
            onIntent = {}
        )
    }
}

private val previewRelease = Release(
    id = 1L,
    title = "Nevermind",
    artist = "Nirvana",
    year = 1991,
    barcode = null,
    coverImageUrl = null,
    label = "DGC",
    rawFormat = "CD, Album",
    country = "US",
    releaseDate = null,
    catalogNumber = "DGCD-24425",
    notes = null
)
