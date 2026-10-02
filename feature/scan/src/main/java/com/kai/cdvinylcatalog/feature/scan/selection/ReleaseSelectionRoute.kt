package com.kai.cdvinylcatalog.feature.scan.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kai.cdvinylcatalog.core.model.Release

@Composable
fun ReleaseSelectionRoute(
    results: List<Release>,
    source: ReleaseSelectionContract.Source,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReleaseSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(results, source) {
        viewModel.setResults(results, source)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ReleaseSelectionContract.Effect.NavigateBack -> onNavigateBack()
                is ReleaseSelectionContract.Effect.NavigateToDetails -> {
                    onNavigateToDetails(effect.releaseId)
                }
            }
        }
    }

    ReleaseSelectionScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}