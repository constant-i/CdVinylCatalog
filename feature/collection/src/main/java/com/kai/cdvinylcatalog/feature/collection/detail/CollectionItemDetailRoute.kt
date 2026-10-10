package com.kai.cdvinylcatalog.feature.collection.detail

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CollectionItemDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToCopy: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionItemDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CollectionItemDetailContract.Effect.NavigateBack -> onNavigateBack()
                is CollectionItemDetailContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CollectionItemDetailContract.Effect.NavigateToEdit -> {
                    onNavigateToEdit(effect.itemId)
                }
                is CollectionItemDetailContract.Effect.NavigateToCopy -> {
                    onNavigateToCopy(effect.itemId)
                }
            }
        }
    }

    CollectionItemDetailScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}