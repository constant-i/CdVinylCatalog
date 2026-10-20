package com.kai.cdvinylcatalog.feature.add.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScanRoute(
    onNavigateBack: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSelection: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScanContract.Effect.NavigateToSelection -> {
                    onNavigateToSelection(effect.barcode)
                }
            }
        }
    }

    ScanScreen(
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onNavigateToCollection = onNavigateToCollection,
        onNavigateToSearch = onNavigateToSearch,
        modifier = modifier
    )
}
