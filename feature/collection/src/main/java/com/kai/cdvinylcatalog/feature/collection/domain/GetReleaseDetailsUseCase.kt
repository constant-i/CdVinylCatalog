package com.kai.cdvinylcatalog.feature.collection.domain

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepositoryImpl
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Получение детальной информации о релизе.
 */
class GetReleaseDetailsUseCase @Inject constructor(
    private val repository: ReleaseRepositoryImpl
) {
    suspend operator fun invoke(releaseId: Long): Outcome<Release> =
        repository.getReleaseDetails(releaseId)
}