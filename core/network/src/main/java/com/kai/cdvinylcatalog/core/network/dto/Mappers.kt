package com.kai.cdvinylcatalog.core.network.dto

import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release

/**
 * Маппер DTO → Domain модель.
 * Извлекает только нужные поля и нормализует "грязные" данные от API.
 */
fun SearchResultDto.toDomain(): Release {
    return Release(
        id = id,
        title = parseTitle(title).second,
        artist = parseTitle(title).first,
        year = year?.toIntOrNull(),
        barcode = barcode?.firstOrNull(),
        coverImageUrl = coverImage,
        label = label?.firstOrNull(),
        rawFormat = rawFormat?.joinToString(", "),
        country = country,
        releaseDate = null,
        catalogNumber = null,
        notes = null,
        tracklist = emptyList()
    )
}

/**
 * Discogs возвращает title в формате "Artist - Album".
 * Разбиваем на две части. Если разделителя нет — считаем, что это название.
 */
private fun parseTitle(rawTitle: String): Pair<String, String> {
    val parts = rawTitle.split(" - ", limit = 2)
    return if (parts.size == 2) {
        parts[0].trim() to parts[1].trim()
    } else {
        "Unknown Artist" to rawTitle.trim()
    }
}

/**
 * Маппер строки формата в enum Format.
 * Discogs возвращает "CD", "Vinyl", "DVD" и т.д.
 */
fun String?.toFormat(): Format {
    if (this == null) return Format.UNKNOWN
    return when {
        contains("CD", ignoreCase = true) -> Format.CD
        contains("DVD", ignoreCase = true) -> Format.DVD
        contains("Vinyl", ignoreCase = true) -> Format.VINYL
        contains("Cassette", ignoreCase = true) -> Format.CASSETTE
        else -> Format.UNKNOWN
    }
}