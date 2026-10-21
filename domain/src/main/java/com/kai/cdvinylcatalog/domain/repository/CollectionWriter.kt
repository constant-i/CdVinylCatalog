package com.kai.cdvinylcatalog.domain.repository

import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.Outcome

/**
 * Изменение коллекции пользователя.
 *
 * Отдельный порт, чтобы экраны только для чтения физически не могли
 * удалить или перезаписать запись.
 */
interface CollectionWriter {

    /** Добавляет релиз из Discogs. */
    suspend fun addToCollection(
        release: Release,
        format: Format,
        notes: String? = null
    ): Outcome<Long>

    /** Добавляет диск, которого нет в Discogs. */
    suspend fun addManualItem(
        title: String,
        artist: String,
        year: Int?,
        format: Format,
        label: String?,
        country: String?,
        barcode: String?,
        notes: String?,
        photoPaths: List<String> = emptyList()
    ): Outcome<Long>

    /** Обновляет существующую запись. */
    suspend fun updateItem(
        id: Long,
        title: String,
        artist: String,
        year: Int?,
        format: Format,
        label: String?,
        country: String?,
        barcode: String?,
        notes: String?,
        photoPaths: List<String> = emptyList(),
        preserveDiscogsImages: List<String> = emptyList()
    ): Outcome<Unit>

    /** Удаляет запись по её локальному ID. */
    suspend fun removeFromCollection(id: Long): Outcome<Unit>
}
