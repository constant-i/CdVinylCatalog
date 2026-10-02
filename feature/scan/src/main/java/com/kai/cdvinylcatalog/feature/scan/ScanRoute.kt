package com.kai.cdvinylcatalog.feature.scan

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ScanRoute(
    onNavigateBack: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSelection: (List<com.kai.cdvinylcatalog.core.model.Release>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScanContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ScanContract.Effect.NavigateToSelection -> {
                    onNavigateToSelection(effect.results)
                }
            }
        }
    }

    ScanScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onNavigateToCollection = onNavigateToCollection,
        onNavigateToSearch = onNavigateToSearch,
        modifier = modifier
    )
}