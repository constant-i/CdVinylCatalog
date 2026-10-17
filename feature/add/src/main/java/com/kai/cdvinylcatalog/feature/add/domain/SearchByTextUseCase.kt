package com.kai.cdvinylcatalog.feature.add.domain

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Поиск релизов в Discogs по текстовому запросу.
 *
 * Валидация запроса — на уровне UI.
 */
class SearchByTextUseCase @Inject constructor(
    private val repository: ReleaseRepository
) {
    suspend operator fun invoke(query: String): Outcome<List<Release>> =
        repository.searchByText(query)
}