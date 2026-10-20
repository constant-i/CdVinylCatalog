package com.kai.cdvinylcatalog.feature.add.domain

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchReleaseUseCase @Inject constructor(
    private val repository: ReleaseRepository
) {
    operator fun invoke(barcode: String): Flow<PagingData<Release>> =
        repository.searchByBarcode(barcode.trim())
}