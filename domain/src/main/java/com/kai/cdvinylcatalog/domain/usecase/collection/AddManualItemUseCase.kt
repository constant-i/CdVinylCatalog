package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Добавляет диск, которого нет в Discogs.
 *
 * @return ID созданной записи
 */
class AddManualItemUseCase @Inject constructor(
    private val writer: CollectionWriter
) {
    suspend operator fun invoke(command: CollectionItemCommand): Outcome<Long> =
        writer.addManualItem(
            title = command.title,
            artist = command.artist,
            year = command.year,
            format = command.format,
            label = command.label,
            country = command.country,
            barcode = command.barcode,
            notes = command.notes,
            photoPaths = command.photoPaths
        )
}
