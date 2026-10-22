package com.kai.cdvinylcatalog.feature.add.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun ReleaseSelectionRoute(
    query: String, // <-- теперь передаем строку запроса (или штрихкод)
    source: ReleaseSelectionContract.Source,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToLocalItem: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReleaseSelectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.results.collectAsLazyPagingItems()

    LaunchedEffect(query, source) {
        viewModel.initSearch(query, source)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ReleaseSelectionContract.Effect.NavigateBack -> onNavigateBack()
                is ReleaseSelectionContract.Effect.NavigateToDetails -> onNavigateToDetails(effect.releaseId)
                is ReleaseSelectionContract.Effect.NavigateToLocalItem -> onNavigateToLocalItem(effect.itemId)
            }
        }
    }

    ReleaseSelectionScreen(
        state = state,
        pagingItems = pagingItems,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}