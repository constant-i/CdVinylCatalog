package com.kai.cdvinylcatalog.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.feature.collection.crate.CollectionRoute
import com.kai.cdvinylcatalog.feature.collection.detail.CollectionItemDetailRoute
import com.kai.cdvinylcatalog.feature.add.scan.ScanRoute
import com.kai.cdvinylcatalog.feature.add.details.ReleaseDetailsRoute
import com.kai.cdvinylcatalog.feature.add.manual.ManualAddRoute
import com.kai.cdvinylcatalog.feature.add.search.SearchRoute
import com.kai.cdvinylcatalog.feature.add.selection.ReleaseSelectionContract
import com.kai.cdvinylcatalog.feature.add.selection.ReleaseSelectionRoute
import com.kai.cdvinylcatalog.home.HomeRoute

object Routes {
    const val HOME = "home"
    const val SCAN = "scan"
    const val COLLECTION = "collection"
    const val COLLECTION_ITEM = "collection_item/{itemId}"
    const val SEARCH = "search"
    const val RELEASE_DETAILS = "release_details/{releaseId}"
    const val SELECTION = "selection"
    const val MANUAL_ADD = "manual_add"
    const val MANUAL_EDIT = "manual_edit/{itemId}"

    fun collectionItem(itemId: Long) = "collection_item/$itemId"
    fun releaseDetails(releaseId: Long) = "release_details/$releaseId"
    fun manualEdit(itemId: Long) = "manual_edit/$itemId"

}

@Composable
fun CdVinylNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Shared State для передачи результатов сканирования на экран поиска
    var pendingResults by remember { mutableStateOf<List<Release>?>(null) }

    var pendingSource by remember { mutableStateOf(ReleaseSelectionContract.Source.SEARCH) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeRoute(
                onNavigateToScan = { navController.navigate(Routes.SCAN) },
                onNavigateToSearch = {
                    pendingResults = null
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToManualAdd = { navController.navigate(Routes.MANUAL_ADD) },
                onNavigateToCollection = { navController.navigate(Routes.COLLECTION) }
            )
        }
        composable(Routes.SCAN) {
            ScanRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCollection = {
                    navController.navigate(Routes.COLLECTION) {
                        popUpTo(Routes.HOME)
                    }
                },
                onNavigateToSearch = {
                    pendingResults = null
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToSelection = { results ->
                    pendingResults = results
                    pendingSource = ReleaseSelectionContract.Source.SCAN
                    navController.navigate(Routes.SELECTION)
                }
            )
        }
        composable(Routes.COLLECTION) {
            CollectionRoute(
                onNavigateBack = { navController.popBackStack() },
                onItemClicked = { itemId ->
                    navController.navigate(Routes.collectionItem(itemId))
                },
                onNavigateToScan = {
                    navController.navigate(Routes.SCAN)
                },
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
                }
            )
        }
        composable(
            route = Routes.COLLECTION_ITEM,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            CollectionItemDetailRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { itemId ->
                    navController.navigate(Routes.manualEdit(itemId))
                }
            )
        }
        composable(Routes.SEARCH) {
            SearchRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelection = { results ->
                    pendingResults = results
                    pendingSource = ReleaseSelectionContract.Source.SEARCH
                    navController.navigate(Routes.SELECTION)
                }
            )
        }
        composable(
            route = Routes.RELEASE_DETAILS,
            arguments = listOf(navArgument("releaseId") { type = NavType.LongType })
        ) {
            ReleaseDetailsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCollection = {
                    pendingResults = null
                    navController.navigate(Routes.COLLECTION) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(Routes.SELECTION) {
            val results = pendingResults
            if (results != null) {
                ReleaseSelectionRoute(
                    results = results,
                    source = pendingSource,
                    onNavigateBack = {
                        pendingResults = null
                        navController.popBackStack()
                    },
                    onNavigateToDetails = { releaseId ->
                        navController.navigate(Routes.releaseDetails(releaseId))
                    }
                )
            }
        }
        composable(Routes.MANUAL_ADD) {
            ManualAddRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { itemId ->
                    navController.navigate(Routes.collectionItem(itemId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.MANUAL_EDIT,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            ManualAddRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { }
            )
        }
    }
}