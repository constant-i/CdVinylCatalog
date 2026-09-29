package com.kai.cdvinylcatalog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collection_items")
data class CollectionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Данные релиза (денормализованы для простоты)
    val releaseId: Long,
    val title: String,
    val artist: String,
    val year: Int?,
    val barcode: String?,
    val coverImageUrl: String?,
    val label: String?,
    val rawFormat: String?,
    val country: String?,
    val releaseDate: String?,
    val catalogNumber: String?,
    val releaseNotes: String?,

    // Данные коллекции
    val format: String,
    val quantity: Int,
    val addedAt: Long,
    val userNotes: String?
)