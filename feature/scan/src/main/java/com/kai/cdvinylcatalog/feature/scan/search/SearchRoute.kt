package com.kai.cdvinylcatalog.feature.scan.search

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kai.cdvinylcatalog.core.model.Release

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSelection: (List<Release>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SearchContract.Effect.NavigateBack -> onNavigateBack()
                is SearchContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SearchContract.Effect.NavigateToSelection -> {
                    onNavigateToSelection(effect.results)
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