package com.kai.cdvinylcatalog.domain.result

/**
 * Доменные ошибки приложения.
 *
 * Не привязаны к конкретному API — если завтра добавим второй источник данных
 * (MusicBrainz, Discogs Marketplace), маппинг в AppError делается в Data-слое.
 *
 * Описывают бизнес-состояния, а не HTTP-коды.
 * Никаких технических сообщений и кодов — только то, что важно UI.
 */
sealed interface AppError {

    /** Нет подключения к интернету */
    data object NoConnection : AppError

    /** Сервер недоступен: timeout, 5xx, DNS-ошибка */
    data object ServerUnavailable : AppError

    /** Требуется авторизация: 401/403, невалидный токен */
    data object AuthenticationRequired : AppError

    /** Превышен лимит запросов: 429 */
    data object RateLimitExceeded : AppError

    /** Ресурс не найден: 404 */
    data object ResourceNotFound : AppError

    /**
     * Не удалось прочитать или записать данные на устройстве:
     * ошибка SQLite, нет места, повреждён файл базы.
     */
    data object LocalStorage : AppError

    /** Неизвестная ошибка — без деталей, чтобы не тащить технические сообщения в Domain */
    data object Unknown : AppError
}