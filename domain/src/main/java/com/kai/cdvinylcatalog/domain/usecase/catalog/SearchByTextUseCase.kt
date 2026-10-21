package com.kai.cdvinylcatalog.domain.usecase.catalog

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Поиск релизов в Discogs по текстовому запросу.
 */
class SearchByTextUseCase @Inject constructor(
    private val repository: ReleaseRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Release>> =
        repository.searchByText(query)
}
