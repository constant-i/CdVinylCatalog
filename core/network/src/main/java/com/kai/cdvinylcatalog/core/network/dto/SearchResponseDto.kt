package com.kai.cdvinylcatalog.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Ответ от Discogs API на поиск.
 */
data class SearchResponseDto(
    @SerializedName("results") val results: List<SearchResultDto>
)