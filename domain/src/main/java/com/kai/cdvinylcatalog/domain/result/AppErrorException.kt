package com.kai.cdvinylcatalog.domain.result

/**
 * Транспортная обёртка над доменной ошибкой.
 *
 * Нужна для интеграции с библиотеками, которые умеют передавать только `Throwable`
 * (например, `PagingSource` из Paging 3 — `LoadResult.Error(error: Throwable)`).
 *
 * Сама доменная ошибка остаётся в [AppError] — здесь только контейнер,
 * чтобы Presentation-слой мог достать `AppError` и показать понятное сообщение,
 * ничего не зная о сетевых исключениях.
 */
class AppErrorException(val error: AppError) : Exception("Domain error: $error")
