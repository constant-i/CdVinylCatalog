package com.kai.cdvinylcatalog.domain.catalog

/**
 * Одна песня в треклисте релиза.
 */
data class Track(
    val position: String,
    val title: String,
    val duration: String?
)