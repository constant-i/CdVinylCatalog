package com.kai.cdvinylcatalog.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Строка коллекции.
 *
 * Индексы:
 * - `releaseId` — по нему фильтруют подзапросы подсчёта копий в списках
 *   коллекции (коррелированный подзапрос выполняется для каждой строки)
 *   и выборка «что уже в коллекции» для бейджей на экране поиска;
 * - `barcode` — офлайн-поиск по штрихкоду, когда сети нет.
 */
@Entity(
    tableName = "collection_items",
    indices = [
        Index(value = ["releaseId"]),
        Index(value = ["barcode"]),
    ]
)
data class CollectionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val releaseId: Long?,
    val title: String,
    val artist: String,
    val year: Int?,
    val barcode: String?,
    val coverImageUrl: String?,
    val imageUrls: String? = null,
    val label: String?,
    val rawFormat: String?,
    val country: String?,
    val releaseDate: String?,
    val catalogNumber: String?,
    val releaseNotes: String?,
    val format: String,
    val addedAt: Long,
    val userNotes: String?
)
