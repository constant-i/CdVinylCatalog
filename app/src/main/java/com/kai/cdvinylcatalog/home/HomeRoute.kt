package com.kai.cdvinylcatalog.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Route — обёртка над HomeScreen.
 * Пока не требует ViewModel.
 */
@Composable
fun HomeRoute(
    onNavigateToScan: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToManualAdd: () -> Unit,
    onNavigateToCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeScreen(
        onNavigateToScan = onNavigateToScan,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToManualAdd = onNavigateToManualAdd,
        onNavigateToCollection = onNavigateToCollection,
        modifier = modifier
    )
}