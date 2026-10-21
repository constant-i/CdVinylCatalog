package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Обновляет существующую запись коллекции.
 */
class UpdateCollectionItemUseCase @Inject constructor(
    private val writer: CollectionWriter
) {
    suspend operator fun invoke(command: UpdateCollectionItemCommand): Outcome<Unit> =
        with(command.fields) {
            writer.updateItem(
                id = command.id,
                title = title,
                artist = artist,
                year = year,
                format = format,
                label = label,
                country = country,
                barcode = barcode,
                notes = notes,
                photoPaths = photoPaths,
                preserveDiscogsImages = command.preserveDiscogsImages
            )
        }
}
