package com.kai.cdvinylcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kai.cdvinylcatalog.navigation.CdVinylNavHost
import com.kai.cdvinylcatalog.core.ui.CdVinylCatalogTheme
import dagger.hilt.android.AndroidEntryPoint
import com.kai.cdvinylcatalog.core.ui.OrientationHandler

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Настраиваем стиль системных панелей под тёмную тему
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            OrientationHandler()
            CdVinylCatalogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CdVinylNavHost(navController = navController)
                }
            }
        }
    }
}