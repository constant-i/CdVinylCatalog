package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Обновляет существующую запись коллекции.
 *
 * Нормализация ввода — здесь, а не в UI: это часть доменных правил.
 */
class UpdateCollectionItemUseCase @Inject constructor(
    private val writer: CollectionWriter
) {
    suspend operator fun invoke(update: CollectionItemUpdate): Outcome<Unit> =
        with(update.draft) {
            writer.updateItem(
                id = update.id,
                title = title.trim(),
                artist = artist.trim(),
                year = yearValue,
                format = format,
                label = label?.trim()?.ifBlank { null },
                country = country?.trim()?.ifBlank { null },
                barcode = barcode?.trim()?.ifBlank { null },
                notes = notes?.trim()?.ifBlank { null },
                photoPaths = photoPaths,
                preserveDiscogsImages = update.preserveDiscogsImages
            )
        }
}
