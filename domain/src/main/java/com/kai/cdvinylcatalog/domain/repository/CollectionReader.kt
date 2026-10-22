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

    /**
     * Ищет записи в коллекции по штрихкоду.
     *
     * Возвращает **список**, потому что один штрихкод может соответствовать
     * нескольким изданиям (прессинги, переиздания). Именно поэтому результат
     * нельзя трактовать как «этот релиз уже есть» — только как
     * «диск с таким штрихкодом уже есть».
     *
     * Используется как офлайн-фолбэк: когда сети нет, Discogs недоступен,
     * и единственный способ ответить пользователю — локальная база.
     */
    suspend fun findByBarcode(barcode: String): Outcome<List<CollectionItem>>
}
