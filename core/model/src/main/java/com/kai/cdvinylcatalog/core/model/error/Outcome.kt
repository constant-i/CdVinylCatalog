package com.kai.cdvinylcatalog.core.model.error

/**
 * Результат операции на уровне Domain.
 *
 * Заменяет kotlin.Result<T>, потому что Result может нести только Throwable,
 * а мы хотим нести доменный AppError — без технических деталей.
 *
 * Аналог Either<AppError, T>, но без внешних зависимостей.
 */
sealed interface Outcome<out T> {

    /** Успешный результат */
    data class Success<out T>(val data: T) : Outcome<T>

    /** Ошибка операции — доменная, без деталей Data-слоя */
    data class Failure(val error: AppError) : Outcome<Nothing>
}