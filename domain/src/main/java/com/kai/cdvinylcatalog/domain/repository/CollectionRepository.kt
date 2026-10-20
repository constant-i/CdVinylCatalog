package com.kai.cdvinylcatalog.domain.repository

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {

    /** Вся коллекция без явной сортировки — по умолчанию новые сверху. */
    fun observeAll(): Flow<PagingData<CollectionItem>>

    fun getItems(sortOrder: SortOrder): Flow<PagingData<CollectionItem>>

    fun observeItemById(id: Long): Flow<CollectionItem?>
    suspend fun getItemById(id: Long): Outcome<CollectionItem?>

    suspend fun getOtherCopies(releaseId: Long, currentItemId: Long): List<CollectionItem>

    suspend fun isInCollection(releaseId: Long): Outcome<Boolean>

    /**
     * Реактивный набор ID релизов Discogs, которые уже есть в коллекции.
     *
     * Нужен экрану результатов поиска, чтобы одним запросом пометить
     * «уже в коллекции» весь постранично загруженный список.
     */
    fun observeCollectionReleaseIds(): Flow<Set<Long>>

    suspend fun addToCollection(
        release: Release,
        format: Format,
        notes: String? = null
    ): Outcome<Long>

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

    suspend fun removeFromCollection(id: Long): Outcome<Unit>
}