package com.kai.cdvinylcatalog.feature.scan.details

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ReleaseDetailsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToCollection: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReleaseDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReleaseDetailsContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                ReleaseDetailsContract.Effect.NavigateBack -> onNavigateBack()
                ReleaseDetailsContract.Effect.NavigateToCollection -> onNavigateToCollection()
            }
        }
    }

    ReleaseDetailsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}