package com.kai.cdvinylcatalog.core.ui

import android.content.Context
import com.kai.cdvinylcatalog.core.model.error.AppError

/**
 * Превращает доменную ошибку в текст для пользователя.
 *
 * Обычная функция (не Composable), принимает Context.
 * Используется в UiMessageMapper и напрямую, если нужно.
 */
fun AppError.asText(context: Context): String = when (this) {
    AppError.NoConnection -> context.getString(R.string.error_no_connection)
    AppError.ServerUnavailable -> context.getString(R.string.error_server_unavailable)
    AppError.AuthenticationRequired -> context.getString(R.string.error_auth_required)
    AppError.RateLimitExceeded -> context.getString(R.string.error_rate_limit)
    AppError.ResourceNotFound -> context.getString(R.string.error_not_found)
    AppError.Unknown -> context.getString(R.string.error_unknown)
}