package com.kai.cdvinylcatalog.data.network

import com.kai.cdvinylcatalog.domain.result.AppError
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Единственное место в Data-слое, где сетевые исключения превращаются в доменные ошибки.
 *
 * Используется и в [DiscogsRepositoryImpl] (через `safeApiCall`),
 * и в [DiscogsSearchPagingSource] (через `LoadResult.Error`).
 */
internal fun mapExceptionToAppError(e: Throwable): AppError = when (e) {
    is UnknownHostException -> AppError.NoConnection
    is SocketTimeoutException -> AppError.ServerUnavailable
    is HttpException -> when (e.code()) {
        401, 403 -> AppError.AuthenticationRequired
        404 -> AppError.ResourceNotFound
        429 -> AppError.RateLimitExceeded
        in 500..599 -> AppError.ServerUnavailable
        else -> AppError.Unknown
    }
    else -> AppError.Unknown
}
