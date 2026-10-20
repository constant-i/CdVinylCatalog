package com.kai.cdvinylcatalog.feature.add.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSelection: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SearchContract.Effect.NavigateBack -> onNavigateBack()
                is SearchContract.Effect.NavigateToSelection -> {
                    onNavigateToSelection(effect.query)
                }
            }
        }
    }

    SearchScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}
