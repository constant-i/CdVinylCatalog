package com.kai.cdvinylcatalog.domain.repository

import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Ответ на вопрос «есть ли этот релиз в коллекции».
 *
 * Отдельный порт, потому что имена методов говорят о членстве, а не о чтении
 * записей: экрану поиска нужен только этот ответ, без доступа к самой коллекции.
 */
interface CollectionMembership {

    /** Разовая проверка одного релиза. */
    suspend fun isInCollection(releaseId: Long): Outcome<Boolean>

    /**
     * Реактивный набор ID релизов Discogs, которые уже есть в коллекции.
     *
     * Нужен экрану результатов поиска, чтобы одним запросом пометить
     * «уже в коллекции» весь постранично загруженный список.
     */
    fun observeCollectionReleaseIds(): Flow<Set<Long>>
}
