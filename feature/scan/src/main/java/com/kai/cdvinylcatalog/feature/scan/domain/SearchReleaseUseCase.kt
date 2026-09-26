package com.kai.cdvinylcatalog.feature.scan.domain

import com.kai.cdvinylcatalog.core.model.DiscogsError
import com.kai.cdvinylcatalog.core.model.ScanResult
import com.kai.cdvinylcatalog.core.network.DiscogsRepository
import javax.inject.Inject

/**
 * UseCase для поиска релиза по штрихкоду.
 * Инкапсулирует бизнес-логику: валидация + вызов репозитория.
 */
class SearchReleaseUseCase @Inject constructor(
    private val repository: DiscogsRepository
) {
    suspend operator fun invoke(barcode: String): ScanResult {
        // Валидация: штрихкод должен быть непустой и достаточно длинный
        if (barcode.isBlank() || barcode.length < 8) {
            return ScanResult.Error(
                DiscogsError.Unknown(
                    "Некорректный штрихкод"
                )
            )
        }
        return repository.searchByBarcode(barcode.trim())
    }
}