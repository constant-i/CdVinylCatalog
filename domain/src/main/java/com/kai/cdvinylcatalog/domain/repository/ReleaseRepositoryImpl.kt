package com.kai.cdvinylcatalog.domain.repository

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.Outcome

/**
 * Репозиторий для работы с релизами в Discogs.
 *
 * Domain-интерфейс. Реализация — в :data:network.
 * Возвращает Outcome с доменным AppError — никаких Throwable наружу.
 */
interface ReleaseRepositoryImpl {

    /**
     * Ищет релизы по штрихкоду.
     *
     * @param barcode штрихкод с диска
     * @return Outcome.Success со списком релизов или Outcome.Failure с доменной ошибкой
     */
    suspend fun searchByBarcode(barcode: String): Outcome<List<Release>>

    /**
     * Ищет релизы по текстовому запросу.
     *
     * @param query поисковая строка (обрезается от пробелов в реализации)
     * @return Outcome.Success со списком релизов или Outcome.Failure с доменной ошибкой
     */
    suspend fun searchByText(query: String): Outcome<List<Release>>

    /**
     * Получает детальную информацию о релизе по его ID в Discogs.
     *
     * @param releaseId ID релиза в Discogs
     * @return Outcome.Success с релизом или Outcome.Failure с доменной ошибкой
     */
    suspend fun getReleaseDetails(releaseId: Long): Outcome<Release>
}