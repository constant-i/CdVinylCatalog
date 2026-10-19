package com.kai.cdvinylcatalog.data.network

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepositoryImpl
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.Outcome
import com.kai.cdvinylcatalog.data.network.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация ReleaseRepository для работы с Discogs API.
 *
 * Возвращает Outcome<T> с доменным AppError — никакие технические
 * детали (HTTP-коды, исключения) не утекают наружу.
 */
@Singleton
class DiscogsRepositoryImpl @Inject constructor(
    private val api: DiscogsApi
) : ReleaseRepositoryImpl {

    override suspend fun searchByBarcode(barcode: String): Outcome<List<Release>> =
        safeApiCall {
            api.searchByBarcode(
                barcode = barcode,
                token = NetworkConstants.DISCOGS_TOKEN
            ).results.map { it.toDomain() }
        }

    override suspend fun searchByText(query: String): Outcome<List<Release>> =
        safeApiCall {
            api.searchByText(
                query = query.trim(),
                token = NetworkConstants.DISCOGS_TOKEN
            ).results.map { it.toDomain() }
        }

    override suspend fun getReleaseDetails(releaseId: Long): Outcome<Release> =
        safeApiCall {
            api.getReleaseDetails(
                releaseId = releaseId,
                token = NetworkConstants.DISCOGS_TOKEN
            ).toDomain()
        }

    /**
     * Общий безопасный вызов API.
     * Маппит исключения в доменный AppError.
     */
    private suspend inline fun <T> safeApiCall(
        crossinline block: suspend () -> T
    ): Outcome<T> = withContext(Dispatchers.IO) {
        try {
            Outcome.Success(block())
        } catch (e: UnknownHostException) {
            Outcome.Failure(AppError.NoConnection)
        } catch (e: SocketTimeoutException) {
            Outcome.Failure(AppError.ServerUnavailable)
        } catch (e: HttpException) {
            Outcome.Failure(mapHttpException(e))
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }

    private fun mapHttpException(e: HttpException): AppError = when (e.code()) {
        401, 403 -> AppError.AuthenticationRequired
        404 -> AppError.ResourceNotFound
        429 -> AppError.RateLimitExceeded
        in 500..599 -> AppError.ServerUnavailable
        else -> AppError.Unknown
    }
}