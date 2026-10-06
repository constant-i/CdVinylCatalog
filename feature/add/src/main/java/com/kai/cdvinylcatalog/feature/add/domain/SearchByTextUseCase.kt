package com.kai.cdvinylcatalog.feature.add.domain

import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.network.DiscogsRepository
import javax.inject.Inject

/**
 * Поиск релизов в Discogs по текстовому запросу.
 * Используется для дисков без штрихкода.
 */
class SearchByTextUseCase @Inject constructor(
    private val repository: DiscogsRepository
) {
    suspend operator fun invoke(query: String): Result<List<Release>> {
        if (query.isBlank()) {
            return Result.failure(Exception("Введите название или исполнителя"))
        }
        return repository.searchByText(query)
    }
}