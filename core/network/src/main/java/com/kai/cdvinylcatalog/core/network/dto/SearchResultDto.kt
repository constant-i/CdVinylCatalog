package com.kai.cdvinylcatalog.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Один результат поиска в Discogs.
 */
data class SearchResultDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("year") val year: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("format") val rawFormat: List<String>?,
    @SerializedName("label") val label: List<String>?,
    @SerializedName("cover_image") val coverImage: String?,
    @SerializedName("barcode") val barcode: List<String>?
)