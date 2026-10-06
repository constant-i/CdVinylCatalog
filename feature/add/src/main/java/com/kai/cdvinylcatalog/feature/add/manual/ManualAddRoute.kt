package com.kai.cdvinylcatalog.feature.add.manual

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ManualAddRoute(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManualAddViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ManualAddContract.Effect.NavigateBack -> onNavigateBack()
                is ManualAddContract.Effect.NavigateToDetails -> {
                    onNavigateToDetails(effect.itemId)
                }
                is ManualAddContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ManualAddScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}