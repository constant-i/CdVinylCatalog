package com.kai.cdvinylcatalog.feature.scan.domain

import com.kai.cdvinylcatalog.core.database.CollectionRepository
import javax.inject.Inject

/**
 * Проверяет, есть ли релиз уже в коллекции пользователя.
 */
class CheckCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(releaseId: Long): Boolean {
        return repository.isInCollection(releaseId)
    }
}