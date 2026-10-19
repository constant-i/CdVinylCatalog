package com.kai.cdvinylcatalog.data.database.entity

import androidx.room.Embedded

data class CollectionItemWithCount(
    @Embedded val item: CollectionItemEntity,
    val copyCount: Int
)