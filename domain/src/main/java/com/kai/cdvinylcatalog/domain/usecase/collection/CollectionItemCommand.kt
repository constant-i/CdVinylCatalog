package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.catalog.Format

/**
 * Данные формы ручного добавления или редактирования диска.
 *
 * Один объект вместо десяти аргументов: форма одна, и набор полей у неё один.
 */
data class CollectionItemCommand(
    val title: String,
    val artist: String,
    val year: Int?,
    val format: Format,
    val label: String? = null,
    val country: String? = null,
    val barcode: String? = null,
    val notes: String? = null,
    val photoPaths: List<String> = emptyList()
)

/**
 * Команда обновления существующей записи.
 *
 * @property id локальный ID записи в базе
 * @property preserveDiscogsImages обложки из Discogs, которые нужно сохранить
 *   при перезаписи списка фотографий
 */
data class UpdateCollectionItemCommand(
    val id: Long,
    val fields: CollectionItemCommand,
    val preserveDiscogsImages: List<String> = emptyList()
)
