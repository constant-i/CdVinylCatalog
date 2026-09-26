package com.kai.cdvinylcatalog

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application-класс для Hilt.
 * Hilt использует его как точку входа для построения графа зависимостей.
 */
@HiltAndroidApp
class CdVinylCatalogApp : Application()