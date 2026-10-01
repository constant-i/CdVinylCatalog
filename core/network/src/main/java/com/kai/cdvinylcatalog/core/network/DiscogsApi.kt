package com.kai.cdvinylcatalog.core.network

import com.kai.cdvinylcatalog.core.network.dto.ReleaseDetailsDto
import com.kai.cdvinylcatalog.core.network.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
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

    /**
     * Поиск информации о конкретном релизе по ID релиза в базе Discogs (releaseId).
     *
     * @param releaseId ID релиза в базе Discogs
     * @param token токен Discogs (передаётся как query-параметр)
     */
    @GET("releases/{releaseId}")
    suspend fun getReleaseDetails(
        @Path("releaseId") releaseId: Long,
        @Query("token") token: String
    ): ReleaseDetailsDto

    /**
     * Поиск релизов по текстовому запросу.
     *
     * @param query поисковая строка (название релиза, артист и т.п.)
     * @param type тип искомой сущности, по умолчанию "release"
     * @param token токен авторизации Discogs
     */
    @GET("database/search")
    suspend fun searchByText(
        @Query("q") query: String,
        @Query("type") type: String = "release",
        @Query("token") token: String
    ): SearchResponseDto
}