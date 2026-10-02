package com.kai.cdvinylcatalog.feature.scan.search

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    initialResults: List<com.kai.cdvinylcatalog.core.model.Release>? = null,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(initialResults) {
        if (initialResults != null) {
            viewModel.setInitialResults(initialResults)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SearchContract.Effect.NavigateBack -> onNavigateBack()
                is SearchContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SearchContract.Effect.OnReleaseAdded -> {
                    // Можно вернуться назад после добавления
                    onNavigateBack()
                }
                is SearchContract.Effect.ShowAddAnotherDialog -> {
                    // Показываем AlertDialog
                }
            }
        }
    }

    SearchScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )

    val askAddAnother = state.askAddAnother
    if (askAddAnother != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SearchContract.Intent.OnAddAnotherDismissed) },
            title = { Text("Уже в коллекции") },
            text = {
                Text("«${askAddAnother.title}» уже есть в вашей коллекции. Добавить ещё одну копию?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onIntent(SearchContract.Intent.OnAddAnotherConfirmed(askAddAnother))
                }) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onIntent(SearchContract.Intent.OnAddAnotherDismissed)
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог выбора формата
    val selectedRelease = state.selectedRelease
    if (state.showFormatPicker && selectedRelease != null) {
        FormatPickerDialog(
            release = selectedRelease,
            selectedFormat = state.selectedFormat,
            onFormatSelected = {
                viewModel.onIntent(SearchContract.Intent.OnFormatSelected(it))
            },
            onConfirm = {
                viewModel.onIntent(SearchContract.Intent.OnConfirmAdd)
            },
            onDismiss = {
                viewModel.onIntent(SearchContract.Intent.OnFormatPickerDismissed)
            }
        )
    }
}