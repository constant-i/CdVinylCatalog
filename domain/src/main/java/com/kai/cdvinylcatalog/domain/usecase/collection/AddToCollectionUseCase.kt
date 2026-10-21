package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Добавляет релиз из Discogs в коллекцию пользователя.
 *
 * @param release релиз из Discogs
 * @param format нормализованный формат (CD, VINYL, DVD, CASSETTE)
 * @param notes опциональный комментарий пользователя
 * @return ID созданной записи
 */
class AddToCollectionUseCase @Inject constructor(
    private val writer: CollectionWriter
) {
    suspend operator fun invoke(
        release: Release,
        format: Format,
        notes: String? = null
    ): Outcome<Long> = writer.addToCollection(release, format, notes)
}
