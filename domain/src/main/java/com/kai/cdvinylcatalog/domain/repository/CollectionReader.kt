package com.kai.cdvinylcatalog.domain.repository

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Чтение коллекции пользователя.
 *
 * Только чтение: клиенту этого порта не нужно (и нельзя) менять данные.
 */
interface CollectionReader {

    /** Постраничный список коллекции в заданном порядке. */
    fun getItems(sortOrder: SortOrder): Flow<PagingData<CollectionItem>>

    /** Наблюдает за одной записью по её локальному ID. */
    fun observeItemById(id: Long): Flow<CollectionItem?>

    /** Читает одну запись по локальному ID. */
    suspend fun getItemById(id: Long): Outcome<CollectionItem?>

    /**
     * Остальные копии того же релиза Discogs, кроме текущей записи.
     */
    suspend fun getOtherCopies(
        releaseId: Long,
        currentItemId: Long
    ): Outcome<List<CollectionItem>>
}
