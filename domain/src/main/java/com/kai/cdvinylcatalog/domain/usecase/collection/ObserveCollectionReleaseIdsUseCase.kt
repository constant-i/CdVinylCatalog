package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.repository.CollectionMembership
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Реактивный набор ID релизов Discogs, уже присутствующих в коллекции.
 *
 * Позволяет помечать результаты поиска бейджем «уже в коллекции»,
 * не выполняя запрос к базе на каждую карточку.
 */
class ObserveCollectionReleaseIdsUseCase @Inject constructor(
    private val membership: CollectionMembership
) {
    operator fun invoke(): Flow<Set<Long>> = membership.observeCollectionReleaseIds()
}
