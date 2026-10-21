package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.repository.CollectionMembership
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Проверяет, есть ли релиз Discogs в коллекции пользователя.
 */
class CheckCollectionUseCase @Inject constructor(
    private val membership: CollectionMembership
) {
    suspend operator fun invoke(releaseId: Long): Outcome<Boolean> =
        membership.isInCollection(releaseId)
}
