package com.kai.cdvinylcatalog.feature.scan.domain

import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.network.DiscogsRepository
import javax.inject.Inject

/**
 * UseCase для поиска релиза по штрихкоду.
 * Инкапсулирует бизнес-логику: валидация + вызов репозитория.
 */
class SearchReleaseUseCase @Inject constructor(
    private val repository: DiscogsRepository
) {
    suspend operator fun invoke(barcode: String): Result<List<Release>> {
        if (barcode.isBlank() || barcode.length < 8) {
            return Result.failure(Exception("Некорректный штрихкод"))
        }
        return repository.searchByBarcode(barcode.trim())
    }
}