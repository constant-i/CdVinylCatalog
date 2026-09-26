package com.kai.cdvinylcatalog.feature.scan

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Route — "умная" обёртка над ScanScreen.
 * Связывает ViewModel с UI, обрабатывает Effect.
 */
@Composable
fun ScanRoute(
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Обработка одноразовых событий
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScanContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                ScanContract.Effect.NavigateToCollection -> {
                    // TODO: навигация — добавим позже
                }
            }
        }
    }

    ScanScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}