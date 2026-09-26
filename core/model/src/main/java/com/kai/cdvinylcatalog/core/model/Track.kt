package com.kai.cdvinylcatalog.core.model

/**
 * Одна песня в треклисте релиза.
 */
data class Track(
    val position: String,
    val title: String,
    val duration: String?
)