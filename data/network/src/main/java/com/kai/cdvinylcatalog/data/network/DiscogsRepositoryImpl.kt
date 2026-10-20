package com.kai.cdvinylcatalog.data.network

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kai.cdvinylcatalog.data.network.dto.toDomain
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscogsRepositoryImpl @Inject constructor(
    private val api: DiscogsApi
) : ReleaseRepository {

    override fun searchByBarcode(barcode: String): Flow<PagingData<Release>> =
        pager { DiscogsSearchPagingSource(api, barcode = barcode.trim()) }

    override fun searchByText(query: String): Flow<PagingData<Release>> =
        pager { DiscogsSearchPagingSource(api, query = query.trim()) }

    override suspend fun getReleaseDetails(releaseId: Long): Outcome<Release> =
        safeApiCall {
            api.getReleaseDetails(
                releaseId = releaseId,
                token = NetworkConstants.DISCOGS_TOKEN
            ).toDomain()
        }

    /**
     * Единая конфигурация Paging для всех сетевых поисков.
     */
    private fun pager(pagingSourceFactory: () -> DiscogsSearchPagingSource): Flow<PagingData<Release>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE / 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow

    private suspend inline fun <T> safeApiCall(
        crossinline block: suspend () -> T
    ): Outcome<T> = withContext(Dispatchers.IO) {
        try {
            Outcome.Success(block())
        } catch (e: Exception) {
            Outcome.Failure(mapExceptionToAppError(e))
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
