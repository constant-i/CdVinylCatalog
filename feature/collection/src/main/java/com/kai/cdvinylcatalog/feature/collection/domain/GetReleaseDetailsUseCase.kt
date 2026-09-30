package com.kai.cdvinylcatalog.feature.collection.domain

import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.network.DiscogsRepository
import javax.inject.Inject

class GetReleaseDetailsUseCase @Inject constructor(
    private val repository: DiscogsRepository
) {
    suspend operator fun invoke(releaseId: Long): Result<Release> {
        return repository.getReleaseDetails(releaseId)
    }
}