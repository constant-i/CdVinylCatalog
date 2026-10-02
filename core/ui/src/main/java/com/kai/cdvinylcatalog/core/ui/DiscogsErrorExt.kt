package com.kai.cdvinylcatalog.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kai.cdvinylcatalog.core.model.DiscogsError

/**
 * Превращает доменную ошибку в текст для пользователя.
 * @Composable — потому что использует stringResource для локализации.
 */
@Composable
fun DiscogsError.toHumanReadable(): String = when (this) {
    DiscogsError.NoInternet -> stringResource(R.string.error_no_internet)
    DiscogsError.Timeout -> stringResource(R.string.error_timeout)
    DiscogsError.Unauthorized -> stringResource(R.string.error_unauthorized)
    DiscogsError.RateLimited -> stringResource(R.string.error_rate_limited)
    DiscogsError.NotFound -> stringResource(R.string.error_not_found)
    is DiscogsError.ServerError -> stringResource(R.string.error_server, code)
    is DiscogsError.Unknown -> stringResource(R.string.error_unknown, message ?: "")
}