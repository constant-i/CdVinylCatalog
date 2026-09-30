package com.kai.cdvinylcatalog.core.network.dto

import com.google.gson.annotations.SerializedName

data class ReleaseDetailsDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("year") val year: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("released") val released: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("artists") val artists: List<ArtistDto>?,
    @SerializedName("labels") val labels: List<LabelDto>?,
    @SerializedName("formats") val formats: List<FormatDto>?,
    @SerializedName("tracklist") val tracklist: List<TrackDto>?,
    @SerializedName("images") val images: List<ImageDto>?
)

data class ArtistDto(
    @SerializedName("name") val name: String
)

data class LabelDto(
    @SerializedName("name") val name: String,
    @SerializedName("catno") val catno: String?
)

data class FormatDto(
    @SerializedName("name") val name: String,
    @SerializedName("descriptions") val descriptions: List<String>?
)

data class TrackDto(
    @SerializedName("position") val position: String,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: String?
)

data class ImageDto(
    @SerializedName("uri") val uri: String,
    @SerializedName("type") val type: String?
)