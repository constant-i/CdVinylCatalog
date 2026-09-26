package com.kai.cdvinylcatalog.core.network

import com.kai.cdvinylcatalog.core.network.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit-интерфейс для Discogs API.
 * Все методы — suspend, чтобы вызываться из корутин.
 */
interface DiscogsApi {

    /**
     * Поиск релиза по штрихкоду.
     *
     * @param barcode штрихкод с диска или пластинки
     * @param token токен Discogs (передаётся как query-параметр)
     */
    @GET("database/search")
    suspend fun searchByBarcode(
        @Query("barcode") barcode: String,
        @Query("type") type: String = "release",
        @Query("token") token: String
    ): SearchResponseDto
}