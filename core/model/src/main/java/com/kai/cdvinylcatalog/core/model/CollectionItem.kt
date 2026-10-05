package com.kai.cdvinylcatalog.core.model

/**
 * Запись в коллекции пользователя.
 * Хранится в локальной базе данных (Room).
 *
 * Release.id = 0L для дисков, добавленных вручную
 */
data class CollectionItem(
    val id: Long,
    val release: Release,
    val format: Format,
    val quantity: Int = 1,
    val addedAt: Long,
    val notes: String? = null
) {
    init {
        require(quantity > 0) { "Количество копий должно быть больше нуля" }
    }
}