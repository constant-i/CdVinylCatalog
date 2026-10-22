package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.catalog.Format

/**
 * Черновик записи коллекции: то, что пользователь ввёл в форму,
 * но что ещё не стало [com.kai.cdvinylcatalog.domain.collection.CollectionItem].
 *
 * Один объект вместо десяти аргументов: форма одна, и набор полей у неё один —
 * используется и при создании, и при редактировании.
 *
 * Здесь же живут правила, которым должен соответствовать черновик.
 * UI их только читает, чтобы вовремя гасить кнопки.
 */
data class DraftCollectionItem(
    val title: String,
    val artist: String,
    val year: String = "",
    val format: Format,
    val label: String? = null,
    val country: String? = null,
    val barcode: String? = null,
    val notes: String? = null,
    val photoPaths: List<String> = emptyList()
) {
    /** Название и исполнитель обязательны: без них диск нельзя опознать. */
    val isValid: Boolean
        get() = title.isNotBlank() && artist.isNotBlank()

    /** Не больше [MAX_PHOTOS] снимков на один диск. */
    val canAddMorePhotos: Boolean
        get() = photoPaths.size < MAX_PHOTOS

    /**
     * Год в виде числа. `null`, если поле пустое или содержит не число:
     * год необязателен, поэтому это не ошибка.
     */
    val yearValue: Int?
        get() = year.trim().toIntOrNull()?.takeIf { it > 0 }

    companion object {
        const val MAX_PHOTOS = 5
    }
}

/**
 * Обновление существующей записи коллекции.
 *
 * @property id локальный ID записи в базе
 * @property draft новые значения полей
 * @property preserveDiscogsImages обложки из Discogs, которые нужно сохранить
 *   при перезаписи списка фотографий
 */
data class CollectionItemUpdate(
    val id: Long,
    val draft: DraftCollectionItem,
    val preserveDiscogsImages: List<String> = emptyList()
)
