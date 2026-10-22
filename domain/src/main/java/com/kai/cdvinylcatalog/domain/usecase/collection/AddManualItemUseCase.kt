package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Добавляет диск, которого нет в Discogs.
 *
 * Нормализация ввода (обрезка пробелов, разбор года) — здесь, а не в UI:
 * это часть доменных правил.
 *
 * @return ID созданной записи
 */
class AddManualItemUseCase @Inject constructor(
    private val writer: CollectionWriter
) {
    suspend operator fun invoke(draft: DraftCollectionItem): Outcome<Long> =
        writer.addManualItem(
            title = draft.title.trim(),
            artist = draft.artist.trim(),
            year = draft.yearValue,
            format = draft.format,
            label = draft.label?.trim()?.ifBlank { null },
            country = draft.country?.trim()?.ifBlank { null },
            barcode = draft.barcode?.trim()?.ifBlank { null },
            notes = draft.notes?.trim()?.ifBlank { null },
            photoPaths = draft.photoPaths
        )
}
