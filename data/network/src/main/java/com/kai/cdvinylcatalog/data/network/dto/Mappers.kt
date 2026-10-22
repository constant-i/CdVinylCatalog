package com.kai.cdvinylcatalog.data.network.dto

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.catalog.Track
import com.kai.cdvinylcatalog.domain.catalog.normalizeBarcode

/**
 * Маппер DTO → Domain модель.
 * Извлекает только нужные поля и нормализует "грязные" данные от API.
 */
fun SearchResultDto.toDomain(): Release {
    return Release(
        id = id,
        title = parseTitle(title).second,
        artist = parseTitle(title).first,
        year = year?.toIntOrNull()?.takeIf { it > 0 },
        barcode = barcode?.firstOrNull()?.let(::normalizeBarcode),
        coverImageUrl = coverImage,
        imageUrls = listOfNotNull(coverImage),
        label = label?.firstOrNull(),
        rawFormat = format?.joinToString(", "),
        country = country,
        releaseDate = null,
        catalogNumber = null,
        notes = null,
        tracklist = emptyList()
    )
}

private const val BARCODE_TYPE = "Barcode"

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

fun ReleaseDetailsDto.toDomain(): Release {
    val allImages = images?.map { it.uri } ?: emptyList()
    return Release(
        id = id,
        title = parseTitle(title).second,
        artist = artists?.joinToString(", ") { it.name } ?: parseTitle(title).first,
        year = year?.takeIf { it > 0 },
        barcode = identifiers
            ?.firstOrNull { it.type.equals(BARCODE_TYPE, ignoreCase = true) }
            ?.value
            ?.let(::normalizeBarcode),
        coverImageUrl = images?.firstOrNull { it.type == "primary" }?.uri
            ?: allImages.firstOrNull(),
        imageUrls = allImages,
        label = labels?.joinToString(", ") { it.name },
        rawFormat = formats?.joinToString(", ") { format ->
            buildString {
                append(format.name)
                format.descriptions?.let {
                    if (it.isNotEmpty()) {
                        append(", ")
                        append(it.joinToString(", "))
                    }
                }
            }
        },
        country = country,
        releaseDate = released,
        catalogNumber = labels?.firstOrNull()?.catno,
        notes = notes,
        tracklist = tracklist?.map { it.toDomain() } ?: emptyList()
    )
}

private fun TrackDto.toDomain(): Track {
    return Track(
        position = position,
        title = title,
        duration = duration
    )
}