package com.kai.cdvinylcatalog.domain.catalog

/**
 * Тип физического носителя.
 */
enum class Format {
    CD,
    DVD,
    VINYL,
    CASSETTE,
    FILE,
    UNKNOWN
}

/**
 * Маппер строки формата в enum Format.
 * Discogs возвращает "CD", "Vinyl", "DVD" и т.д.
 */
fun parseFormat(rawFormat: String?): Format {
    if (rawFormat == null) return Format.UNKNOWN
    return when {
        rawFormat.contains("CD", ignoreCase = true) -> Format.CD
        rawFormat.contains("DVD", ignoreCase = true) -> Format.DVD
        rawFormat.contains("Vinyl", ignoreCase = true) -> Format.VINYL
        rawFormat.contains("Cassette", ignoreCase = true) -> Format.CASSETTE
        rawFormat.contains("File", ignoreCase = true) -> Format.FILE
        else -> Format.UNKNOWN
    }
}

/**
 * Восстанавливает формат из имени, сохранённого в базе или в Bundle.
 *
 * Обратная операция к `Format.name`. Неизвестное значение — не ошибка:
 * данные могли быть записаны другой версией приложения, и падать
 * из-за этого незачем.
 *
 * @return распознанный формат или [Format.UNKNOWN]
 */
fun parseStoredFormat(value: String?): Format =
    value
        ?.let { stored -> Format.entries.firstOrNull { it.name == stored } }
        ?: Format.UNKNOWN