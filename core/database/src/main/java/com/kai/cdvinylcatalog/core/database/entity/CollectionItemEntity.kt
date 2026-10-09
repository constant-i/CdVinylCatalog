package com.kai.cdvinylcatalog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collection_items")
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