package com.kai.cdvinylcatalog.feature.scan.domain

import com.kai.cdvinylcatalog.core.database.CollectionRepository
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release
import javax.inject.Inject

/**
 * Добавляет релиз в коллекцию пользователя.
 *
 * @param release релиз из Discogs
 * @param format нормализованный формат (CD, VINYL, DVD, CASSETTE)
 * @param notes опциональный комментарий пользователя
 * @return ID созданной или обновлённой записи
 */
class AddToCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(
        release: Release,
        format: Format,
        notes: String? = null
    ): Long {
        return repository.addToCollection(release, format, notes)
    }
}