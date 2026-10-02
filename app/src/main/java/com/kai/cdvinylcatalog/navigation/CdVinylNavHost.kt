package com.kai.cdvinylcatalog.navigation

import android.widget.Toast
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
import com.kai.cdvinylcatalog.feature.collection.CollectionRoute
import com.kai.cdvinylcatalog.feature.collection.detail.CollectionItemDetailRoute
import com.kai.cdvinylcatalog.feature.scan.ScanRoute
import com.kai.cdvinylcatalog.feature.scan.search.SearchRoute
import com.kai.cdvinylcatalog.home.HomeRoute

object Routes {
    const val HOME = "home"
    const val SCAN = "scan"
    const val COLLECTION = "collection"
    const val COLLECTION_ITEM = "collection_item/{itemId}"
    const val SEARCH = "search"

    fun collectionItem(itemId: Long) = "collection_item/$itemId"
}

@Composable
fun CdVinylNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Shared State для передачи результатов сканирования на экран поиска
    var pendingResults by remember { mutableStateOf<List<Release>?>(null) }

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
                onNavigateToManualAdd = {
                    Toast.makeText(
                        navController.context,
                        "Ручное добавление в разработке",
                        Toast.LENGTH_SHORT
                    ).show()
                },
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
                onNavigateToSearchWithResults = { results ->
                    pendingResults = results
                    navController.navigate(Routes.SEARCH)
                }
            )
        }

        composable(Routes.COLLECTION) {
            CollectionRoute(
                onNavigateBack = { navController.popBackStack() },
                onItemClicked = { itemId ->
                    navController.navigate(Routes.collectionItem(itemId))
                }
            )
        }

        composable(
            route = Routes.COLLECTION_ITEM,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            CollectionItemDetailRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SEARCH) {
            SearchRoute(
                onNavigateBack = {
                    pendingResults = null
                    navController.popBackStack()
                },
                initialResults = pendingResults
            )
        }
    }
}