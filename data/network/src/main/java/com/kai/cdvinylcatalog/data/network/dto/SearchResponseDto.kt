package com.kai.cdvinylcatalog.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Ответ от Discogs API на поиск.
 */
data class SearchResponseDto(
    @SerializedName("results") val results: List<SearchResultDto>,
    @SerializedName("pagination") val pagination: PaginationDto?
)

/**
 * Метаданные постраничной выдачи Discogs.
 */
data class PaginationDto(
    @SerializedName("page") val page: Int,
    @SerializedName("pages") val pages: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("items") val items: Int
)
