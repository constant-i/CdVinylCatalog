package com.kai.cdvinylcatalog.feature.add.domain

import com.kai.cdvinylcatalog.domain.repository.CollectionRepository
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

class CheckCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(releaseId: Long): Outcome<Boolean> {
        return repository.isInCollection(releaseId)
    }
}