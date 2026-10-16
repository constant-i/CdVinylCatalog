package com.kai.cdvinylcatalog.domain.collection

import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release

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
    val addedAt: Long,
    val notes: String? = null,
    val copyCount: Int = 1
)