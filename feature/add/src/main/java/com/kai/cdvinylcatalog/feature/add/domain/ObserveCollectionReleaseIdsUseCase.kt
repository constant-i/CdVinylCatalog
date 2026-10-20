package com.kai.cdvinylcatalog.feature.add.domain

import com.kai.cdvinylcatalog.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Реактивный набор ID релизов Discogs, уже присутствующих в коллекции.
 *
 * Позволяет помечать результаты поиска бейджем «уже в коллекции»,
 * не выполняя запрос к базе на каждую карточку.
 */
class ObserveCollectionReleaseIdsUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    operator fun invoke(): Flow<Set<Long>> = repository.observeCollectionReleaseIds()
}
