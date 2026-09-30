package com.kai.cdvinylcatalog.core.model

/**
 * Доменная модель релиза (компакт-диск или пластинка).
 * Содержит только те поля, которые нужны приложению.
 */
data class Release(
    val id: Long,
    val title: String,
    val artist: String,
    val year: Int?,
    val barcode: String?,
    val coverImageUrl: String?,
    val imageUrls: List<String> = emptyList(),
    val label: String?,
    val rawFormat: String?,        // "CD, Album" от API
    val country: String?,          // "US", "EU", "JP"
    val releaseDate: String?,      // "1991-09-24"
    val catalogNumber: String?,    // "DGCD-24425"
    val notes: String?,            // примечания от Discogs
    val tracklist: List<Track> = emptyList()
)