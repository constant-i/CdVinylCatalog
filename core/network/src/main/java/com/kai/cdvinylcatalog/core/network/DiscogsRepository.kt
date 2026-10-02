package com.kai.cdvinylcatalog.core.network

import com.kai.cdvinylcatalog.core.model.DiscogsError
import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.network.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с Discogs API.
 * Инкапсулирует вызовы API, обработку ошибок и маппинг.
 */
@Singleton
class DiscogsRepository @Inject constructor(
    private val api: DiscogsApi
) {

    /**
     * Ищет релизы по штрихкоду.
     *
     * @param barcode штрихкод с диска
     * @return ScanResult.Found или ScanResult.Error
     */
    suspend fun searchByBarcode(barcode: String): Result<List<Release>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.searchByBarcode(
                    barcode = barcode,
                    token = NetworkConstants.DISCOGS_TOKEN
                )
                Result.success(response.results.map { it.toDomain() })
            } catch (e: UnknownHostException) {
                Result.failure(Exception("Нет подключения к интернету"))
            } catch (e: SocketTimeoutException) {
                Result.failure(Exception("Сервер не отвечает"))
            } catch (e: HttpException) {
                Result.failure(Exception("Ошибка сервера: ${e.code()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun mapHttpException(e: HttpException): DiscogsError {
        return when (e.code()) {
            401, 403 -> DiscogsError.Unauthorized
            404 -> DiscogsError.NotFound
            429 -> DiscogsError.RateLimited
            in 500..599 -> DiscogsError.ServerError(e.code())
            else -> DiscogsError.Unknown(e.message())
        }
    }

    /**
     * Поиск информации о конкретном релизе по ID релиза в базе Discogs (releaseId).
     *
     * @param releaseId ID релиза в базе Discogs
     * @return ScanResult.Found или ScanResult.Error
     */
    suspend fun getReleaseDetails(releaseId: Long): Result<Release> =
        withContext(Dispatchers.IO) {
            try {
                val dto = api.getReleaseDetails(
                    releaseId = releaseId,
                    token = NetworkConstants.DISCOGS_TOKEN
                )
                Result.success(dto.toDomain())
            } catch (e: UnknownHostException) {
                Result.failure(Exception("Нет подключения к интернету"))
            } catch (e: SocketTimeoutException) {
                Result.failure(Exception("Сервер не отвечает"))
            } catch (e: HttpException) {
                Result.failure(Exception("Ошибка сервера: ${e.code()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Ищет релизы по текстовому запросу в базе Discogs.
     *
     * @param query поисковая строка (обрезается от пробелов)
     * @return [Result.success] со списком релизов или [Result.failure] с причиной ошибки
     */
    suspend fun searchByText(query: String): Result<List<Release>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.searchByText(
                    query = query.trim(),
                    token = NetworkConstants.DISCOGS_TOKEN
                )
                val releases = response.results.map { it.toDomain() }
                Result.success(releases)
            } catch (e: UnknownHostException) {
                Result.failure(Exception("Нет подключения к интернету"))
            } catch (e: SocketTimeoutException) {
                Result.failure(Exception("Сервер не отвечает"))
            } catch (e: HttpException) {
                Result.failure(Exception("Ошибка сервера: ${e.code()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}