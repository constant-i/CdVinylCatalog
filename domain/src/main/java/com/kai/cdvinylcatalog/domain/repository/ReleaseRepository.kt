package com.kai.cdvinylcatalog.domain.repository

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

interface ReleaseRepository {

    fun searchByBarcode(barcode: String): Flow<PagingData<Release>>
    fun searchByText(query: String): Flow<PagingData<Release>>

    suspend fun getReleaseDetails(releaseId: Long): Outcome<Release>
}