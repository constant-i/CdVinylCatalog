package com.kai.cdvinylcatalog.core.network

/**
 * Константы для сетевого слоя.
 */
object NetworkConstants {
    const val BASE_URL = "https://api.discogs.com/"
    const val USER_AGENT = "CdVinylCatalog/1.0 +https://github.com/constant-i/cdvinylcatalog"
    const val TIMEOUT_SECONDS = 30L
    val DISCOGS_TOKEN: String = BuildConfig.DISCOGS_TOKEN
}
