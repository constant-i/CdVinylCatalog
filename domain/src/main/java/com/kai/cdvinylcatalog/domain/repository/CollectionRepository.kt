package com.kai.cdvinylcatalog.domain.repository

import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {

    fun observeAll(): Flow<List<CollectionItem>>
    fun observeItemById(id: Long): Flow<CollectionItem?>
    suspend fun getItemById(id: Long): Outcome<CollectionItem?>

    fun getItems(sortOrder: SortOrder): Flow<List<CollectionItem>>

    suspend fun getOtherCopies(releaseId: Long, currentItemId: Long): List<CollectionItem>

    suspend fun isInCollection(releaseId: Long): Outcome<Boolean>

    suspend fun addToCollection(
        release: Release,
        format: Format,
        notes: String? = null
    ): Outcome<Long>

    suspend fun addManualItem(
        title: String,
        artist: String,
        year: Int?,
        format: com.kai.cdvinylcatalog.domain.catalog.Format,
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
        format: com.kai.cdvinylcatalog.domain.catalog.Format,
        label: String?,
        country: String?,
        barcode: String?,
        notes: String?,
        photoPaths: List<String> = emptyList(),
        preserveDiscogsImages: List<String> = emptyList()
    ): Outcome<Unit>

    suspend fun removeFromCollection(id: Long): Outcome<Unit>
}