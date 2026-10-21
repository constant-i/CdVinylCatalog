package com.kai.cdvinylcatalog.feature.add.domain

import com.kai.cdvinylcatalog.domain.repository.CollectionMembership
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

class CheckCollectionUseCase @Inject constructor(
    private val repository: CollectionMembership
) {
    suspend operator fun invoke(releaseId: Long): Outcome<Boolean> {
        return repository.isInCollection(releaseId)
    }
}
