package com.kai.cdvinylcatalog

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "Activity onCreate: savedInstanceState=${savedInstanceState != null}")
        enableEdgeToEdge()
        setContent {
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

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "Activity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "Activity onResume")
    }

    override fun onPause() {
        Log.d("Lifecycle", "Activity onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("Lifecycle", "Activity onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("Lifecycle", "Activity onDestroy")
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("Lifecycle", "Activity onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }
}