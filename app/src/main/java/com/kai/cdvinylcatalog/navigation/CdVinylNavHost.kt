package com.kai.cdvinylcatalog.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
    const val SELECTION = "selection/{source}/{query}"
    const val MANUAL_ADD = "manual_add"
    const val MANUAL_EDIT = "manual_edit/{itemId}"

    fun collectionItem(itemId: Long) = "collection_item/$itemId"
    fun releaseDetails(releaseId: Long) = "release_details/$releaseId"
    fun manualEdit(itemId: Long) = "manual_edit/$itemId"

    /**
     * Экран результатов поиска в Discogs.
     *
     * @param source откуда пришли (сканирование штрихкода или ручной поиск)
     * @param query  штрихкод или поисковая строка
     */
    fun selection(source: ReleaseSelectionContract.Source, query: String) =
        "selection/${source.name}/${Uri.encode(query)}"
}

@Composable
fun CdVinylNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeRoute(
                onNavigateToScan = { navController.navigate(Routes.SCAN) },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
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
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToSelection = { barcode ->
                    navController.navigate(
                        Routes.selection(ReleaseSelectionContract.Source.SCAN, barcode)
                    )
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
                },
                onNavigateToCopy = { copyId ->
                    navController.navigate(Routes.collectionItem(copyId)) {
                        popUpTo(Routes.COLLECTION_ITEM) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SEARCH) {
            SearchRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelection = { query ->
                    navController.navigate(
                        Routes.selection(ReleaseSelectionContract.Source.SEARCH, query)
                    )
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
                    navController.navigate(Routes.COLLECTION) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.SELECTION,
            arguments = listOf(
                navArgument("source") { type = NavType.StringType },
                navArgument("query") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val source = backStackEntry.arguments
                ?.getString("source")
                ?.let { name ->
                    ReleaseSelectionContract.Source.entries.firstOrNull { it.name == name }
                }
                ?: ReleaseSelectionContract.Source.SEARCH

            val query = backStackEntry.arguments?.getString("query").orEmpty()

            if (query.isBlank()) {
                // Пустой запрос — показывать нечего, возвращаемся назад
                navController.popBackStack()
            } else {
                ReleaseSelectionRoute(
                    query = query,
                    source = source,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { releaseId ->
                        navController.navigate(Routes.releaseDetails(releaseId))
                    },
                    onNavigateToLocalItem = { itemId ->
                        navController.navigate(Routes.collectionItem(itemId))
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
