package com.kai.cdvinylcatalog.feature.collection.crate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CollectionRoute(
    onNavigateBack: () -> Unit,
    onItemClicked: (Long) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CollectionContract.Effect.NavigateBack -> onNavigateBack()
                CollectionContract.Effect.NavigateToScan -> onNavigateToScan()
                CollectionContract.Effect.NavigateToSearch -> onNavigateToSearch()
            }
        }
    }

    CollectionScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onItemClicked = onItemClicked,
        modifier = modifier
    )
}