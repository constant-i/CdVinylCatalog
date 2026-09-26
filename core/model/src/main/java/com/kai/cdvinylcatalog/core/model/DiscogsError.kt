package com.kai.cdvinylcatalog.core.model

/**
 * Доменные ошибки, которые могут возникнуть при работе с Discogs API.
 * Сетевой слой маппит HTTP-ошибки и исключения в эти типы.
 */
sealed class DiscogsError {
    /** Нет подключения к интернету */
    data object NoInternet : DiscogsError()

    /** Сервер не ответил за отведённое время */
    data object Timeout : DiscogsError()

    /** Discogs вернул 401/403 — проблема с токеном */
    data object Unauthorized : DiscogsError()

    /** Discogs вернул 429 — превышен лимит запросов */
    data object RateLimited : DiscogsError()

    /** Релиз не найден (404) */
    data object NotFound : DiscogsError()

    /** Сервер вернул 5xx */
    data class ServerError(val code: Int) : DiscogsError()

    /** Неизвестная ошибка с текстом */
    data class Unknown(val message: String?) : DiscogsError()
}