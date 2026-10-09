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

/**
 * Объединяет отредактированные пользователем данные с данными из Discogs.
 * Приоритет — у данных пользователя.
 * Если поле пустое — берём из Discogs.
 */
fun Release.mergeWith(discogs: Release?): Release {
    if (discogs == null) return this

    return Release(
        id = if (id > 0) id else discogs.id,
        title = title.ifBlank { discogs.title },
        artist = artist.ifBlank { discogs.artist },
        year = year ?: discogs.year,
        barcode = barcode ?: discogs.barcode,
        coverImageUrl = coverImageUrl ?: discogs.coverImageUrl,
        imageUrls = imageUrls.ifEmpty { discogs.imageUrls },
        label = label ?: discogs.label,
        rawFormat = rawFormat ?: discogs.rawFormat,
        country = country ?: discogs.country,
        releaseDate = releaseDate ?: discogs.releaseDate,
        catalogNumber = catalogNumber ?: discogs.catalogNumber,
        notes = notes ?: discogs.notes,
        // Треклист всегда из Discogs — пользователь его не редактирует
        tracklist = discogs.tracklist.ifEmpty { tracklist }
    )
}