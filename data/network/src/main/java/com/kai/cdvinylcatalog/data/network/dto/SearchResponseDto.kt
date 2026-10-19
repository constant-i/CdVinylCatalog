package com.kai.cdvinylcatalog.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Ответ от Discogs API на поиск.
 */
data class SearchResponseDto(
    @SerializedName("results") val results: List<SearchResultDto>
)