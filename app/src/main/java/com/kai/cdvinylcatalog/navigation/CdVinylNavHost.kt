package com.kai.cdvinylcatalog.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kai.cdvinylcatalog.feature.collection.CollectionRoute
import com.kai.cdvinylcatalog.feature.scan.ScanRoute

/**
 * Маршруты навигации приложения.
 */
object Routes {
    const val SCAN = "scan"
    const val COLLECTION = "collection"
}

/**
 * NavHost — граф навигации приложения.
 */
@Composable
fun CdVinylNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SCAN,
        modifier = modifier
    ) {
        composable(Routes.SCAN) {
            ScanRoute(
                onNavigateToCollection = {
                    navController.navigate(Routes.COLLECTION)
                }
            )
        }
        composable(Routes.COLLECTION) {
            CollectionRoute(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}