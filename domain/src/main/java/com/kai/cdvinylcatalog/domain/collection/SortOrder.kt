package com.kai.cdvinylcatalog.domain.collection

enum class SortOrder {
    ADDED_DESC,      // по дате добавления (новые сверху)
    ADDED_ASC,       // по дате добавления (старые сверху)
    ARTIST_ASC,      // по исполнителю A→Z
    YEAR_DESC        // по году (новые сверху)
}

/**
 * Восстанавливает порядок сортировки из имени, сохранённого в Bundle.
 *
 * Обратная операция к `SortOrder.name`. Хранить имя, а не сам enum,
 * выгоднее: `SavedStateHandle` пишет в Bundle через Java-сериализацию,
 * которая сохраняет полное имя класса, а оно зависит от обфускации.
 * Строка от имён классов не зависит и читается в отладчике глазами.
 *
 * Неизвестное значение — не ошибка: состояние могло быть записано другой
 * версией приложения. В этом случае возвращается порядок по умолчанию.
 */
fun parseStoredSortOrder(value: String?): SortOrder =
    value
        ?.let { stored -> SortOrder.entries.firstOrNull { it.name == stored } }
        ?: SortOrder.ADDED_DESC
