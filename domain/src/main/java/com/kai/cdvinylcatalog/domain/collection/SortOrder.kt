package com.kai.cdvinylcatalog.domain.collection

enum class SortOrder {
    ADDED_DESC,      // по дате добавления (новые сверху)
    ADDED_ASC,       // по дате добавления (старые сверху)
    ARTIST_ASC,      // по исполнителю A→Z
    YEAR_DESC        // по году (новые сверху)
}