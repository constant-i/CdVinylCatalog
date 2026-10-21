package com.kai.cdvinylcatalog.domain.usecase.catalog

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.catalog.mergeWith
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Детальная информация о релизе из Discogs.
 *
 * Если передана локальная запись ([localItem]), поля пользователя имеют
 * приоритет над данными Discogs — это позволяет экрану деталей показывать
 * отредактированные данные, не теряя треклист и обложки.
 */
class GetReleaseDetailsUseCase @Inject constructor(
    private val repository: ReleaseRepository
) {
    suspend operator fun invoke(
        releaseId: Long,
        localItem: Release? = null
    ): Outcome<Release> =
        when (val outcome = repository.getReleaseDetails(releaseId)) {
            is Outcome.Success -> Outcome.Success(
                localItem?.mergeWith(outcome.data) ?: outcome.data
            )
            is Outcome.Failure -> outcome
        }
}
