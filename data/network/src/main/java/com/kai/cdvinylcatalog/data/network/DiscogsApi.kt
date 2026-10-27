package com.kai.cdvinylcatalog.data.network

import com.kai.cdvinylcatalog.data.network.dto.ReleaseDetailsDto
import com.kai.cdvinylcatalog.data.network.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit-интерфейс для Discogs API.
 * Все методы — suspend, чтобы вызываться из корутин.
 */
interface DiscogsApi {

    @GET("database/search")
    suspend fun searchByBarcode(
        @Query("barcode") barcode: String,
        @Query("type") type: String = "release",
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 50
    ): SearchResponseDto

    @GET("releases/{releaseId}")
    suspend fun getReleaseDetails(
        @Path("releaseId") releaseId: Long
    ): ReleaseDetailsDto

    @GET("database/search")
    suspend fun searchByText(
        @Query("q") query: String,
        @Query("type") type: String = "release",
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 50
    ): SearchResponseDto
}
