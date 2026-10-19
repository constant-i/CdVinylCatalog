package com.kai.cdvinylcatalog.feature.add.domain

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepositoryImpl
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Поиск релизов в Discogs по штрихкоду.
 *
 * Валидация штрихкода — на уровне UI (кнопка disabled).
 * UseCase предполагает валидный вход.
 */
class SearchReleaseUseCase @Inject constructor(
    private val repository: ReleaseRepositoryImpl
) {
    suspend operator fun invoke(barcode: String): Outcome<List<Release>> =
        repository.searchByBarcode(barcode.trim())
}