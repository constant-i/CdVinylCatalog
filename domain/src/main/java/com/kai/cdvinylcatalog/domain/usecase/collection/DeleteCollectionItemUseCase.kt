package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.catalog.localImages
import com.kai.cdvinylcatalog.domain.media.PhotoStorage
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.Outcome
import javax.inject.Inject

/**
 * Удаляет запись коллекции вместе с её фотографиями.
 *
 * Строка в базе и файлы на диске живут в разных слоях, поэтому связать их
 * может только доменный сценарий: хранилище знает о файлах, но не о записях,
 * а репозиторий — наоборот.
 *
 * Порядок операций важен. Сначала база: она источник истины. Если удалить
 * файлы, а запись в базе останется, пользователь увидит диск с битыми
 * картинками. Обратная ситуация безопасна: файл без ссылки — это просто
 * лишние килобайты, а не сломанный экран. Поэтому неудача при удалении
 * файла не отменяет успешное удаление записи.
 *
 * Картинки из Discogs лежат по URL, их трогать нельзя — [localImages]
 * отдаёт только файлы в хранилище приложения.
 */
class DeleteCollectionItemUseCase @Inject constructor(
    private val reader: CollectionReader,
    private val writer: CollectionWriter,
    private val photoStorage: PhotoStorage
) {
    suspend operator fun invoke(itemId: Long): Outcome<Unit> {
        val item = when (val outcome = reader.getItemById(itemId)) {
            is Outcome.Failure -> return outcome
            // Удалять нечего: запись уже удалена или её никогда не было
            is Outcome.Success -> outcome.data ?: return Outcome.Success(Unit)
        }

        return when (val outcome = writer.removeFromCollection(itemId)) {
            is Outcome.Failure -> outcome
            is Outcome.Success -> {
                item.release.localImages().forEach(photoStorage::delete)
                outcome
            }
        }
    }
}
